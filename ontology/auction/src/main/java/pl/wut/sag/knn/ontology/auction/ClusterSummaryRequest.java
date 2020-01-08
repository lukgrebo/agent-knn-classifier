package pl.wut.sag.knn.ontology.auction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSummaryRequest {
    private UUID requestId;

    public static ClusterSummaryRequest ofRandomUUID() {
        return new ClusterSummaryRequest(UUID.randomUUID());
    }
}
