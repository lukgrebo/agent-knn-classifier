package pl.wut.sag.knn.infrastructure.codec

import lombok.Getter

import java.util.function.Function

class ObjectWithAttributes {
    @Getter
    private final UUID id
    private final Map<Integer, String> attributesAndValues

    Optional<String> getAsString(final int attributeOrderNumber) {
        return Optional.ofNullable(attributesAndValues.get(attributeOrderNumber));
    }

    int nAttributes() {
        return attributesAndValues.size()
    }

    def <R> Optional<R> getAndParse(final int attributeOrderNumber, final Function<String, R> parser) {
        return getAsString(attributeOrderNumber).map(parser)
    }

    static ObjectWithAttributes of(final Map<Integer, String> attributesAndValues) {
        return new ObjectWithAttributes(UUID.randomUUID(), attributesAndValues)
    }
}
