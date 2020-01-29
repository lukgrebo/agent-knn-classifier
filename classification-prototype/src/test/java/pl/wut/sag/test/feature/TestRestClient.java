package pl.wut.sag.test.feature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class TestRestClient {
    private final OkHttpClient client;
    private final Gson gson;
    private static final MediaType applicationJson = MediaType.parse("application/json");
    private static final int OK = 200;

    public TestRestClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder().setPrettyPrinting()
                .create();
    }

    public <T, R> R post(final T body, final URI uri, final Class<R> clazz) {
        return operation(uri, b -> b.post(toRequestBody(body)).build(), json -> gson.fromJson(json, clazz));
    }

    public <T, R> R post(final T body, final URI uri, final Function<String, R> responseParser) {
        return operation(uri, b -> b.post(toRequestBody(body)).build(), responseParser);
    }

    public <R> R get(final URI uri, final Function<String, R> responseParser) {
        return operation(uri, b -> b.get().build(), responseParser);
    }

    public <R> R get(final URI uri, final Class<R> clazz) {
        return operation(uri, b -> b.get().build(), json -> gson.fromJson(json, clazz));
    }

    public <R> R get(final URI uri, final TypeToken<R> token) {
        return get(uri, json -> {
            log.info("About to parse json {}", json);
            return gson.fromJson(json, token.getType());
        });
    }

    public <T, R> R post(final T body, final URI uri, final TypeToken<R> token) {
        return post(body, uri, json -> gson.fromJson(json, token.getType()));
    }

    <R> R operation(final URI uri, final Function<Request.Builder, Request> toRequest, final Function<String, R> parseResponse) {
        final Request request = toRequest.apply(rqBuilder(uri));
        log.info("Performing operation url: {} method: {}", uri, request.method());
        try (final Response response = client.newCall(request).execute()) {
            if (OK == response.code()) {
                final String body = response.body().string();
                log.info("Got response {}", body);
                return parseResponse.apply(body);
            } else {
                throw new RuntimeException(response.body().string());
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request.Builder rqBuilder(final URI uri) {
        try {
            return new Request.Builder().url(uri.toURL());
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> RequestBody toRequestBody(final T body) {
        final String bodyString = gson.toJson(body);
        log.trace("Body string is :{}", bodyString);
        return RequestBody.create(applicationJson, bodyString);
    }
}
