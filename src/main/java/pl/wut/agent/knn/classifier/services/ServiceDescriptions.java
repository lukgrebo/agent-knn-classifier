package pl.wut.agent.knn.classifier.services;

import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ServiceDescriptions {
    public static final ServiceDescription CLUSTERING_SERVICE = prepareClusteringServiceDescription();

    private static ServiceDescription prepareClusteringServiceDescription() {
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName("ClusteringService");
        serviceDescription.setType("Knn");
        return serviceDescription;
    }
}
