package pl.wut.agent.knn.classifier.definitions.classification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class Cluster {

    /**
     * Modifiable list.
     */
    private final List<ObjectWithAttributes> objectWithAttributes;

    private final String clusterClass;
}
