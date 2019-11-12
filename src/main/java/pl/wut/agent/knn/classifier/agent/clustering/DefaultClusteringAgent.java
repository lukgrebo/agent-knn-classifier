package pl.wut.agent.knn.classifier.agent.clustering;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.agent.knn.classifier.agent.ClusteringAgent;
import pl.wut.agent.knn.classifier.algorithm.DistanceCalculator;
import pl.wut.agent.knn.classifier.algorithm.EuclideanDistanceCalculator;
import pl.wut.agent.knn.classifier.definitions.Ontologies;
import pl.wut.agent.knn.classifier.definitions.bidding.Bid;
import pl.wut.agent.knn.classifier.definitions.classification.Cluster;
import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;
import pl.wut.agent.knn.classifier.infrastructure.parser.DoubleParser;
import pl.wut.agent.knn.classifier.services.ServiceDescriptions;

import java.util.UUID;

@Slf4j
public class DefaultClusteringAgent extends Agent implements ClusteringAgent {

    private final Cluster managedCluster = Cluster.emptyWithClass(UUID.randomUUID().toString());
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());

    @Override
    protected void setup() {
        this.addBehaviour(new Bidder(this));
        this.registerToYellowPages();
    }

    @Override
    public Bid bidFor(final ObjectWithAttributes objectUnderClassification) {
        final double averageDistance = managedCluster.getObjectWithAttributes().stream()
                .mapToDouble(o -> distanceCalculator.calculateDistance(o, objectUnderClassification))
                .average()
                .orElse(Double.MAX_VALUE);
        final double bidValue = 1 / averageDistance;

        return Bid.from(getAID(), bidValue, objectUnderClassification.getId());
    }

    @Override
    public void addToCluster(final ObjectWithAttributes classifiedObject) {
        managedCluster.getObjectWithAttributes().add(classifiedObject);
    }

    private void registerToYellowPages() {
        final DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        dfAgentDescription.addServices(ServiceDescriptions.CLUSTERING_SERVICE);
        dfAgentDescription.addOntologies(Ontologies.OBJECT_MARKET);
        try {
            DFService.register(this, dfAgentDescription);
        } catch (final FIPAException e) {
            log.error("Could not initialize agent: " + getAID() + " shutting down", e);
            doDelete();
        }
    }

}
