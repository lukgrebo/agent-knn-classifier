package pl.wut.sag.classification.agent.user.interfaces.web;

import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectRequest;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest;

import java.util.List;
import java.util.UUID;

public interface UserApiWebHandle {
    String processTrainingRequest(OrderClassificationTrainingRequest request);
    List<String> getContexts();
    String checkObjectClass(CheckObjectRequest request);

    String getResults(String context);

    String getResult(String context, UUID id);

    String clearResults(String context);

}
