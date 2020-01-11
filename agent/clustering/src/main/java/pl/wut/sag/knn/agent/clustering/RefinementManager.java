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
import pl.wut.sag.knn.ontology.auction.RefinementSummary;
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

    private final ClusteringAgent agent;
    private final Map<UUID, Set<OwnedBid>> bids = new HashMap<>();
    private static final int refinementSize = 3;
    private int expectedBidderCount;
    private int soldObjects;
    private ACLMessage refinementStartMessage;

    @Override
    public void startRefinement(final ACLMessage incomingMessage) {
        log.info("{} Got request to start refinement!", agent.getName());
        this.refinementStartMessage = incomingMessage;
        offerMostDistantElementToOtherAgents();
    }

    @Override
    public void handleBid(final ACLMessage message) {
        final Bid bid = agent.codec.decode(message.getContent(), AuctionProtocol.sendBid.getMessageClass()).result();
        log.info("Got refinement bid for object {}", bid.getObjectUuid());
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
                refiningMessage.addReceiver(message.getSender());
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
        final RefinementSummary refinementSummary = new RefinementSummary();
        final ACLMessage messageToDataAgent = AuctionProtocol.refinementFinishedResponse.toResponse(refinementStartMessage, agent.codec.encode(refinementSummary));

        agent.send(messageToDataAgent);
    }

    private void offerMostDistantElementToOtherAgents() {
        final Result<List<DFAgentDescription>, FIPAException> agents = agent.serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
        this.expectedBidderCount = agents.result().size();
        final ObjectWithAttributes mostDistantElement = agent.distanceCalculator.findMostDistantElement(agent.managedCluster.viewElements());

        final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
        message.setContent(agent.codec.encode(mostDistantElement));
        agents.result().stream().map(DFAgentDescription::getName).forEach(message::addReceiver);

        agent.send(message);
    }

    @Override
    public boolean isRefinementDone() {
        return soldObjects == refinementSize || agent.managedCluster.viewElements().isEmpty();
    }
}
