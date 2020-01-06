package pl.wut.sag.knn.ontology;

import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
public class MiningRequest {

    private UUID requestId;
    private URL miningUrl;
    private MiningRequestType type;
}
