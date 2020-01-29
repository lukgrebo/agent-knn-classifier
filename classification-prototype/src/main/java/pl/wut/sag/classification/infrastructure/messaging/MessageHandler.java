package pl.wut.sag.classification.infrastructure.messaging;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MessageHandler extends CyclicBehaviour {

    private final List<IMessageSpecification> specifications;
    private final List<IMessageSpecification> toAdd = new ArrayList<>();
    private final List<IMessageSpecification> toRemove = new ArrayList<>();

    public MessageHandler(final IMessageSpecification... specifications) {
        this.specifications = new ArrayList<>(Arrays.asList(specifications));
    }

    public void add(final IMessageSpecification messageSpecification) {
        toAdd.add(messageSpecification);
    }

    public void remove(final IMessageSpecification specification) {
        toRemove.add(specification);
    }

    @Override
    public void action() {
        specifications.addAll(toAdd);
        toAdd.clear();
        specifications.removeAll(toRemove);
        toRemove.clear();

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
