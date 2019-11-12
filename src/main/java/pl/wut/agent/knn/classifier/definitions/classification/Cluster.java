package pl.wut.agent.knn.classifier.definitions.classification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Cluster {

    /**
     * Modifiable list.
     */
    private final List<ObjectWithAttributes> objectWithAttributes;

    private final String clusterClass;

    public static Cluster emptyWithClass(final String className) {
        return new Cluster(new ArrayList<>(), className);
    }
}
