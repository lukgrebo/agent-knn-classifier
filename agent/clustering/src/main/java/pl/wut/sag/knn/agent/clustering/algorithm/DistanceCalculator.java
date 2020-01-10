package pl.wut.sag.knn.agent.clustering.algorithm;


import pl.wut.sag.knn.infrastructure.collection.ImmutableList;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

public interface DistanceCalculator {
    double calculateDistance(final ObjectWithAttributes one, final ObjectWithAttributes other);

    default double calculateAverageDistaneInCluster(ImmutableList<ObjectWithAttributes> elements) {
        return elements.stream()
                .mapToDouble(e -> elements.stream()
                        .filter(el -> !(el == e))
                        .mapToDouble(el -> calculateDistance(e, el)).average().getAsDouble()
                ).average().getAsDouble();

    }
}