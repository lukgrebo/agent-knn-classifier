package pl.wut.sag.knn.ontology;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiningRequest {

    private UUID requestId;
    private URL miningUrl;
    private MiningRequestType type;
    private double minimalBid;
}
