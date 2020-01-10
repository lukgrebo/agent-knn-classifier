package pl.wut.sag.knn.agent.data.model;

import jade.core.AID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.Set;


@Getter
@RequiredArgsConstructor
public class ClusterSummaryWithObjects {
    private final AID agent;
    private final double averageDistance;
    private final Set<ObjectWithAttributes> objects;
}
