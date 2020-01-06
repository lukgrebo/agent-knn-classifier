package pl.wut.sag.knn.infrastructure.parser;


import pl.wut.sag.knn.infrastructure.function.Result;

public class DoubleParser implements Parser<Double> {

    private final static String javaDecimalSeparator = ".";
    private final static String allowedDecimalSeparator = ",";

    @Override
    public Result<Double, ParseError> parse(final String input) {
        if (input == null) {
            return error(ParseError.inputIsNull());
        }

        try {
            return Result.ok(Double.parseDouble(input.replaceAll(allowedDecimalSeparator, javaDecimalSeparator)));
        } catch (final RuntimeException e) {
            return error(ParseError.ofCause(e));
        }
    }

    private Result<Double, ParseError> error(final ParseError error) {
        return Result.error(error);
    }

}
