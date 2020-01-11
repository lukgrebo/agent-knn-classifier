package pl.wut.sag.knn.ontology.auction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class RefinementSummary {
    private final ClusterSummary clusterSummary;
}
