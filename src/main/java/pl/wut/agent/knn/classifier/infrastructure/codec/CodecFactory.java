package pl.wut.agent.knn.classifier.infrastructure.codec;

import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodecFactory {
    private final JsonCodec jsonDecoder = new JsonCodec(new GsonBuilder().setPrettyPrinting().create());
    private static final CodecFactory INSTANCE = prepare();

    private static synchronized CodecFactory prepare() {
        return new CodecFactory();
    }

    public static synchronized CodecFactory instance() {
        return INSTANCE;
    }

    public Optional<Codec> forKodec(final String codec) {
        final SupportedCodec supportedCodec = SupportedCodec.forName(codec)
                .orElseThrow(() -> new RuntimeException("Codec: " + codec + " not supported"));

        switch (supportedCodec) {
            case JSON:
                return Optional.of(jsonDecoder);
            default:
                return Optional.empty();
        }
    }

}
