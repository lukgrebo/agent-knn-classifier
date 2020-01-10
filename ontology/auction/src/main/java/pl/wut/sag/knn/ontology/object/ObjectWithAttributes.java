package pl.wut.sag.knn.ontology.object;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectWithAttributes {
    @Getter
    private final UUID id;

    private final Map<Integer, String> attributesAndValues;

    public Stream<IndexedAttribute> indexedAttributes() {
        return attributesAndValues.entrySet().stream().map(e -> IndexedAttribute.of(e.getKey(), e.getValue()));
    }

    public Optional<String> getAsString(final int attributeOrderNumber) {
        return Optional.ofNullable(attributesAndValues.get(attributeOrderNumber));
    }

    public int nAttributes() {
        return attributesAndValues.size();
    }

    /**
     * @param attributeOrderNumber order number of attribute.
     * @param parser               function used to parse attribute value.
     * @param <R>                  ok type.
     * @return Optional containing parsed value or empty.
     * @throws RuntimeException Runtime exceptions thrown by parser.
     */
    public <R> Optional<R> getAndParse(final int attributeOrderNumber, final Function<String, R> parser) {
        return getAsString(attributeOrderNumber).map(parser);
    }

    public static ObjectWithAttributes of(final Map<Integer, String> attributesAndValues) {
        return new ObjectWithAttributes(UUID.randomUUID(), attributesAndValues);
    }

    public String humanReadableString() {
        return String.format("{%s(%s)}", id, humanFormattedAttributes());
    }

    private String humanFormattedAttributes() {
        final Integer maxIndex = attributesAndValues.keySet().stream().max(Integer::compareTo).orElse(0);

        return IntStream.rangeClosed(0, maxIndex)
                .mapToObj(attributesAndValues::get)
                .map(Optional::ofNullable)
                .map(o -> o.orElse(" "))
                .collect(Collectors.joining(","));
    }
}
