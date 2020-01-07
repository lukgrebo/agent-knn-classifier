package pl.wut.sag.knn.ontology.auction;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ClusterSummary {
    private final Set<UUID> objectsIds;
}
