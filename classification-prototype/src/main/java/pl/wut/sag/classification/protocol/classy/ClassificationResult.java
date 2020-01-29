package pl.wut.sag.classification.protocol.classy;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ClassificationResult {
    private ObjectWithAttributes object;
    private Map<String, DistanceInfo> distanceByClassName;
    private String className;

    public static ClassificationResult of(final ObjectWithAttributes object, final Map<String, DistanceInfo> distanceByClassName) {
        final Optional<String> best = distanceByClassName.entrySet().stream()
                .min(Comparator.comparingDouble(e -> e.getValue().getAveragePositiveDistance()))
                .map(Map.Entry::getKey);
        return new ClassificationResult(object, distanceByClassName, best.orElse(""));
    }

    private ClassificationResult(final ObjectWithAttributes object, final Map<String, DistanceInfo> distanceByClassName, final String className) {
        this.object = object;
        this.distanceByClassName = distanceByClassName;
        this.className = className;
    }
}
