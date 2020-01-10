package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface AuctionStatisticsGatherer {
    void register(AID agent, ClusterSummary summary);

    boolean isGatheringFinished();

    Map<AID, ClusterSummary> getSummary();

    static AuctionStatisticsGatherer defaultGatherer(final int participants) {
        return new DefaultAuctionStatisticsGatherer(participants);
    }
}

@Slf4j
@RequiredArgsConstructor
final class DefaultAuctionStatisticsGatherer implements AuctionStatisticsGatherer {

    private final int expectedAuctionParticipants;
    private Map<AID, ClusterSummary> summaryByAgent = new HashMap<>();

    @Override
    public void register(final AID agent, final ClusterSummary summary) {
        summaryByAgent.put(agent, summary);
        log.debug("Gatherer registered {} responses", summaryByAgent.size());
    }

    @Override
    public boolean isGatheringFinished() {
        return expectedAuctionParticipants == summaryByAgent.size();
    }

    @Override
    public Map<AID, ClusterSummary> getSummary() {
        return Collections.unmodifiableMap(summaryByAgent);
    }

}
