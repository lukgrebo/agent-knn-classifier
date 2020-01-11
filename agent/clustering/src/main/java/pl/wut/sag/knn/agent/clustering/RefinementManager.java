package pl.wut.sag.knn.agent.clustering;


import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.model.OwnedBid;
import pl.wut.sag.knn.infrastructure.collection.CollectionUtil;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.ontology.auction.RefinementSummary;
import pl.wut.sag.knn.ontology.auction.StartRefinementRequest;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface RefinementManager {
    void startRefinement(final ACLMessage message);

    void handleBid(final ACLMessage message);

    boolean isRefinementDone();

    static RefinementManager newManager(final ClusteringAgent agent) {
        return new DefaultRefinementManager(agent);
    }
}

@Slf4j
@RequiredArgsConstructor
class DefaultRefinementManager implements RefinementManager {

    private static final int DEFAULT_REFINEMENT_SIZE = 15;

    private final ClusteringAgent agent;
    private final Map<UUID, Set<OwnedBid>> bids = new HashMap<>();
    private int refinementSize = DEFAULT_REFINEMENT_SIZE;
    private int expectedBidderCount;
    private int soldObjects;
    private ACLMessage refinementStartMessage;

    @Override
    public void startRefinement(final ACLMessage incomingMessage) {
        log.info("{} Got request to start refinement!", agent.getName());
        this.refinementStartMessage = incomingMessage;
        final StartRefinementRequest request = agent.codec.decode(incomingMessage.getContent(), AuctionProtocol.startRefinementRequest.getMessageClass()).result();
        this.refinementSize = request.getRefinementSize();
        offerMostDistantElementToOtherAgents();
    }

    @Override
    public void handleBid(final ACLMessage message) {
        final Bid bid = agent.codec.decode(message.getContent(), AuctionProtocol.sendBid.getMessageClass()).result();
        log.info("Got refinement bid for object {} from {} value {}", bid.getObjectUuid(), message.getSender().getName(), bid.getValue());
        bids.merge(bid.getObjectUuid(), CollectionUtil.initializedMutableCollection(HashSet::new, new OwnedBid(bid, message.getSender())), CollectionUtil::mergeToSet);
        final Optional<ObjectWithAttributes> maybeElement = agent.managedCluster.viewElements().stream().filter(e -> e.getId().equals(bid.getObjectUuid())).findFirst();
        final Set<OwnedBid> allBids = this.bids.get(bid.getObjectUuid());
        if (maybeElement.isPresent() && allBids.size() == expectedBidderCount) {
            final ObjectWithAttributes element = maybeElement.get();

            final Optional<OwnedBid> maybeMaxBid = allBids.stream().max(Comparator.comparingDouble(OwnedBid::getValue));

            maybeMaxBid.ifPresent(maxBid -> {
                agent.managedCluster.getElements().remove(element);
                final ACLMessage refiningMessage = AuctionProtocol.acceptBidAndSendObject.templatedMessage();
                refiningMessage.setContent(agent.codec.encode(element));
                refiningMessage.addReceiver(maxBid.getOwner());
                log.info("Sending object {} to agent {}", maxBid.getObjectUuid(), maxBid.getOwner());
                agent.send(refiningMessage);
                soldObjects += 1;
                if (!isRefinementDone()) {
                    offerMostDistantElementToOtherAgents();
                } else {
                    informDataAgentOfFinishedRefinement();
                }
            });
        }
    }

    private void informDataAgentOfFinishedRefinement() {
        log.info("Informing data agent of finished refinement");
        final ClusterSummary summary = new ClusterSummary(agent.managedCluster.viewElements().stream().map(ObjectWithAttributes::getId).collect(Collectors.toSet()),
                agent.distanceCalculator.calculateAverageDistaneInCluster(agent.managedCluster.viewElements()));
        final RefinementSummary refinementSummary = RefinementSummary.of(summary);
        final ACLMessage messageToDataAgent = AuctionProtocol.refinementFinishedResponse.toResponse(refinementStartMessage, agent.codec.encode(refinementSummary));

        agent.send(messageToDataAgent);
    }

    private void offerMostDistantElementToOtherAgents() {
        final Result<List<DFAgentDescription>, FIPAException> agents = agent.serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
        this.expectedBidderCount = agents.result().size();
        final Optional<ObjectWithAttributes> maybeMostDistantElement = agent.distanceCalculator.findMostDistantElement(agent.managedCluster.viewElements());
        maybeMostDistantElement.ifPresent(mostDistantElement -> {
            final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
            message.setContent(agent.codec.encode(mostDistantElement));
            log.info("Offering object {} as refinement offer", mostDistantElement.getId());
            agents.result().stream().map(DFAgentDescription::getName).forEach(message::addReceiver);

            agent.send(message);
        });
    }

    @Override
    public boolean isRefinementDone() {
        return soldObjects == refinementSize || agent.managedCluster.viewElements().isEmpty();
    }
}
