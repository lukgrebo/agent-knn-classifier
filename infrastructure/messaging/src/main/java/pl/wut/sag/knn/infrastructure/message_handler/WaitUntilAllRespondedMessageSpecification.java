package pl.wut.sag.knn.infrastructure.message_handler;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Collects responses from group of agents.
 */
public class WaitUntilAllRespondedMessageSpecification<R> implements IMessageSpecification {

    private WaitUntilAllRespondedMessageSpecification(@NonNull final Set<AID> agentsToWaitFor, @NonNull final MessageTemplate messageTemplate, @NonNull final Function<ACLMessage, R> messageMapper, final Consumer<WaitUntilAllRespondedMessageSpecification<R>> doWithSpecification, final Consumer<Map<AID, R>> doWithResponses, final Duration timeout) {
        this.agentsToWaitFor = agentsToWaitFor;
        this.messageTemplate = messageTemplate;
        this.messageMapper = messageMapper;
        this.doWithSpecification = doWithSpecification;
        this.doWithResponses = doWithResponses;
        this.timeout = timeout;
    }

    private final Instant creation = Instant.now();

    @NonNull
    private final Set<AID> agentsToWaitFor;

    @NonNull
    private final MessageTemplate messageTemplate;

    @NonNull
    /**
     * Maps incoming messages to desired processable output
     */
    private final Function<ACLMessage, R> messageMapper;

    private final Map<AID, R> responses = new HashMap<>();

    /**
     * Action to perform on this specification after all agents responded or timeout has elapsed.
     */
    private final Consumer<WaitUntilAllRespondedMessageSpecification<R>> doWithSpecification;

    /**
     * Action to perform on responsed after all agents responded or timeout has elapsed.
     */
    private final Consumer<Map<AID, R>> doWithResponses;

    /**
     * Nullable - if specified waiting will be timed out
     */
    private final Duration timeout;


    @Override
    public MessageTemplate getTemplateToMatch() {
        return messageTemplate;
    }

    @Override
    public void processMessage(final ACLMessage message) {
        final AID sender = message.getSender();
        final R r = messageMapper.apply(message);

        responses.put(sender, r);
        if (doWithSpecification != null) {
            doWithSpecification.accept(this);
        }
        if (doWithResponses != null) {
            doWithResponses.accept(responses);
        }
    }

    private boolean isFinished() {
        if (timeout != null && Instant.now().isAfter(creation.plus(timeout))) {
            return true;
        }

        return responses.keySet().containsAll(agentsToWaitFor);
    }

    public static WaitUntilAllRespondedMessageSpecification<ACLMessage> simpleWait(final Set<AID> agentsToWaitFor,
                                                                                   final MessageTemplate messageTemplate) {
        return new WaitUntilAllRespondedMessageSpecification<>(agentsToWaitFor, messageTemplate, Function.identity(), null, null, null);
    }

    /**
     * Automatically registers and deregisters specification from message handle
     *
     * @param doAfterFinishAndDeregister - action to perform on responses after waiting on agents is finished.
     * @param messageHandler             - message handler to who specification will be registered and from whom will be deregistered.
     * @param <R>                        - type of response processed.
     */
    public static <R> WaitUntilAllRespondedMessageSpecification<R> complexWithRegisterAndDeregister(final Set<AID> agentsToWaitFor,
                                                                                                    final MessageTemplate messageTemplate,
                                                                                                    final MessageHandler messageHandler,
                                                                                                    final Function<ACLMessage, R> mapper,
                                                                                                    final Consumer<Map<AID, R>> doAfterFinishAndDeregister) {
        return new WaitUntilAllRespondedMessageSpecification<>(agentsToWaitFor, messageTemplate, mapper, messageHandler::remove, doAfterFinishAndDeregister, null);
    }

}
