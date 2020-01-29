package pl.wut.sag.classification.infrastructure.startup;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentStartupManager {

    public AgentContainer startChildContainer(final AgentStartupInfo agentStartupInfoImpl) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();
        profile.setParameter(Profile.PLATFORM_ID, agentStartupInfoImpl.platformId());
        profile.setParameter(Profile.CONTAINER_NAME, agentStartupInfoImpl.containerName());
        profile.setParameter(Profile.MAIN_HOST, agentStartupInfoImpl.mainContainerHost());
        profile.setParameter(Profile.MAIN_PORT, Integer.toString(agentStartupInfoImpl.mainContainerPort()));

        return runtime.createAgentContainer(profile);
    }

    public AgentContainer startMainContainer(final String platformId, final boolean gui) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();
        profile.setParameter(Profile.PLATFORM_ID, platformId);
        final String contianerName = platformId + "-main-container";
        profile.setParameter(Profile.CONTAINER_NAME, contianerName);
        profile.setParameter("gui", Boolean.toString(gui));

        log.info("Starting main container {} for platform {} ", contianerName, platformId);
        return runtime.createMainContainer(profile);
    }

    public <T> AgentController startAgent(
            final AgentContainer agentContainer, final Class<? extends Agent> agentClass,
            final String agentName, final T dependencies) throws StaleProxyException {
        log.info("Starting agent of class {} with name {}", agentClass.getCanonicalName(), agentName);
        final AgentController newAgent = agentContainer.createNewAgent(agentName, agentClass.getCanonicalName(), new Object[]{dependencies});
        newAgent.start();

        return newAgent;
    }
}
