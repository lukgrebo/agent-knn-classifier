package pl.wut.sag.classification.infrastructure.parser;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParseError {
    @Getter
    private final String message;

    @Getter
    private final Exception cause;

    public static ParseError ofMessage(final String message) {
        return new ParseError(Objects.requireNonNull(message), null);
    }

    static ParseError ofCause(final Exception cause) {
        return new ParseError("Exception occured during parsing: " + cause.getClass().getSimpleName() + cause.getMessage(), cause);
    }

    static ParseError inputIsNull() {
        return new ParseError("Input string is null", null);
    }
}
