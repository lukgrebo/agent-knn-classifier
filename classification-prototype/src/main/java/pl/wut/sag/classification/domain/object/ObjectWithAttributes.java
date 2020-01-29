package pl.wut.sag.classification.domain.object;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectWithAttributes {
    @Getter
    private final UUID id;

    private final Map<Integer, String> attributesAndValues;

    @Setter
    private int discriminatorColumn;

    public Stream<IndexedAttribute> indexedAttributesSkipDiscriminator() {
        return attributesAndValues.entrySet().stream()
                .filter(e -> e.getKey() != discriminatorColumn)
                .map(e -> IndexedAttribute.of(e.getKey(), e.getValue()));
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

    public static ObjectWithAttributes of(final Map<Integer, String> attributesAndValues, final int discriminatorColumn) {
        return new ObjectWithAttributes(UUID.randomUUID(), attributesAndValues, discriminatorColumn);
    }

    public static ObjectWithAttributes of(final UUID uuid, final Map<Integer, String> attributesAndValues, final int discriminatorColumn) {
        return new ObjectWithAttributes(uuid, attributesAndValues, discriminatorColumn);
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
