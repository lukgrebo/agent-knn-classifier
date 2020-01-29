package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderClassificationTrainingRequest {
    private String context;
    private Integer discriminatorColumn;
    private URL trainingSetUrl;
    private double traningSetWeight;
}
