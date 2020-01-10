package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface AuctionState {
    void register(AID agent, ClusterSummary summary);

    boolean isGatheringFinished();

    boolean isRefinementStarted();

    void startRefinement();

    boolean isRefinementDone();

    void refinementFinished(final AID aid);

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
    private final Set<AID> refinementFinished = new HashSet<>();
    boolean refinementStarted;

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
        return refinementFinished.size() == expectedAuctionParticipants;
    }

    @Override
    public void refinementFinished(final AID aid) {
        refinementFinished.add(aid);
    }

    @Override
    public Map<AID, ClusterSummary> getSummary() {
        return Collections.unmodifiableMap(summaryByAgent);
    }

}
