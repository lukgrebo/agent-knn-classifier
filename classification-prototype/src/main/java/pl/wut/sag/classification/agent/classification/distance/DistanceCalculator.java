package pl.wut.sag.classification.agent.classification.distance;


import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.collection.ImmutableList;

import java.util.Comparator;
import java.util.Optional;

public interface DistanceCalculator {
    double calculateDistance(final ObjectWithAttributes one, final ObjectWithAttributes other);

    default double calculateAverageDistance(final ImmutableList<ObjectWithAttributes> elements, final ObjectWithAttributes element) {
        return elements.stream()
                .filter(el -> !(el == element))
                .mapToDouble(el -> calculateDistance(element, el)).average().orElse(0D);
    }

    default double calculateAverageDistaneInCluster(final ImmutableList<ObjectWithAttributes> elements) {
        return elements.stream()
                .mapToDouble(e -> calculateAverageDistance(elements, e)).average().orElse(0.0001);
    }

    default Optional<ObjectWithAttributes> findMostDistantElement(final ImmutableList<ObjectWithAttributes> elements) {
        return elements.stream().max(Comparator.comparingDouble(e -> calculateAverageDistance(elements, e)));
    }
}