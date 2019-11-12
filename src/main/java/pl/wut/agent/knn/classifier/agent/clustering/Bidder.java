package pl.wut.agent.knn.classifier.agent.clustering;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.agent.knn.classifier.agent.ClusteringAgent;
import pl.wut.agent.knn.classifier.definitions.Ontologies;
import pl.wut.agent.knn.classifier.definitions.bidding.Bid;
import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;
import pl.wut.agent.knn.classifier.infrastructure.codec.Codec;
import pl.wut.agent.knn.classifier.infrastructure.codec.CodecFactory;
import pl.wut.agent.knn.classifier.infrastructure.codec.DecodingError;
import pl.wut.agent.knn.classifier.infrastructure.function.Either;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class Bidder extends CyclicBehaviour {

    private final ClusteringAgent agent;
    private final CodecFactory codecFactory = CodecFactory.instance();

    @Override
    public void action() {
        final ACLMessage received = this.myAgent.receive();
        if (received != null && received.getPerformative() == ACLMessage.CFP && received.getOntology().equals(Ontologies.OBJECT_MARKET)) {
            handleProposal(received);
        } else {
            block();
        }
    }

    private void handleProposal(final ACLMessage received) {
        log.info("Received data object proposal");
        final Optional<Codec> decoderOpt = codecFactory.forKodec(received.getLanguage());
        if (decoderOpt.isPresent()) {
            final Either<ObjectWithAttributes, DecodingError> decodingResult = decoderOpt.get().decode(received.getContent(), ObjectWithAttributes.class);
            if (decodingResult.isRight()) {
                log.error("Could not decode incoming object " + decodingResult.getRight().getMessage(), decodingResult.getRight().getCause());
            }
            final Bid bid = agent.bidFor(decodingResult.getLeft());
            reply(received, bid, decoderOpt.get());
        } else {
            log.error("Codec not found for language: " + received.getLanguage());
        }
    }

    private void reply(final ACLMessage received, final Bid bid, final Codec codec) {
        final ACLMessage reply = received.createReply();
        final String replyString = codec.encode(bid);
        reply.setContent(replyString);
        reply.setPerformative(ACLMessage.PROPOSE);
        myAgent.send(reply);
    }

}
