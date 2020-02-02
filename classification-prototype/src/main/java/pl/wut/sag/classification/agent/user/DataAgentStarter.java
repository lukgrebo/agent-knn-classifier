package pl.wut.sag.classification.agent.user;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.agent.data.DataAgent;
import pl.wut.sag.classification.agent.data.DataAgentDependencies;
import pl.wut.sag.classification.infrastructure.codec.Codec;
import pl.wut.sag.classification.infrastructure.messaging.IMessageSpecification;
import pl.wut.sag.classification.infrastructure.messaging.MessageHandler;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupInfo;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupManager;
import pl.wut.sag.classification.protocol.up.ImUpProtocol;

import java.util.function.Consumer;

@RequiredArgsConstructor
class DataAgentStarter {
    private final AgentStartupManager startupManager = new AgentStartupManager();
    private final Codec codec = Codec.json();

    public void run(final String context, final AID userAgent, final MessageHandler messageHandler, final Consumer<AID> doWithResponse) {
        final AgentContainer container = startupManager.startChildContainer(AgentStartupInfo.withDefaults("data-container-" + context));
        try {
            final DataAgentDependencies dependencies = new DataAgentDependencies(context, userAgent);
            startupManager.startAgent(container, DataAgent.class, "data-agent-" + context, dependencies);
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
        messageHandler.add(new IMessageSpecification() {
            @Override
            public MessageTemplate getTemplateToMatch() {
                return ImUpProtocol.imUp.toMessageTemplate();
            }

            @Override
            public void processMessage(final ACLMessage message) {
                doWithResponse.accept(message.getSender());
                messageHandler.remove(this);
            }
        });

    }


}
