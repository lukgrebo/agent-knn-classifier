package pl.wut.sag.classification.agent.user;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.agent.data.DataAgent;
import pl.wut.sag.classification.infrastructure.function.Result;
import pl.wut.sag.classification.infrastructure.messaging.ServiceDiscovery;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupInfo;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupManager;
import pl.wut.sag.classification.protocol.classy.ClassificationProtocol;

import java.util.List;

@RequiredArgsConstructor
class DataAgentStarter {
    private final AgentStartupManager startupManager = new AgentStartupManager();
    private final ServiceDiscovery serviceDiscovery;

    public AID run(final String context) {
        final AgentContainer container = startupManager.startChildContainer(AgentStartupInfo.withDefaults("data-container-" + context));
        try {
            startupManager.startAgent(container, DataAgent.class, "data-agent-" + context, context);
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }

        Result<List<DFAgentDescription>, FIPAException> search;
        do {
            sleep();
            search = serviceDiscovery.findServices(ClassificationProtocol.dataAgentOfContext(context));
        } while (search.isError() || search.result().isEmpty());

        return search.result().get(0).getName();
    }

    private void sleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
