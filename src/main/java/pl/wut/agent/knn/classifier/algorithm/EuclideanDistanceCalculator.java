package pl.wut.agent.knn.classifier.algorithm;

import lombok.RequiredArgsConstructor;
import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes;
import pl.wut.agent.knn.classifier.infrastructure.function.Either;
import pl.wut.agent.knn.classifier.infrastructure.parser.DoubleParser;
import pl.wut.agent.knn.classifier.infrastructure.parser.ParseError;

import java.util.Optional;

@RequiredArgsConstructor
public class EuclideanDistanceCalculator implements DistanceCalculator {

    private final DoubleParser doubleParser;

    @Override
    public double calculateDistance(final ObjectWithAttributes alreadyClassified, final ObjectWithAttributes other) {
        return alreadyClassified.indexedAttributes().map(o -> singleDimenstionalDistance(o.getValue(), other.getAsString(o.getIndex())))
                .map(d -> Math.pow(d, 2))
                .reduce(Double::sum)
                .map(Math::sqrt)
                .get();
    }

    private double singleDimenstionalDistance(final String value, final Optional<String> other) {
        if (!other.isPresent()) {
            return 0d; //TODO, missing attribute fine function
        }

        final String otherValue = other.get();
        final Either<Double, ParseError> parseFirst = doubleParser.parse(value);
        final Either<Double, ParseError> parseOther = doubleParser.parse(otherValue);

        if (parseFirst.isLeft() && parseOther.isLeft()) {
            return parseFirst.getLeft() - parseOther.getLeft();
        } else {
            return value.equals(otherValue) ? 0d : 1d;
        }

    }


}
