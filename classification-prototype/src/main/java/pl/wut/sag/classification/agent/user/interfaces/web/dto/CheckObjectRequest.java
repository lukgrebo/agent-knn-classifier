package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckObjectRequest {
    private String context;
    private Map<Integer, String> attributesByIndex;
}
