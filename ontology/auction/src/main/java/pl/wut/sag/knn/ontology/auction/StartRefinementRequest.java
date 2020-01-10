package pl.wut.sag.knn.ontology.auction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartRefinementRequest {
    private UUID auctionId;
}
