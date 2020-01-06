package pl.wut.sag.knn.agent.user;

import jade.core.Agent;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.user.api.UserAgentApi;
import pl.wut.sag.knn.agent.user.api.UserAgentApiHandle;
import pl.wut.sag.knn.agent.user.api.dto.MiningRequest;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.function.Result;

@Slf4j
public class UserAgent extends Agent implements UserAgentApiHandle {

    private final Codec codec = Codec.json();

    @Override
    protected void setup() {
        final UserAgentDependencies dependencies = (UserAgentDependencies) getArguments()[0];
        log.info("Running user agent");
        UserAgentApi.start(dependencies.getApiConfig(), this, codec);

    }

    @Override
    public Result<String, Exception> processMiningRequest(final MiningRequest miningRequest) {
        log.info("Got mining request to process {}", miningRequest);
        return Result.ok("Hehe xD");
    }
}
