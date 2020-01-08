package pl.wut.sag.knn.infrastructure.message_handler;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MessageHandler extends CyclicBehaviour {

    private final List<IMessageSpecification> specifications;

    public MessageHandler(final IMessageSpecification... specifications) {
        this.specifications = new ArrayList<>(Arrays.asList(specifications));
    }

    public void add(final IMessageSpecification messageSpecification) {
        specifications.add(messageSpecification);
    }

    public void remove(final IMessageSpecification specification) {
        specifications.remove(specification);
    }

    @Override
    public void action() {
        final ACLMessage message = myAgent.receive();
        if (message != null) {
            specifications.stream()
                    .filter(s -> s.getTemplateToMatch().match(message))
                    .map(IMessageSpecification::getAction)
                    .forEach(action -> action.accept(message));
        } else {
//            log.debug("No message received");
            block();
        }
    }
}
