package pl.wut.sag.knn.agent.clustering.algorithm;

import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.agent.clustering.Cluster;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

public interface BidCalculator {
    Bid calculateBid(final Cluster cluster, final ObjectWithAttributes incomingObject);

    static BidCalculator calculator(final DistanceCalculator distanceCalculator) {
        return new DefaultBidCalculator(distanceCalculator);
    }
}

@RequiredArgsConstructor
class DefaultBidCalculator implements BidCalculator {

    private final DistanceCalculator distanceCalculator;

    @Override
    public Bid calculateBid(final Cluster cluster, final ObjectWithAttributes incomingObject) {
        final double averageDistance = cluster.viewElements().stream()
                .mapToDouble(o -> distanceCalculator.calculateDistance(o, incomingObject))
                .average()
                .orElse(Double.MIN_VALUE);

        final double bidValue;
        if (cluster.viewElements().isEmpty() || averageDistance == 0) {
            bidValue = 100000;
        } else {
            bidValue = 1 / averageDistance;
        }

        return Bid.from(bidValue, incomingObject.getId());
    }
}
