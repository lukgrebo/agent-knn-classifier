package pl.wut.sag.knn.agent.data.config;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class DataAgentConfiguration {

    @Getter
    private final String objectMarketOntology = "object-market";

    @Getter
    private final String clusteringServiceName = "clustering-service";
}