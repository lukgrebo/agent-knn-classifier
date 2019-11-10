package pl.wut.agent.knn.classifier.definitions.bidding;

import jade.core.AID;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "from")
public class Bid implements Serializable {
    private final AID bidder;
    private final double value;
    private final UUID objectUuid;
}
