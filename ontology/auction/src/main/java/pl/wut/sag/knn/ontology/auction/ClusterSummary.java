package pl.wut.sag.knn.ontology.auction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSummary {
    private Set<UUID> objectsIds;
    private double averageDistance;
}
