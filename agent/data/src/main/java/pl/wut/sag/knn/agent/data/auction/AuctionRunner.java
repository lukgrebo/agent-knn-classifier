package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.ClusteringAgentRunner;
import pl.wut.sag.knn.agent.data.DataAgent;
import pl.wut.sag.knn.agent.data.model.Auction;
import pl.wut.sag.knn.agent.data.model.AuctionStatus;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.MiningRequest;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;


public interface AuctionRunner {
    void handleBid(final Bid bid, final AID sender);

    AuctionStatus getAuctionStatus();
}

@Slf4j
class DefaultAuctionRunner implements AuctionRunner {

    DefaultAuctionRunner(final MiningRequest request, final Codec codec, final Queue<ObjectWithAttributes> objectsToPropose, final ServiceDiscovery serviceDiscovery, final DataAgent dataAgent, final ClusteringAgentRunner clusteringAgentRunner) {
        this.request = request;
        this.codec = codec;
        this.objectsToPropose = objectsToPropose;
        this.serviceDiscovery = serviceDiscovery;
        this.dataAgent = dataAgent;
        this.clusteringAgentRunner = clusteringAgentRunner;
        this.finalizer = MiningFinalizer.finalizer(serviceDiscovery, dataAgent, codec, objectsToPropose.stream().collect(Collectors.toMap(ObjectWithAttributes::getId, Function.identity())));
        startNewAuction();
    }

    private final MiningRequest request;
    private final Codec codec;
    private final Queue<ObjectWithAttributes> objectsToPropose;
    private Auction currentAuction;
    private final ServiceDiscovery serviceDiscovery;
    private final DataAgent dataAgent;
    private final ClusteringAgentRunner clusteringAgentRunner;
    private final MiningFinalizer finalizer;
    private final Beliefs beliefs = new Beliefs();

    @Override
    public void handleBid(final Bid bid, final AID sender) {
        if (!bid.getObjectUuid().equals(currentAuction.getObject().getId())) {
            log.error("Auction for object" + bid.getObjectUuid() + " has already ended");
        }

        currentAuction.registerBid(sender, bid);

        if (beliefs.shouldFinalizeAuction()) {
            finalize(currentAuction);
            startNewAuction();
        } else if (beliefs.shouldCreateNewAgent()) {
            final int currentAgentNumber = findAllClusteringAgents().result().size();
            final Result<Void, StaleProxyException> result = clusteringAgentRunner.runClusteringAgent();
            Result<Boolean, FIPAException> agentRegisteredCheck = findAllClusteringAgents().mapResult(l -> l.size() > currentAgentNumber);
            while (!(agentRegisteredCheck.isValid() && agentRegisteredCheck.result())) {
                agentRegisteredCheck = findAllClusteringAgents().mapResult(l -> l.size() > currentAgentNumber);
            }
            log.info("Highest bid unsatisfactory {}, creating new agent {}: ", getHighestBid(), result);
            proposeCurrentObject();
        } else {
            log.info("Waiting for other bids, got {} bids", currentAuction.getBids().size());
        }

    }

    private void startNewAuction() {
        final Optional<ObjectWithAttributes> objectToProcess = Optional.ofNullable(objectsToPropose.poll());
        objectToProcess.ifPresent(o -> currentAuction = new Auction(o));
        if (objectToProcess.isPresent()) {
            log.info("New object is being processed, objects left {}", objectsToPropose.size());
            proposeCurrentObject();
        } else {
            log.info("No new auction will be started. Auction ended.");
            log.info("Currently there are: " + findAllClusteringAgents().result().size() + " agents");
            finalizer.finalizeMining();
        }
    }


    private Result<List<DFAgentDescription>, FIPAException> findAllClusteringAgents() {
        return serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
    }

    @Override
    public AuctionStatus getAuctionStatus() {
        final Result<List<DFAgentDescription>, FIPAException> allServices = findAllClusteringAgents();

        final int bidders = allServices.result().size();
        return new AuctionStatus(request.getRequestId(), bidders, objectsToPropose.size(), objectsToPropose.isEmpty());
    }

    private Void proposeCurrentObject() {
        final Result<List<DFAgentDescription>, FIPAException> clusters = findAllClusteringAgents();
        if (clusters.isError()) {
            log.error("Error occured during current object proposal: " + clusters.error());
        } else if (clusters.result().isEmpty()) {
            log.error("No clustering agents found!!!");
        }
        final List<AID> names = clusters.result().stream().map(DFAgentDescription::getName).collect(Collectors.toList());
        log.info("Sending CFP for current object {} to {} clusters", currentAuction.getObject().getId(), names.size());
        final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
        names.forEach(message::addReceiver);
        message.setContent(codec.encode(currentAuction.getObject()));
        dataAgent.send(message);

        return null;
    }

    private void finalize(final Auction auction) {
        auction.getBids().entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().getValue()))
                .ifPresent(this::sendObject);
    }

    private void sendObject(final Map.Entry<AID, Bid> aidBidEntry) {
        final ACLMessage message = AuctionProtocol.acceptBidAndSendObject.templatedMessage();
        message.addReceiver(aidBidEntry.getKey());
        log.info("Sending object to auction winner {}", aidBidEntry.getKey().getName());
        message.setContent(codec.encode(currentAuction.getObject()));
        dataAgent.send(message);
    }

    private double getHighestBid() {
        return currentAuction.getBids().values().stream()
                .map(Bid::getValue)
                .max(Double::compare)
                .orElse(0D);
    }


    private class Beliefs {

        boolean shouldCreateNewAgent() {
            return allAgentsAlreadyBidded() && !highestOfferSatisfiesMinimalValue();
        }

        boolean shouldFinalizeAuction() {
            return allAgentsAlreadyBidded() && highestOfferSatisfiesMinimalValue();
        }

        private boolean highestOfferSatisfiesMinimalValue() {
            return getHighestBid() >= request.getMinimalBid();
        }

        boolean allAgentsAlreadyBidded() {
            final List<AID> alreadyBidded = new ArrayList<>(currentAuction.getBids().keySet());

            final Result<List<AID>, FIPAException> cluteringAgentSearchResult = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService())
                    .mapResult(a -> a.stream().map(DFAgentDescription::getName).collect(Collectors.toList()));
            log.debug("Found {} agents and {} bids", cluteringAgentSearchResult.result().size(), alreadyBidded.size());

            return cluteringAgentSearchResult.isValid() && alreadyBidded.containsAll(cluteringAgentSearchResult.result());
        }
    }
}
