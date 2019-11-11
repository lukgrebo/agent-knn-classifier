package pl.wut.agent.knn.classifier.infrastructure.codec;

public interface Codec {
    <R> R decode(String representation, Class<R> ontology);
    <R> String encode(R object);
}
