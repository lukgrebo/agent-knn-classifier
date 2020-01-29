package pl.wut.sag.classification.infrastructure.codec;


import com.google.gson.GsonBuilder;
import pl.wut.sag.classification.infrastructure.function.Result;

public interface Codec {
    <R> Result<R, DecodingError> decode(String representation, Class<R> ontology);

    <R> String encode(R object);

    static Codec json() {
        return new JsonCodec(new GsonBuilder().setPrettyPrinting().create());
    }
}
