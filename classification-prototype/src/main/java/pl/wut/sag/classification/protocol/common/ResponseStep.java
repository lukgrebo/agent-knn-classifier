package pl.wut.sag.classification.protocol.common;

import jade.lang.acl.ACLMessage;
import lombok.Builder;
import lombok.NonNull;

public class ResponseStep<T extends Protocol, C> extends ProtocolStep<T, C> {

    @Builder(builderMethodName = "responseStepBuilder")
    protected ResponseStep(final @NonNull String stepName,
                           @NonNull final int performative,
                           final @NonNull Class<C> messageClass,
                           @NonNull final T protocol) {
        super(stepName, performative, messageClass, protocol);
    }

    public ACLMessage toResponse(final ACLMessage originalRequest, final String content) {
        final ACLMessage reply = originalRequest.createReply();
        reply.setContent(content);
        reply.setPerformative(getPerformative());
        reply.setOntology(getMessageClass().getCanonicalName());

        return reply;
    }
}
