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
    private Map<String, Double> distanceByClassName;
    private String className;

    public static ClassificationResult of(final ObjectWithAttributes object, final Map<String, Double> distanceByClassName) {
        final Optional<Map.Entry<String, Double>> best = distanceByClassName.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue));
        return new ClassificationResult(object, distanceByClassName, best.map(Map.Entry::getKey).orElse(""));
    }

    private ClassificationResult(final ObjectWithAttributes object, final Map<String, Double> distanceByClassName, final String className) {
        this.object = object;
        this.distanceByClassName = distanceByClassName;
        this.className = className;
    }
}
