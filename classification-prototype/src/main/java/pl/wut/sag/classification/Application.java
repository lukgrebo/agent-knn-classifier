package pl.wut.sag.classification;

import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import pl.wut.sag.classification.agent.user.UserAgent;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupManager;

public class Application {

    public static void main(final String[] args) throws StaleProxyException {
        final AgentStartupManager startupManager = new AgentStartupManager();
        final AgentContainer agentContainer = startupManager.startMainContainer("sag-knn", true);
        startupManager.startAgent(agentContainer, UserAgent.class, "user-agent", new Object[]{});
    }
}
