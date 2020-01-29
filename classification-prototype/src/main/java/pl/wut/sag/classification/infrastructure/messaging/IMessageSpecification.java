package pl.wut.sag.classification.infrastructure.messaging;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

public interface IMessageSpecification {
    MessageTemplate getTemplateToMatch();

    void processMessage(final ACLMessage message);

    default Consumer<ACLMessage> getAction() {
        return message -> processMessage(message);
    }
}
