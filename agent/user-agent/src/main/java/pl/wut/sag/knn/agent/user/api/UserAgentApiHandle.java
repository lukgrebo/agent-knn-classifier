package pl.wut.sag.knn.agent.user.api;


import pl.wut.sag.knn.agent.user.api.dto.MiningRequest;
import pl.wut.sag.knn.infrastructure.function.Result;

public interface UserAgentApiHandle {

    Result<String, ? extends Exception> processMiningRequest(MiningRequest miningRequest);
}
