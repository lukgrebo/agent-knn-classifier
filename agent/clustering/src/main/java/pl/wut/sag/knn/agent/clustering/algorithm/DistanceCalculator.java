package pl.wut.sag.knn.agent.clustering.algorithm;


import pl.wut.sag.knn.infrastructure.collection.ImmutableList;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.Comparator;

public interface DistanceCalculator {
    double calculateDistance(final ObjectWithAttributes one, final ObjectWithAttributes other);

    default double calculateAverageDistance(final ImmutableList<ObjectWithAttributes> elements, final ObjectWithAttributes element) {
        return elements.stream()
                .filter(el -> !(el == element))
                .mapToDouble(el -> calculateDistance(element, el)).average().getAsDouble();
    }

    default double calculateAverageDistaneInCluster(final ImmutableList<ObjectWithAttributes> elements) {
        return elements.stream()
                .mapToDouble(e -> calculateAverageDistance(elements, e)).average().getAsDouble();
    }

    default ObjectWithAttributes findMostDistantElement(final ImmutableList<ObjectWithAttributes> elements) {
        return elements.stream().max(Comparator.comparingDouble(e -> calculateAverageDistance(elements, e))).get();
    }
}