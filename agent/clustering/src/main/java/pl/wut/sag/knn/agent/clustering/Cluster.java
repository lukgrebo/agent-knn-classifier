package pl.wut.sag.knn.agent.clustering;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.infrastructure.collection.ImmutableList;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Cluster {

    /**
     * Modifiable list.
     */
    @Getter(AccessLevel.PACKAGE)
    private final List<ObjectWithAttributes> elements;

    private final String clusterClass;

    static Cluster emptyWithClass(final String className) {
        return new Cluster(new ArrayList<>(), className);
    }

    public ImmutableList<ObjectWithAttributes> viewElements() {
        return ImmutableList.of(elements);
    }

}
