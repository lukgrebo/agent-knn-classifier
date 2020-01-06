package pl.wut.sag.knn.agent.clustering;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.clustering.algorithm.DistanceCalculator;
import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator;
import pl.wut.sag.knn.agent.clustering.config.ClusteringAgentConfig;
import pl.wut.sag.knn.agent.clustering.config.MessageTemplates;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.codec.CodecFactory;
import pl.wut.sag.knn.infrastructure.codec.DecodingError;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.DoubleParser;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class DefaultClusteringAgent extends Agent {

    private final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final ClusteringAgentConfig config = new ClusteringAgentConfig();
    private final CodecFactory codecFactory = CodecFactory.instance();

    @Override
    protected void setup() {
        final MessageTemplates templates = new MessageTemplates(config);
        this.addBehaviour(new MessageHandler(
                MessageSpecification.of(templates.biddingOfferTemplate(), this::sendBid),
                MessageSpecification.of(templates.offerAcceptationTemplate(), this::addNewObject)
        ));
        this.registerToYellowPages();
    }

    private void sendBid(final ACLMessage aclMessage) {
        final Optional<Codec> codec = codecFactory.forKodec(aclMessage.getLanguage());
        if (!codec.isPresent()) {
            log.warn("Codec for language: {} not found", aclMessage.getLanguage());
            return;
        }
        final Result<ObjectWithAttributes, DecodingError> decodingResult = codec.get().decode(aclMessage.getContent(), ObjectWithAttributes.class);
        if (!decodingResult.isValid()) {
            log.error("Could not decode: {} to {}", aclMessage.getContent(), ObjectWithAttributes.class.getSimpleName());
            return;
        }
        final ObjectWithAttributes objectUnderClassification = decodingResult.result();

        final double averageDistance = managedCluster.getElements().stream()
                .mapToDouble(o -> distanceCalculator.calculateDistance(o, objectUnderClassification))
                .average()
                .orElse(Double.MAX_VALUE);

        final double bidValue = 1 / averageDistance;

        final ACLMessage reply = aclMessage.createReply();
        final String replyString = codec.get().encode(Bid.from(bidValue, objectUnderClassification.getId()));
        reply.setContent(replyString);
        reply.setPerformative(ACLMessage.PROPOSE);

        this.send(reply);
    }

    private void registerToYellowPages() {
        final DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName(config.clusteringServiceName());
        dfAgentDescription.addServices(serviceDescription);
        dfAgentDescription.addOntologies(config.objectMarketOntology());
        try {
            DFService.register(this, dfAgentDescription);
        } catch (final FIPAException e) {
            log.error("Could not initialize agent: " + getAID() + " shutting down", e);
            doDelete();
        }
    }

    private void addNewObject(final ACLMessage received) {
        final Optional<Codec> codecOpt = codecFactory.forKodec(received.getLanguage());
        if (codecOpt.isPresent()) {
            final Codec codec = codecOpt.get();
            final Result<ObjectWithAttributes, DecodingError> decoded = codec.decode(received.getContent(), ObjectWithAttributes.class);
            if (decoded.isValid()) {
                final ObjectWithAttributes objectWithAttributes = decoded.result();
                managedCluster.getElements().add(objectWithAttributes);
                replyPositive(received);
            } else {
                final String error = "Could not decode " + received.getContent() + ": " + decoded.error().getMessage();
                log.error(error, decoded.error().getCause());
                replyError(received, error);
            }
        } else {
            final String error = "Codec not found for language: " + received.getLanguage();
            log.error(error);
            replyError(received, error);
        }
    }

    private void replyPositive(final ACLMessage message) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        this.send(reply);
    }

    private void replyError(final ACLMessage message, final String error) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(error);
        this.send(reply);
    }

}
