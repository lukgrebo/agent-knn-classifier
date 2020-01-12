package pl.wut.sag.knn.agent.clustering.algorithm;

import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.parser.DoubleParser;
import pl.wut.sag.knn.infrastructure.parser.ParseError;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.Optional;

@RequiredArgsConstructor
public class EuclideanDistanceCalculator implements DistanceCalculator {

    private final DoubleParser doubleParser;

    @Override
    public double calculateDistance(final ObjectWithAttributes alreadyClassified, final ObjectWithAttributes other) {
        return alreadyClassified.indexedAttributesSkipDiscriminator()
                .map(o -> singleDimenstionalDistance(o.getValue(), other.getAsString(o.getIndex())))
                .map(d -> Math.pow(d, 2))
                .reduce(Double::sum)
                .map(Math::sqrt)
                .get();
    }

    private double singleDimenstionalDistance(final String value, final Optional<String> other) {
        if (!other.isPresent()) {
            return 0.5D;
        }

        final String otherValue = other.get();
        final Result<Double, ParseError> parseFirst = doubleParser.parse(value);
        final Result<Double, ParseError> parseOther = doubleParser.parse(otherValue);

        if (parseFirst.isValid() && parseOther.isValid()) {
            return parseFirst.result() - parseOther.result();
        } else {
            return value.equals(otherValue) ? 0d : 1d;
        }

    }


}
