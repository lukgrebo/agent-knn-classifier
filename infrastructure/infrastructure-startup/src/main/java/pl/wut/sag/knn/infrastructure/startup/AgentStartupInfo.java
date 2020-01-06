package pl.wut.sag.knn.infrastructure.startup;

public interface AgentStartupInfo {
    String platformId();

    String containerName();

    String mainContainerHost();

    int mainContainerPort();
}
