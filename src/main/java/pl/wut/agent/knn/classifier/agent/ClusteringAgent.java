package pl.wut.agent.knn.classifier.agent;

import pl.wut.agent.knn.classifier.definitions.bidding.Bid;
import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;

/**
 * Responsible for clustering.
 * Owner of cluster. Classifies objects to it's cluster.
 */
public interface ClusteringAgent {
    /** Calculates bid value for given object */
    Bid bidFor(ObjectWithAttributes objectUnderClassification);
    void addToCluster(ObjectWithAttributes classifiedObject);
}
