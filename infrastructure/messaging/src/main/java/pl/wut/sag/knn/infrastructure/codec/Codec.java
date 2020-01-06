package pl.wut.sag.knn.infrastructure.codec;


import pl.wut.sag.knn.infrastructure.function.Result;

public interface Codec {
    <R> Result<R, DecodingError> decode(String representation, Class<R> ontology);

    <R> String encode(R object);
}
