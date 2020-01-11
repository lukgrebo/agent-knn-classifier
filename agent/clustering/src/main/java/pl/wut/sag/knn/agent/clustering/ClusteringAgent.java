package pl.wut.sag.knn.agent.clustering;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.algorithm.BidCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.DistanceCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.DoubleParser;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ClusteringAgent extends Agent {

    final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    final Codec codec = Codec.json();
    final BidCalculator bidCalculator = BidCalculator.calculator(distanceCalculator);
    final ServiceDiscovery serviceDiscovery = new ServiceDiscovery(this);
    private final RefinementManager refinementManager = RefinementManager.newManager(this);

    @Override
    protected void setup() {
        this.addBehaviour(new MessageHandler(
                MessageSpecification.of(AuctionProtocol.proposeObject.toMessageTemplate(), this::bidRequested),
                MessageSpecification.of(AuctionProtocol.sendBid.toMessageTemplate(), refinementManager::handleBid),
                MessageSpecification.of(AuctionProtocol.acceptBidAndSendObject.toMessageTemplate(), this::addNewObject),
                MessageSpecification.of(AuctionProtocol.requestSummary.toMessageTemplate(), this::sendSummary),
                MessageSpecification.of(AuctionProtocol.startRefinementRequest.toMessageTemplate(), refinementManager::startRefinement)
        ));
        this.registerToYellowPages();
    }

    private void sendSummary(final ACLMessage message) {
        log.info("{} Got summary request", getName());
        final ClusterSummary summary = new ClusterSummary(
                managedCluster.getElements().stream().map(ObjectWithAttributes::getId).collect(Collectors.toSet()), distanceCalculator.calculateAverageDistaneInCluster(managedCluster.viewElements()));
        send(AuctionProtocol.summaryResponse.toResponse(message, codec.encode(summary)));

        if (refinementManager.isRefinementDone()) {
            ServiceRegistration.deregister(this);
            this.takeDown();
            this.doDelete();
        }
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
