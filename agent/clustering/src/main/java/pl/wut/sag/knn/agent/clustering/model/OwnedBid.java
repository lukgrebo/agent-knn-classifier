package pl.wut.sag.knn.agent.clustering.model;


import jade.core.AID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.ontology.auction.Bid;

import java.util.UUID;

@RequiredArgsConstructor
public class OwnedBid {

    private final Bid bid;
    @Getter
    private final AID owner;

    public double getValue() {
        return bid.getValue();
    }

    public UUID getObjectUuid() {
        return bid.getObjectUuid();
    }
}
