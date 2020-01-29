package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.Data;

@Data
public class AgentStats {

    private String agentName;
    private int trainingSetSize;
    private String className;
}
