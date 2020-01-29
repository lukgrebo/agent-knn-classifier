package pl.wut.sag.classification.agent.user.interfaces.web;

import io.javalin.Javalin;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectRequest;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest;
import pl.wut.sag.classification.infrastructure.codec.Codec;
import pl.wut.sag.classification.infrastructure.codec.DecodingError;
import pl.wut.sag.classification.infrastructure.function.Result;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAgentWebApi {

    private static final Codec codec = Codec.json();
    private final Javalin javalin;

    public static UserAgentWebApi setupApi(final int port, final UserApiWebHandle webHandle) {
        final Javalin javalin = Javalin.create()
                .post("/train", ctx -> {
                    final Result<OrderClassificationTrainingRequest, DecodingError> parsed = codec.decode(ctx.body(), OrderClassificationTrainingRequest.class);
                    if (parsed.isError()) {
                        ctx.result("Nie udało się odczytać obiektu przesłanego w zapytaniu");
                        return;
                    }
                    ctx.result(webHandle.processTrainingRequest(parsed.result()));
                })
                .post("/check", ctx -> {
                    final Result<CheckObjectRequest, DecodingError> parsed = codec.decode(ctx.body(), CheckObjectRequest.class);
                    if (parsed.isError()) {
                        ctx.result("Nie udało się odczytać obiektu przesłanego w zapytaniu");
                        return;
                    }
                    ctx.result(webHandle.checkObjectClass(parsed.result()));
                })
                .get("/results/:context", ctx -> {
                    final String context = ctx.pathParam("context");
                    if (context == null || context.isEmpty()) {
                        ctx.result("Podanie kontekstu w ścieżce jest wymagane");
                        return;
                    }
                    ctx.result(webHandle.getResults(context));

                })
                .post("/results/:context/clear", ctx -> {
                    final String context = ctx.pathParam("context");

                    if (context == null || context.isEmpty()) {
                        ctx.result("Podanie kontekstu w ścieżce jest wymagane");
                        return;
                    }
                    ctx.result(webHandle.clearResults(context));

                })
                .get("/results/:context/:id", ctx -> {
                    final String context = ctx.pathParam("context");
                    if (context == null || context.isEmpty()) {
                        ctx.result("Podanie kontekstu w ścieżce jest wymagane");
                        return;
                    }
                    final UUID id = UUID.fromString(ctx.pathParam("id"));
                    ctx.result(webHandle.getResult(context, id));

                })
                .get("/contexts", ctx -> {
                    ctx.result(codec.encode(webHandle.getContexts()));
                })
                .start(port);

        return new UserAgentWebApi(javalin);
    }


}
