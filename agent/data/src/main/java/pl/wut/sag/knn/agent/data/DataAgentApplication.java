package pl.wut.sag.knn.agent.data;

import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupInfoImpl;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupManager;

public class DataAgentApplication {

    public static void main(final String[] args) throws StaleProxyException {
        final AgentStartupManager startupManager = new AgentStartupManager();
        final AgentStartupInfoImpl info = AgentStartupInfoImpl.builder()
                .containerName("data-agent")
                .mainContainerHost("localhost")
                .platformId("sag-knn")
                .mainContainerPort(1099)
                .build();

        final AgentContainer agentContainer = startupManager.startChildContainer(info);
        startupManager.startAgent(agentContainer, DataAgent.class, "data-agent", new Object());
    }
}
