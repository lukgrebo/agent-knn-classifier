package pl.wut.agent.knn.classifier.infrastructure.codec;

import com.google.gson.GsonBuilder;

import java.util.Optional;

public class DecoderFactory {
    private final JsonDecoder jsonDecoder = new JsonDecoder(new GsonBuilder().setPrettyPrinting().create());

    private DecoderFactory() {
    }

    public Optional<Decoder> forKodec(final String codec) {
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
