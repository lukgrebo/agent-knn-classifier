package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface AuctionState {
    void register(AID agent, ClusterSummary summary);

    boolean isGatheringFinished();

    boolean isRefinementStarted();

    void startRefinement();

    boolean isRefinementDone();

    void finishRefinement();

    Map<AID, ClusterSummary> getSummary();

    static AuctionState newState(final int participants) {
        return new DefaultAuctionState(participants);
    }
}

@Slf4j
@RequiredArgsConstructor
final class DefaultAuctionState implements AuctionState {

    private final int expectedAuctionParticipants;
    private Map<AID, ClusterSummary> summaryByAgent = new HashMap<>();
    boolean refinementStarted = false;
    private boolean refinementDone;

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
    public boolean isRefinementStarted() {
        return refinementStarted;
    }

    @Override
    public void startRefinement() {
        this.refinementStarted = true;
    }

    @Override
    public boolean isRefinementDone() {
        return refinementDone;
    }

    @Override
    public void finishRefinement() {
        this.refinementDone = true;
    }

    @Override
    public Map<AID, ClusterSummary> getSummary() {
        return Collections.unmodifiableMap(summaryByAgent);
    }

}
