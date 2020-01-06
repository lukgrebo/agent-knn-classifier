package pl.wut.sag.knn.agent.clustering;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.algorithm.DistanceCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.DoubleParser;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.UUID;

@Slf4j
public class DefaultClusteringAgent extends Agent {

    private final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final Codec codec = Codec.json();

    @Override
    protected void setup() {
        this.addBehaviour(new MessageHandler(
                MessageSpecification.of(AuctionProtocol.proposeObject.toMessageTemplate(), this::bidRequested),
                MessageSpecification.of(AuctionProtocol.acceptBidAndSendObject.toMessageTemplate(), this::addNewObject)
        ));
        this.registerToYellowPages();
    }

    private void bidRequested(final ACLMessage aclMessage) {
        final ObjectWithAttributes objectUnderClassification = codec.decode(aclMessage.getContent(), ObjectWithAttributes.class).result();

        final double averageDistance = managedCluster.getElements().stream()
                .mapToDouble(o -> distanceCalculator.calculateDistance(o, objectUnderClassification))
                .average()
                .orElse(Double.MAX_VALUE);

        final double bidValue = 1 / averageDistance;

        final ACLMessage reply = AuctionProtocol.sendBid.toResponse(aclMessage, codec.encode(Bid.from(bidValue, objectUnderClassification.getId())));

        this.send(reply);
    }

    private void addNewObject(final ACLMessage received) {
        final ObjectWithAttributes decoded = codec.decode(received.getContent(), ObjectWithAttributes.class).result();
        managedCluster.getElements().add(decoded);
        replyPositive(received);
    }

    private void registerToYellowPages() {

    }

    private void replyPositive(final ACLMessage message) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        this.send(reply);
    }

}
