package pl.wut.sag.classification.agent.classification.distance;

import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.function.Result;
import pl.wut.sag.classification.infrastructure.parser.DoubleParser;
import pl.wut.sag.classification.infrastructure.parser.ParseError;

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
                .orElse(0D);

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
