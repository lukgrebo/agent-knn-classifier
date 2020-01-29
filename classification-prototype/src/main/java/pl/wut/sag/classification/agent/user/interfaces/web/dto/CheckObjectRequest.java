package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CheckObjectRequest {
    private String context;
    private Map<Integer, String> attributesByIndex;
}
