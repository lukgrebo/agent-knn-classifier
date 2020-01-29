package pl.wut.sag.classification.agent.classification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ClassificationAgentDependencies {
    private final String className;
    private final int discriminatorColumn;
}
