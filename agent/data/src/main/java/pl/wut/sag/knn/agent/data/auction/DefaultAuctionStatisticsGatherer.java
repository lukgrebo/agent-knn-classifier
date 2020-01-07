package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

interface AuctionStatisticsGatherer {
    void registerSummary(final ClusterSummary clusterSummary, final AID agent);

    boolean isGatheringFinished();
}

@RequiredArgsConstructor
class DefaultAuctionStatisticsGatherer implements AuctionStatisticsGatherer {

    private final int expectedAuctionParticipants;
    private Map<AID, Set<UUID>> objectsByAgent = new HashMap<>();

    @Override
    public void registerSummary(final ClusterSummary clusterSummary, final AID agent) {
        objectsByAgent.put(agent, clusterSummary.getObjectsIds());
    }

    @Override
    public boolean isGatheringFinished() {
        return expectedAuctionParticipants == objectsByAgent.size();
    }
}
