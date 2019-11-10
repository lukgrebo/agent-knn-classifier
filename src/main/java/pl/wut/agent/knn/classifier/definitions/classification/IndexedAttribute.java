package pl.wut.agent.knn.classifier.definitions.classification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class IndexedAttribute {
    private final int index;
    private final String value;
}
