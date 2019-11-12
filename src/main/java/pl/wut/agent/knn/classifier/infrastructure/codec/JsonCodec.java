package pl.wut.agent.knn.classifier.infrastructure.codec;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.wut.agent.knn.classifier.infrastructure.function.Either;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JsonCodec implements Codec {

    private final Gson gson;

    @Override
    public <R> Either<R, DecodingError> decode(final String representation, final Class<R> ontology) {
        return Either.asExceptionHandler(representation, r -> gson.fromJson(r, ontology), DecodingError::ofCause);
    }

    @Override
    public <R> String encode(final R object) {
        return gson.toJson(object);
    }
}
