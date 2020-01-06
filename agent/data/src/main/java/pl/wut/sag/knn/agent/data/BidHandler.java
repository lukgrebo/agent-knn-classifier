package pl.wut.sag.knn.agent.data;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.agent.data.model.Auction;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.codec.DecodingError;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.auction.Bid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class BidHandler {

    private final List<Auction> auctions = new ArrayList<>();
    private final DataAgentConfiguration config;
    private final Codec codec;
    private final ServiceDiscovery serviceDiscovery;

    public void handleBid(final ACLMessage receive) {
        final Result<Bid, DecodingError> decode = codec.decode(receive.getContent(), Bid.class);
        if (decode.isError()) {
            log.error("Cannot decode: " + receive.getContent(), decode.error().getCause());
        } else {
            final Bid bid = decode.result();
            final Optional<Auction> auctionOpt = findAuction(bid);

            if (!auctionOpt.isPresent()) {
                log.info("Auction for bid: " + bid.getObjectUuid() + " not found");
                return;
            }

            final Auction auction = auctionOpt.get();
            auction.getBids().put(receive.getSender(), bid);
            if (shouldFinalize(auction)) {
                finalize(auction);
            }
        }
    }

    private boolean shouldFinalize(final Auction auction) {
        final List<AID> alreadyBidded = new ArrayList<>(auction.getBids().keySet());
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName(config.clusteringServiceName());

        final Result<List<AID>, FIPAException> cluteringAgentSearchResult = serviceDiscovery.findServices(serviceDescription)
                .mapResult(a -> a.stream().map(DFAgentDescription::getName).collect(Collectors.toList()));

        return cluteringAgentSearchResult.isValid() && alreadyBidded.containsAll(cluteringAgentSearchResult.result());
    }

    private void finalize(final Auction auction) {
        auction.getBids().entrySet().stream().max(Comparator.comparingDouble(e -> e.getValue().getValue()))
                .ifPresent(null /* todo, finalize */);
    }

    private Optional<Auction> findAuction(final Bid bid) {
        return this.auctions.stream().filter(a -> a.getObject().getId().equals(bid.getObjectUuid())).findFirst();
    }
}
