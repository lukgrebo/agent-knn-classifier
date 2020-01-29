package pl.wut.sag.classification.infrastructure.startup;

public interface AgentStartupInfo {
    String platformId();

    String containerName();

    String mainContainerHost();

    int mainContainerPort();

    static AgentStartupInfo withDefaults(final String containerName) {
        return new AgentStartupInfo() {
            @Override
            public String platformId() {
                return "sag-knn";
            }

            @Override
            public String containerName() {
                return containerName;
            }

            @Override
            public String mainContainerHost() {
                return "localhost";
            }

            @Override
            public int mainContainerPort() {
                return 1099;
            }
        };
    }
}
