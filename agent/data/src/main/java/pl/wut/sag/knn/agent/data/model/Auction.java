package pl.wut.sag.knn.agent.data.model;

import jade.core.AID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class Auction {
    private final ObjectWithAttributes object;
    private final Map<AID, Bid> bids = new HashMap<>();

    public void registerBid(final AID sender, final Bid bid) {
        bids.put(sender, bid);
    }
}
