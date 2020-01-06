package pl.wut.sag.knn.agent.user;

import lombok.Value;
import pl.wut.sag.knn.agent.user.api.ApiConfig;

@Value
public class UserAgentDependencies {
    private final ApiConfig apiConfig;
}
