package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.Data;

import java.net.URL;

@Data
public class OrderClassificationTrainingRequest {
    private String context;
    private Integer discriminatorColumn;
    private URL trainingSetUrl;
}
