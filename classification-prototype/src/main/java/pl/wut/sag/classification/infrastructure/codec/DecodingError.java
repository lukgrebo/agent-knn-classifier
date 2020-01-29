package pl.wut.sag.classification.infrastructure.codec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DecodingError {
    private final String message;
    private final Exception cause;

    public static DecodingError ofCause(final Exception cause, final String input) {
        return new DecodingError(String.format("Error occured during deserialization of input [%s], error[%s]", input, cause.getMessage()), cause);
    }
}
