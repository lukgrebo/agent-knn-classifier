package pl.wut.sag.classification.infrastructure.dataLoader;

import pl.wut.sag.classification.infrastructure.function.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public interface DataLoader {
    Result<List<String>, IOException> getData(final URL url);

    static DataLoader defaultLoader() {
        return new DefaultDataLoader();
    }
}

class DefaultDataLoader implements DataLoader {

    @Override
    public Result<List<String>, IOException> getData(final URL url) {
        try {
            final URLConnection conn = url.openConnection();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return Result.ok(reader.lines().collect(Collectors.toList()));
            }
        } catch (final IOException e) {
            return Result.error(e);
        }
    }
}
