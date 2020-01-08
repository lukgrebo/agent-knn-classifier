package pl.wut.sag.knn.infrastructure.message_handler;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

public interface IMessageSpecification {
    MessageTemplate getTemplateToMatch();

    Consumer<ACLMessage> getAction();
}
