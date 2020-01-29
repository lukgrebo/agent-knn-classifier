package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.Data;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

import java.util.List;
import java.util.Map;

@Data
public class ClassificationStatistics {

    private Map<String, ObjectWithAttributes> classifications;
    private List<AgentStats> agentStats;
}
