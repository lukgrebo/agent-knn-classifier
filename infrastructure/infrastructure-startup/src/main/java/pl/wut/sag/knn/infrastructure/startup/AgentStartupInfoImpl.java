package pl.wut.sag.knn.infrastructure.startup;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Accessors(fluent = true)
@Getter
public class AgentStartupInfoImpl implements AgentStartupInfo {

    private final String platformId;
    private final String containerName;
    private final String mainContainerHost;
    private final int mainContainerPort;

    @Builder
    private AgentStartupInfoImpl(final String platformId, final String containerName, final String mainContainerHost, final int mainContainerPort) {
        this.platformId = Objects.requireNonNull(platformId, "Platform id must be specified");
        this.containerName = Objects.requireNonNull(containerName, "Container name must be specified");
        this.mainContainerHost = Objects.requireNonNull(mainContainerHost, "Main container host must be specified");
        this.mainContainerPort = Objects.requireNonNull(mainContainerPort, "Main container port must be specified");
    }
}
