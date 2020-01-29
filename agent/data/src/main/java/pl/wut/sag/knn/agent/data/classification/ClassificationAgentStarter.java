package pl.wut.sag.knn.agent.data.classification;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.agent.classification.ClassificationAgent;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupInfo;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupManager;
import pl.wut.sag.knn.protocol.classy.ClassificationProtocol;

import java.util.List;

@RequiredArgsConstructor
public class ClassificationAgentStarter {

    private final ServiceDiscovery serviceDiscovery;
    private final AgentStartupManager agentStartupManager = new AgentStartupManager();

    public AID start(String className) {
        final AgentContainer container = agentStartupManager.startChildContainer(AgentStartupInfo.withDefaults("class_container-" + className));
        try {
            agentStartupManager.startAgent(container, ClassificationAgent.class, "classification-agent-" + className, new Object[]{className});
        } catch (StaleProxyException e) {
            throw new RuntimeException();
        }
        Result<List<DFAgentDescription>, FIPAException> result;
        do {
            result = serviceDiscovery.findServices(ClassificationProtocol.agentOfClassName(className));
        } while (result.isError() || result.result().isEmpty());

        return result.result().get(0).getName();
    }
}
