package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.model.Auction;
import pl.wut.sag.knn.agent.data.model.AuctionStatus;
import pl.wut.sag.knn.infrastructure.MessageSender;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;


public interface AuctionRunner {
    void handleBid(final Bid bid);

    AuctionStatus getAuctionStatus();
}

@Slf4j
@RequiredArgsConstructor
class DefaultAuctionRunner implements AuctionRunner {

    private final UUID correspondingRequestUUID;
    private final Codec codec;
    private final Queue<ObjectWithAttributes> objectsToPropose;
    private Auction currentAuction = new Auction(objectsToPropose.peek());
    private final ServiceDiscovery serviceDiscovery;
    private final MessageSender messageSender;

    @Override
    public void handleBid(final Bid bid) {
        if (bid.getObjectUuid().equals(currentAuction.getObject().getId())) {
            log.error("Auction for object" + bid.getObjectUuid() + " has already ended");
        }

        if (shouldFinalize(currentAuction)) {
            finalize(currentAuction);
            startNewAuction();
        }
    }

    private void startNewAuction() {
        Optional.ofNullable(objectsToPropose.poll()).ifPresent(o -> {
            currentAuction = new Auction(o);
        });
    }

    @Override
    public AuctionStatus getAuctionStatus() {
        final Result<List<DFAgentDescription>, FIPAException> allServices = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());

        final int bidders = allServices.result().size();
        return new AuctionStatus(correspondingRequestUUID, bidders, objectsToPropose.size(), objectsToPropose.isEmpty());
    }

    private boolean shouldFinalize(final Auction auction) {
        final List<AID> alreadyBidded = new ArrayList<>(auction.getBids().keySet());

        final Result<List<AID>, FIPAException> cluteringAgentSearchResult = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService())
                .mapResult(a -> a.stream().map(DFAgentDescription::getName).collect(Collectors.toList()));

        return cluteringAgentSearchResult.isValid() && alreadyBidded.containsAll(cluteringAgentSearchResult.result());
    }

    private void proposeCurrentObject() {
        final Result<List<DFAgentDescription>, FIPAException> clusters = serviceDiscovery.findServices(AuctionProtocol.proposeObject.getTargetService());
        if (clusters.isError() || clusters.result().isEmpty()) {
            log.error("Either error or no clusters found {}" + clusters.error());
            return;
        }
        final List<AID> names = clusters.result().stream().map(DFAgentDescription::getName).collect(Collectors.toList());
        log.info("Sending CFP for current object {} to {} clusters", currentAuction.getObject().getId(), names.size());
        final ACLMessage message = AuctionProtocol.proposeObject.templatedMessage();
        names.forEach(message::addReceiver);
        message.setContent(codec.encode(currentAuction.getObject()));
        messageSender.send(message);

    }

    private void finalize(final Auction auction) {
        auction.getBids().entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().getValue()))
                .ifPresent(this::sendObject);
    }

    private void sendObject(final Map.Entry<AID, Bid> aidBidEntry) {
        final ACLMessage message = AuctionProtocol.acceptBidAndSendObject.templatedMessage();
        message.addReceiver(aidBidEntry.getKey());
        message.setContent(codec.encode(currentAuction.getObject()));
        messageSender.send(message);
    }

}
