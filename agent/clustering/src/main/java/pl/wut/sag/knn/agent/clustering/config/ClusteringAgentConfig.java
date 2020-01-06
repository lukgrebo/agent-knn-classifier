package pl.wut.sag.knn.agent.clustering.config;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ClusteringAgentConfig {
    private static final String objectMarketOntology = "object-market";
    private static final String clusteringServiceName = "clustering-service";
}
