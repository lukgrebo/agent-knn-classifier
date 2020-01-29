package pl.wut.sag.knn.agent.user.api;

import io.javalin.Javalin;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.user.api.dto.MiningRequest;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.codec.DecodingError;
import pl.wut.sag.knn.infrastructure.function.Parser;
import pl.wut.sag.knn.infrastructure.function.Result;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAgentApi {

    private static UserAgentApi instance;
    private final ApiConfig config;
    private final UserAgentApiHandle userAgentApiHandle;
    private final Codec codec;

    /* Request params */
    private final static String MINING_REQUEST_ID_PARAM = "miningRequestId";

    public static UserAgentApi start(final ApiConfig config, final UserAgentApiHandle handle, final Codec codec) {
        if (instance == null) {
            instance = new UserAgentApi(config, handle, codec);
            instance.start();
        }
        return instance;
    }

    private void start() {
        final Javalin javalin = Javalin.create()
                .post("/mining", ctx -> {
                    log.info("Got mining request");
                    final Result<MiningRequest, DecodingError> decodingResult = codec.decode(ctx.body(), MiningRequest.class);
                    if (decodingResult.isError()) {
                        ctx.result("Could not decode incoming object");
                        return;
                    }
                    final Result<String, ? extends Exception> miningResponse = userAgentApiHandle.processMiningRequest(decodingResult.result());
                    if (miningResponse.isError()) {
                        ctx.result("Could not start mining: " + miningResponse.error().getMessage());
                        return;
                    }
                    ctx.result(miningResponse.result());
                })

                .get("/mining/report", ctx -> {
                    final String miningRequestId = ctx.queryParam(MINING_REQUEST_ID_PARAM);
                    if (miningRequestId == null || miningRequestId.isEmpty()) {
                        ctx.result("parameter " + miningRequestId + " is required");
                        return;
                    }
                    final Result<UUID, Exception> parsed = Parser.tryParse(miningRequestId, UUID::fromString);
                    if (parsed.isError()) {
                        ctx.result(MINING_REQUEST_ID_PARAM + " must be id");
                        return;
                    }


                    ctx.result(userAgentApiHandle.getReport(parsed.result()).orElse("Nie ma jeszcze raportu"));
                })
                .start(config.getPort());
    }

}
