package pl.wut.agent.knn.classifier.algorithm;

import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;

public interface DistanceCalculator {
    double calculateDistance(final ObjectWithAttributes one, final ObjectWithAttributes other);
}