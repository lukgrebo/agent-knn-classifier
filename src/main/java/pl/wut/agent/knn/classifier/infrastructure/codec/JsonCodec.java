package pl.wut.agent.knn.classifier.infrastructure.codec;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class JsonCodec implements Codec {

    private final Gson gson;

    @Override
    public <R> R decode(final String representation, final Class<R> ontology) {
        return gson.fromJson(representation, ontology);
    }

    @Override
    public <R> String encode(final R object) {
        return gson.toJson(object);
    }
}
