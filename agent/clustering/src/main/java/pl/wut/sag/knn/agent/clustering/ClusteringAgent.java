package pl.wut.sag.knn.agent.clustering;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.algorithm.DistanceCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.collection.ImmutableList;
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

    private final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final Codec codec = Codec.json();

    @Override
    protected void setup() {
        this.addBehaviour(new MessageHandler(
                MessageSpecification.of(AuctionProtocol.proposeObject.toMessageTemplate(), this::bidRequested),
                MessageSpecification.of(AuctionProtocol.acceptBidAndSendObject.toMessageTemplate(), this::addNewObject),
                MessageSpecification.of(AuctionProtocol.requestSummary.toMessageTemplate(), this::sendSummary)
        ));
        this.registerToYellowPages();
    }

    private void sendSummary(final ACLMessage message) {
        final ClusterSummary summary = new ClusterSummary(
                managedCluster.getElements().stream().map(ObjectWithAttributes::getId).collect(Collectors.toSet()),
                distanceCalculator.calculateAverageDistaneInCluster(managedCluster.viewElements()));

        send(AuctionProtocol.summaryResponse.toResponse(message, codec.encode(summary)));
        ServiceRegistration.deregister(this);
        this.doDelete();
        this.takeDown();
    }

    private void bidRequested(final ACLMessage aclMessage) {
        final ObjectWithAttributes objectUnderClassification = codec.decode(aclMessage.getContent(), ObjectWithAttributes.class).result();

        final double averageDistance = managedCluster.getElements().stream()
                .mapToDouble(o -> distanceCalculator.calculateDistance(o, objectUnderClassification))
                .average()
                .orElse(Double.MIN_VALUE);

        final double bidValue;
        if (managedCluster.getElements().isEmpty() || averageDistance == 0) {
            bidValue = 100000;
        } else {
            bidValue = 1 / averageDistance;
        }

        final ACLMessage reply = AuctionProtocol.sendBid.toResponse(aclMessage, codec.encode(Bid.from(bidValue, objectUnderClassification.getId())));

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
                AuctionProtocol.requestSummary.getTargetService());
    }

    private void replyPositive(final ACLMessage message) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        this.send(reply);
    }

}
