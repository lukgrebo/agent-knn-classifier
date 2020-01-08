package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AuctionStatisticsGatherer {
    void register(AID agent, ClusterSummary summary);

    boolean isGatheringFinished();

    static AuctionStatisticsGatherer defaultGatherer(final int participants) {
        return new DefaultAuctionStatisticsGatherer(participants);
    }
}

@Slf4j
@RequiredArgsConstructor
final class DefaultAuctionStatisticsGatherer implements AuctionStatisticsGatherer {

    private final int expectedAuctionParticipants;
    private Map<AID, Set<UUID>> objectsByAgent = new HashMap<>();
    private final ReportGenerator reportGenerator = ReportGenerator.stringToConsole();

    @Override
    public void register(final AID agent, final ClusterSummary summary) {
        objectsByAgent.put(agent, summary.getObjectsIds());
        log.debug("Gatherer registered {} responses", objectsByAgent.size());

        if (isGatheringFinished()) {
            try {
                reportGenerator.generate(new URL("https://www.google.com"), new HashMap<>()); //TODO
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isGatheringFinished() {
        return expectedAuctionParticipants == objectsByAgent.size();
    }

}
