package pl.wut.agent.knn.classifier.infrastructure.codec;

import pl.wut.agent.knn.classifier.infrastructure.function.Either;

public interface Codec {
    <R> Either<R, DecodingError> decode(String representation, Class<R> ontology);

    <R> String encode(R object);
}
