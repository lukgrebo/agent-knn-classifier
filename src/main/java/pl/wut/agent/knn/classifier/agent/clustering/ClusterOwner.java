package pl.wut.agent.knn.classifier.agent.clustering;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.agent.knn.classifier.agent.ClusteringAgent;
import pl.wut.agent.knn.classifier.definitions.Ontologies;
import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;
import pl.wut.agent.knn.classifier.infrastructure.codec.Codec;
import pl.wut.agent.knn.classifier.infrastructure.codec.CodecFactory;
import pl.wut.agent.knn.classifier.infrastructure.codec.DecodingError;
import pl.wut.agent.knn.classifier.infrastructure.function.Either;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ClusterOwner extends CyclicBehaviour {

    private final ClusteringAgent agent;
    private final CodecFactory codecFactory = CodecFactory.instance();

    @Override
    public void action() {
        final ACLMessage received = myAgent.receive();
        if (received != null && received.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && received.getOntology().equals(Ontologies.OBJECT_MARKET)) {
            addNewObject(received);
        } else {
            block();
        }
    }

    private void addNewObject(final ACLMessage received) {
        final Optional<Codec> codecOpt = codecFactory.forKodec(received.getLanguage());
        if (codecOpt.isPresent()) {
            final Codec codec = codecOpt.get();
            final Either<ObjectWithAttributes, DecodingError> decoded = codec.decode(received.getContent(), ObjectWithAttributes.class);
            if (decoded.isLeft()) {
                final ObjectWithAttributes objectWithAttributes = decoded.getLeft();
                agent.addToCluster(objectWithAttributes);
                replyPositive(received);
            } else {
                final String error = "Could not decode " + received.getContent() + ": " + decoded.getRight().getMessage();
                log.error(error, decoded.getRight().getCause());
                replyError(received, error);
            }
        } else {
            final String error = "Codec not found for language: "  + received.getLanguage();
            log.error(error);
            replyError(received, error);
        }
    }

    private void replyPositive(final ACLMessage message) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.AGREE); //TODO, is it the right performative?
        myAgent.send(reply);
    }

    private void replyError(final ACLMessage message, final String error) {
        final ACLMessage reply = message.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(error);
        myAgent.send(reply);
    }

}
