package pl.wut.sag.knn.agent.data;

import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.agent.clustering.ClusteringAgent;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupInfoImpl;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupManager;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ClusteringAgentRunner {

    private final AgentStartupManager manager;
    private final AgentContainer container;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static ClusteringAgentRunner initializeClusteringAgentsContainerAndGetRunner() {
        final AgentStartupManager manager = new AgentStartupManager();
        final AgentContainer childContainer = manager.startChildContainer(AgentStartupInfoImpl.builder()
                .mainContainerHost("localhost")
                .mainContainerPort(1099)
                .platformId("sag-knn")
                .containerName("clusters-container")
                .build());

        return new ClusteringAgentRunner(manager, childContainer);
    }

    public Result<Void, StaleProxyException> runClusteringAgent() {
        try {
            manager.startAgent(container, ClusteringAgent.class, "clustering" + counter.incrementAndGet(), new Object());
            return Result.empty();
        } catch (final StaleProxyException e) {
            return Result.error(e);
        }
    }
}
