package pl.wut.sag.knn.infrastructure.message_handler;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class MessageSpecification {
    private final MessageTemplate templateToMatch;
    private final Consumer<ACLMessage> action;
}
