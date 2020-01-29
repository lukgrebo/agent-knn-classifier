package pl.wut.sag.classification.infrastructure.function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Accessors(fluent = true)
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<L, R> {

    @Getter
    private final L result;

    @Getter
    private final R error;

    public static <L, R> Result<L, R> ok(final L left) {
        return new Result<>(Objects.requireNonNull(left), null);
    }

    public static <R> Result<Void, R> empty() {
        return new Result<>(null, null);
    }

    public static <L, R> Result<L, R> error(final R right) {
        return new Result<>(null, Objects.requireNonNull(right));
    }

    public static <T, L, R> Result<L, R> asExceptionHandler(final T input, final Function<T, L> mapping, final BiFunction<Exception, T, R> exceptionMapper) {
        try {
            return Result.ok(mapping.apply(input));
        } catch (final Exception e) {
            return Result.error(exceptionMapper.apply(e, input));
        }
    }

    public L getOrThrow(final Function<R, ? extends RuntimeException> mapper) {
        if(isValid()) {
            return result();
        } else {
            throw mapper.apply(error());
        }
    }

    public <X> Result<X, R> mapResult(final Function<L, X> mapper) {
        return this.isValid() ? Result.ok(mapper.apply(this.result)) : Result.error(this.error);
    }

    public boolean isValid() {
        return result != null;
    }

    public boolean isError() {
        return !isValid();
    }
}
