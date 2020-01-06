package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.model.Auction;
import pl.wut.sag.knn.agent.data.model.AuctionStatus;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Queue<ObjectWithAttributes> objectsToPropose;
    private final Map<UUID, Auction> auctionByObjectId = new HashMap<>();
    private final ServiceDiscovery serviceDiscovery;

    @Override
    public void handleBid(final Bid bid) {
        final Optional<Auction> auctionOpt = findAuction(bid);

        if (!auctionOpt.isPresent()) {
            log.info("Auction for bid: " + bid.getObjectUuid() + " not found");
            return;
        }

        final Auction auction = auctionOpt.get();
        if (shouldFinalize(auction)) {
            finalize(auction);
        }
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

    private void finalize(final Auction auction) {
        auction.getBids().entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().getValue()))
                .ifPresent(null /* todo, finalize */);
    }

    private Optional<Auction> findAuction(final Bid bid) {
        return Optional.ofNullable(auctionByObjectId.get(bid.getObjectUuid()));
    }
}
