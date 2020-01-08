package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AuctionStatisticsGatherer {
    void register(AID agent, ClusterSummary summary);

    boolean isGatheringFinished();

    Map<AID, Set<UUID>> getSummary();

    static AuctionStatisticsGatherer defaultGatherer(final int participants) {
        return new DefaultAuctionStatisticsGatherer(participants);
    }
}

@Slf4j
@RequiredArgsConstructor
final class DefaultAuctionStatisticsGatherer implements AuctionStatisticsGatherer {

    private final int expectedAuctionParticipants;
    private Map<AID, Set<UUID>> objectsByAgent = new HashMap<>();

    @Override
    public void register(final AID agent, final ClusterSummary summary) {
        objectsByAgent.put(agent, summary.getObjectsIds());
        log.debug("Gatherer registered {} responses", objectsByAgent.size());
    }

    @Override
    public boolean isGatheringFinished() {
        return expectedAuctionParticipants == objectsByAgent.size();
    }

    @Override
    public Map<AID, Set<UUID>> getSummary() {
        return Collections.unmodifiableMap(objectsByAgent);
    }

}
