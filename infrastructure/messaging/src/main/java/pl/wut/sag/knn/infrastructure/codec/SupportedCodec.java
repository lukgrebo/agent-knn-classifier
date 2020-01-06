package pl.wut.sag.knn.infrastructure.codec;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SupportedCodec {
    JSON("json", "json;utf-8");

    SupportedCodec(final String... names) {
        this.alternativeNames = Stream.of(names).map(String::toLowerCase).collect(Collectors.toList());
    }

    private final List<String> alternativeNames;

    public static Optional<SupportedCodec> forName(final String name) {
        return Arrays.stream(values())
                .filter(f -> f.alternativeNames.contains(name.toLowerCase()))
                .findFirst();
    }
}
