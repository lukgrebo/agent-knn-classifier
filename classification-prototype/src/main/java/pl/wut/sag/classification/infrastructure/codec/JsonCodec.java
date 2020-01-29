package pl.wut.sag.classification.infrastructure.codec;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.infrastructure.function.Result;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JsonCodec implements Codec {

    private final Gson gson;

    @Override
    public <R> Result<R, DecodingError> decode(final String representation, final Class<R> ontology) {
        return Result.asExceptionHandler(representation, r -> gson.fromJson(r, ontology), DecodingError::ofCause);
    }

    @Override
    public <R> String encode(final R object) {
        return gson.toJson(object);
    }
}
