package pl.wut.sag.knn.agent.clustering.algorithm;


import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

public interface DistanceCalculator {
    double calculateDistance(final ObjectWithAttributes one, final ObjectWithAttributes other);
}