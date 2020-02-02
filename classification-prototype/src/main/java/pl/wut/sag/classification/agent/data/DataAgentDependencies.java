package pl.wut.sag.classification.agent.data;

import jade.core.AID;
import lombok.Data;

@Data
public class DataAgentDependencies {
    private final String context;
    private final AID userAgent;
}
