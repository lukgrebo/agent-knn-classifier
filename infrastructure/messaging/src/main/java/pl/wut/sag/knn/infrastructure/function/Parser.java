package pl.wut.sag.knn.infrastructure.function;

import java.util.function.Function;

public class Parser {

    public static <T, L> Result<L, Exception> tryParse(final T input, final Function<T, L> parser) {
        try {
            return Result.ok(parser.apply(input));
        } catch (final Exception e) {
            return Result.error(e);
        }
    }
}
