package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.ClusteringAgentRunner;
import pl.wut.sag.knn.agent.data.model.Auction;
import pl.wut.sag.knn.agent.data.model.AuctionStatus;
import pl.wut.sag.knn.infrastructure.MessageSender;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;


public interface AuctionRunner {
    void handleBid(final Bid bid, final AID sender);

    AuctionStatus getAuctionStatus();
}

@Slf4j
class DefaultAuctionRunner implements AuctionRunner {

    DefaultAuctionRunner(final UUID correspondingRequestUUID, final Codec codec, final Queue<ObjectWithAttributes> objectsToPropose, final ServiceDiscovery serviceDiscovery, final MessageSender messageSender, final ClusteringAgentRunner clusteringAgentRunner) {
        this.correspondingRequestUUID = correspondingRequestUUID;
        this.codec = codec;
        this.objectsToPropose = objectsToPropose;
        this.serviceDiscovery = serviceDiscovery;
        this.messageSender = messageSender;
        this.clusteringAgentRunner = clusteringAgentRunner;
        startNewAuction();
    }

    private final UUID correspondingRequestUUID;
    private final Codec codec;
    private final Queue<ObjectWithAttributes> objectsToPropose;
    private Auction currentAuction;
    private final ServiceDiscovery serviceDiscovery;
    private final MessageSender messageSender;
    private final ClusteringAgentRunner clusteringAgentRunner;
    private final Beliefs beliefs = new Beliefs();

    @Override
    public void handleBid(final Bid bid, final AID sender) {
        if (!bid.getObjectUuid().equals(currentAuction.getObject().getId())) {
            log.error("Auction for object" + bid.getObjectUuid() + " has already ended");
        }

        currentAuction.registerBid(sender, bid);

        if (beliefs.shouldFinalizeAuction(currentAuction)) {
            finalize(currentAuction);
            startNewAuction();
        } else if (beliefs.shouldCreateNewAgent(currentAuction)) {
            final Result<Void, StaleProxyException> result = clusteringAgentRunner.runClusteringAgent();
            log.info("New clustering agent creation result: " + result);
            proposeCurrentObject();
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
        }
    }

    @Override
    public AuctionStatus getAuctionStatus() {
        final Result<List<DFAgentDescription>, FIPAException> allServices = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());

        final int bidders = allServices.result().size();
        return new AuctionStatus(correspondingRequestUUID, bidders, objectsToPropose.size(), objectsToPropose.isEmpty());
    }

    private Void proposeCurrentObject() {
        final Result<List<DFAgentDescription>, FIPAException> clusters = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
        if (clusters.isError()) {
            log.error("Either error occured during current object proposal: " + clusters.error());
            return proposeCurrentObject();
        } else if (clusters.result().isEmpty()) {
            log.info("No clustering agents found, trying to create new {}", clusteringAgentRunner.runClusteringAgent());
            return proposeCurrentObject();
        }
        final List<AID> names = clusters.result().stream().map(DFAgentDescription::getName).collect(Collectors.toList());
        log.info("Sending CFP for current object {} to {} clusters", currentAuction.getObject().getId(), names.size());
        final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
        names.forEach(message::addReceiver);
        message.setContent(codec.encode(currentAuction.getObject()));
        messageSender.send(message);

        return null;
    }

    private void finalize(final Auction auction) {
        auction.getBids().entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().getValue()))
                .ifPresent(this::sendObject);
    }

    private void sendObject(final Map.Entry<AID, Bid> aidBidEntry) {
        final ACLMessage message = AuctionProtocol.acceptBidAndSendObject.templatedMessage();
        message.addReceiver(aidBidEntry.getKey());
        message.setContent(codec.encode(currentAuction.getObject()));
        messageSender.send(message);
    }

    private class Beliefs {

        boolean shouldCreateNewAgent(final Auction auction) {
            return allAgentsAlreadyBidded(auction) && !highestOfferSatisfiesMinimalValue(auction);
        }

        boolean shouldFinalizeAuction(final Auction auction) {
            return allAgentsAlreadyBidded(auction) && highestOfferSatisfiesMinimalValue(auction);
        }

        private boolean highestOfferSatisfiesMinimalValue(final Auction auction) {
            return auction.getBids().values().stream()
                    .map(Bid::getValue)
                    .max(Double::compare)
                    .orElse(0D) >= 1.00;
        }

        boolean allAgentsAlreadyBidded(final Auction auction) {
            final List<AID> alreadyBidded = new ArrayList<>(auction.getBids().keySet());

            final Result<List<AID>, FIPAException> cluteringAgentSearchResult = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService())
                    .mapResult(a -> a.stream().map(DFAgentDescription::getName).collect(Collectors.toList()));

            return cluteringAgentSearchResult.isValid() && alreadyBidded.containsAll(cluteringAgentSearchResult.result());
        }
    }
}
