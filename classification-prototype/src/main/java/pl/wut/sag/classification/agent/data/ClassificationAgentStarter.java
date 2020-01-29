package pl.wut.sag.classification.agent.data;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.agent.classification.ClassificationAgent;
import pl.wut.sag.classification.agent.classification.ClassificationAgentDependencies;
import pl.wut.sag.classification.infrastructure.function.Result;
import pl.wut.sag.classification.infrastructure.messaging.ServiceDiscovery;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupManager;
import pl.wut.sag.classification.protocol.classy.ClassificationProtocol;

import java.util.List;

@RequiredArgsConstructor
class ClassificationAgentStarter {
    private final AgentStartupManager startupManager = new AgentStartupManager();
    private final ServiceDiscovery serviceDiscovery;

    public AID run(final ClassificationAgentDependencies dependencies, final AgentContainer container) {
        try {
            startupManager.startAgent(container, ClassificationAgent.class, "class-agent-" + dependencies.getClassName(), dependencies);
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }

        Result<List<DFAgentDescription>, FIPAException> search;
        do {
            sleep();
            search = serviceDiscovery.findServices(ClassificationProtocol.classificationAgentOfClassName(dependencies.getClassName()));
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
