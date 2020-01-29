package pl.wut.sag.classification.agent.classification.knn;

import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.agent.classification.distance.DistanceCalculator;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultKNearestNeightbours implements KNearestNeightbours {

    private final DistanceCalculator distanceCalculator;

    @Override
    public Map<ObjectWithAttributes, String> runAndGetVotes(final Set<ObjectWithAttributes> cluster, final ObjectWithAttributes candidate, final int k) {
        final Set<ObjectWithAttributes> nearestNeighbours = cluster.stream()
                .sorted(Comparator.comparingDouble((ObjectWithAttributes o) -> distanceCalculator.calculateDistance(o, candidate)))
                .limit(k)
                .collect(Collectors.toSet());

        return nearestNeighbours.stream().collect(Collectors.toMap(Function.identity(), o -> o.getClassname().get()));
    }

}
