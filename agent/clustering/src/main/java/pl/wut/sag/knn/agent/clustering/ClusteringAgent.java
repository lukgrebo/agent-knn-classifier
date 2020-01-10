package pl.wut.sag.knn.agent.clustering;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.algorithm.BidCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.DistanceCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.DoubleParser;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ClusteringAgent extends Agent {

    private final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final Codec codec = Codec.json();
    private final BidCalculator bidCalculator = BidCalculator.calculator(distanceCalculator);
    private final ServiceDiscovery serviceDiscovery = new ServiceDiscovery(this);
    boolean refinement;

    @Override
    protected void setup() {
        this.addBehaviour(new MessageHandler(
                MessageSpecification.of(AuctionProtocol.proposeObject.toMessageTemplate(), this::bidRequested),
                MessageSpecification.of(AuctionProtocol.sendBid.toMessageTemplate(), this::handleBid),
                MessageSpecification.of(AuctionProtocol.acceptBidAndSendObject.toMessageTemplate(), this::addNewObject),
                MessageSpecification.of(AuctionProtocol.requestSummary.toMessageTemplate(), this::sendSummary),
                MessageSpecification.of(AuctionProtocol.startRefinementRequest.toMessageTemplate(), this::startRefinement)
        ));
        this.registerToYellowPages();
    }

    private void handleBid(final ACLMessage message) {
        final Bid bid = codec.decode(message.getContent(), AuctionProtocol.sendBid.getMessageClass()).result();
        final Optional<ObjectWithAttributes> maybeElement = managedCluster.viewElements().stream().filter(e -> e.getId().equals(bid.getObjectUuid())).findFirst();
        if (maybeElement.isPresent()) {
            final ObjectWithAttributes element = maybeElement.get();
            final Bid myBid = bidCalculator.calculateBid(managedCluster, element);
            if (bid.getValue() >= myBid.getValue()) {
                managedCluster.getElements().remove(element);
                final ACLMessage refiningMessage = AuctionProtocol.acceptBidAndSendObject.templatedMessage();
                refiningMessage.setContent(codec.encode(element));
                refiningMessage.addReceiver(message.getSender());
                send(refiningMessage);
            }
        }
    }

    private void startRefinement(final ACLMessage request) {
        log.info("{} Got request to start refinement!", getName());
        final Result<List<DFAgentDescription>, FIPAException> agents = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
        final ObjectWithAttributes mostDistantElement = distanceCalculator.findMostDistantElement(managedCluster.viewElements());

        refinement = true;
        final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
        message.setContent(codec.encode(mostDistantElement));
        agents.result().stream().map(DFAgentDescription::getName).forEach(message::addReceiver);

        send(message);
    }

    private void sendSummary(final ACLMessage message) {
        log.info("{} Got summary request", getName());
        final ClusterSummary summary = new ClusterSummary(
                managedCluster.getElements().stream().map(ObjectWithAttributes::getId).collect(Collectors.toSet()), distanceCalculator.calculateAverageDistaneInCluster(managedCluster.viewElements()));
        send(AuctionProtocol.summaryResponse.toResponse(message, codec.encode(summary)));
//        ServiceRegistration.deregister(this);
//        this.doDelete();
//        this.takeDown();
    }

    private void bidRequested(final ACLMessage aclMessage) {
        final ObjectWithAttributes objectUnderClassification = codec.decode(aclMessage.getContent(), ObjectWithAttributes.class).result();
        final Bid bid = bidCalculator.calculateBid(managedCluster, objectUnderClassification);
        final ACLMessage reply = AuctionProtocol.sendBid.toResponse(aclMessage, codec.encode(bid));

        this.send(reply);
    }

    private void addNewObject(final ACLMessage received) {
        final ObjectWithAttributes decoded = codec.decode(received.getContent(), ObjectWithAttributes.class).result();
        managedCluster.getElements().add(decoded);
        replyPositive(received);
    }

    private void registerToYellowPages() {
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5),
                AuctionProtocol.proposeObject.getTargetService(),
                AuctionProtocol.requestSummary.getTargetService(),
                AuctionProtocol.startRefinementRequest.getTargetService());
    }

    private void replyPositive(final ACLMessage message) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        this.send(reply);
    }

}
