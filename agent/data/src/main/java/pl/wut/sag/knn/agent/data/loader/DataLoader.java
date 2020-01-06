package pl.wut.sag.knn.agent.data.loader;

import pl.wut.sag.knn.infrastructure.function.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public interface DataLoader {
    Result<Stream<String>, IOException> openDataStream(final URL url);

    static DataLoader defaultLoader() {
        return new DefaultDataLoader();
    }
}

class DefaultDataLoader implements DataLoader {

    @Override
    public Result<Stream<String>, IOException> openDataStream(final URL url) {
        try {
            final URLConnection conn = url.openConnection();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return Result.ok(reader.lines());
            }
        } catch (final IOException e) {
            return Result.error(e);
        }
    }
}
