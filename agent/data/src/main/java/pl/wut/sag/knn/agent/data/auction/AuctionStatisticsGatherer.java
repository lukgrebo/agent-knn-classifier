package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.message_handler.IMessageSpecification;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface AuctionStatisticsGatherer {
    boolean isGatheringFinished();

    static AuctionStatisticsGatherer defaultGatherer(final int participants, final MessageHandler handler, final Codec codec) {
        final DefaultAuctionStatisticsGatherer gatherer = new DefaultAuctionStatisticsGatherer(participants, handler, codec);
        handler.add(gatherer);

        return gatherer;
    }
}

@Slf4j
@RequiredArgsConstructor
final class DefaultAuctionStatisticsGatherer implements AuctionStatisticsGatherer, IMessageSpecification {

    private final int expectedAuctionParticipants;
    private final MessageHandler messageHandler;
    private final Codec codec;
    private Map<AID, Set<UUID>> objectsByAgent = new HashMap<>();

    @Override
    public boolean isGatheringFinished() {
        return expectedAuctionParticipants == objectsByAgent.size();
    }

    @Override
    public MessageTemplate getTemplateToMatch() {
        return AuctionProtocol.summaryResponse.toMessageTemplate();
    }

    @Override
    public Consumer<ACLMessage> getAction() {
        return message -> {
            final ClusterSummary result = codec.decode(message.getContent(), AuctionProtocol.summaryResponse.getMessageClass()).result();
            objectsByAgent.put(message.getSender(), result.getObjectsIds());
            if (isGatheringFinished()) {
                log.info("Gathering finished");
                messageHandler.remove(this);
            }
        };
    }
}
