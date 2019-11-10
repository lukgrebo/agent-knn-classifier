package pl.wut.agent.knn.classifier.infrastructure.parser;

import pl.wut.agent.knn.classifier.infrastructure.function.Either;

public class DoubleParser implements Parser<Double> {

    private final static String javaDecimalSeparator = ".";
    private final static String allowedDecimalSeparator = ",";

    @Override
    public Either<Double, ParseError> parse(final String input) {
        if (input == null) {
            return error(ParseError.inputIsNull());
        }

        try {
            return Either.left(Double.parseDouble(input.replaceAll(allowedDecimalSeparator, javaDecimalSeparator)));
        } catch (final RuntimeException e) {
            return error(ParseError.ofCause(e));
        }
    }

    private Either<Double, ParseError> error(final ParseError error) {
        return Either.right(error);
    }

}
