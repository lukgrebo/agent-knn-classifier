package pl.wut.sag.knn.protocol;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
public class ProtocolStep<T extends Protocol, C> {

    private final String stepName;
    private final int performative;
    private final boolean required;
    private final Class<C> messageClass;
    private final T protocol;

    @Builder
    protected ProtocolStep(@NonNull final String stepName,
                           @NonNull final int performative,
                           @NonNull final boolean required,
                           @NonNull final Class<C> messageClass,
                           @NonNull final T protocol) {
        this.stepName = Objects.requireNonNull(stepName);
        this.performative = performative;
        this.required = required;
        this.messageClass = messageClass;
        this.protocol = protocol;
    }

    public MessageTemplate toMessageTemplate() {
        return Stream.of(
                MessageTemplate.MatchPerformative(performative),
                MessageTemplate.MatchOntology(messageClass.getCanonicalName()),
                MessageTemplate.MatchProtocol(protocol.getName())
        )
                .reduce(MessageTemplate.MatchAll(), MessageTemplate::and);
    }

    public ACLMessage templatedMessage() {
        final ACLMessage message = new ACLMessage(this.performative);
        message.setProtocol(this.protocol.getName());
        message.setOntology(messageClass.getCanonicalName());

        return message;
    }


}
