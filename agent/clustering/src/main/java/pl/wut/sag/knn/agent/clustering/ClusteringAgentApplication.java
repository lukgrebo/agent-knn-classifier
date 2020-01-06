package pl.wut.sag.knn.agent.clustering;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupInfoImpl;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupManager;

@Slf4j
public class ClusteringAgentApplication {

    public static void main(final String[] args) throws StaleProxyException {
        final AgentStartupManager manager = new AgentStartupManager();
        final AgentStartupInfoImpl startupInfo = AgentStartupInfoImpl.builder()
                .platformId("sag-knn")
                .mainContainerPort(1099)
                .mainContainerHost("localhost")
                .containerName("cluster-container")
                .build();

        final AgentContainer container = manager.startChildContainer(startupInfo);
        final AgentController agentController = manager.startAgent(container, ClusteringAgent.class, "clustering-agent", new Object());

        log.info("Starting clustering agent");

    }
}
