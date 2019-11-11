package pl.wut.agent.knn.classifier.infrastructure.codec;

public interface Decoder {
    <R> R decode(String representation, Class<R> ontology);
}
