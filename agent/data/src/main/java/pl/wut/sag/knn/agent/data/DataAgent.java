package pl.wut.sag.knn.agent.data;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.auction.AuctionRunner;
import pl.wut.sag.knn.agent.data.auction.AuctionRunnerFactory;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.agent.data.loader.CsvObjectParser;
import pl.wut.sag.knn.agent.data.loader.DataLoader;
import pl.wut.sag.knn.agent.data.model.AuctionStatus;
import pl.wut.sag.knn.infrastructure.MessageSender;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.ParseError;
import pl.wut.sag.knn.ontology.MiningRequest;
import pl.wut.sag.knn.ontology.MiningStatus;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.MiningProtocol;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DataAgent extends Agent implements MessageSender {

    private final Codec codec = Codec.json();
    private final CsvObjectParser csvObjectParser = new CsvObjectParser();
    private final Queue<MiningRequest> miningRequests = new ArrayDeque<>();
    private final AuctionRunnerFactory auctionRunnerFactory = new AuctionRunnerFactory(new DataAgentConfiguration(), codec, new ServiceDiscovery(this), this);
    private AuctionRunner currentRunner;

    @Override
    protected void setup() {
        addBehaviour(new MessageHandler(
                MessageSpecification.of(MiningProtocol.sendRequest.toMessageTemplate(), this::startMining),
                MessageSpecification.of(AuctionProtocol.sendBid.toMessageTemplate(), this::handleBid),
                MessageSpecification.of(MiningProtocol.checkStatus.toMessageTemplate(), this::checkStatus)
        ));
        registerServices();
    }

    private void startMining(final ACLMessage message) {
        final MiningRequest request = codec.decode(message.getContent(), MiningProtocol.sendRequest.getMessageClass()).result();
        log.info("Got mining request", request);

        final DataLoader dataLoader = DataLoader.defaultLoader();
        final Result<List<String>, IOException> result = dataLoader.getData(request.getMiningUrl());
        if (result.isError()) {
            log.info("Error loading data from" + request.getMiningUrl());
            return;
        }
        final List<Result<ObjectWithAttributes, ParseError>> parsedEntries = result.result().stream()
                .map(csvObjectParser::parse)
                .collect(Collectors.toList());

        if (currentRunner == null) {
            log.info("Could not parse {}", parsedEntries.stream().filter(Result::isError).collect(Collectors.toList()).size());
            final Set<ObjectWithAttributes> objects = parsedEntries.stream().filter(Result::isValid).map(Result::result).collect(Collectors.toSet());
            currentRunner = auctionRunnerFactory.newRunner(request.getRequestId(), objects);
        } else {
            log.info("Currently there is auction ongoing, add request to queue");
            miningRequests.add(request);
        }
    }

    private void handleBid(final ACLMessage message) {
        final Bid bid = codec.decode(message.getContent(), AuctionProtocol.sendBid.getMessageClass()).result();
        if (currentRunner != null) {
            log.info("Got bid {}", bid);
            currentRunner.handleBid(bid, message.getSender());
        } else {
            log.warn("There is no ongoing auction and bid was received, ignoring {}", bid);
        }
    }

    private void checkStatus(final ACLMessage message) {
        final UUID statusToCheck = codec.decode(message.getContent(), MiningProtocol.checkStatus.getMessageClass()).result();
        if (currentRunner == null) {
            sendStatusResponse(message, new MiningStatus(0, "Nic nam nie wiadomo o takim zapytaniu"));
        } else {
            final AuctionStatus currentRunnerStatus = currentRunner.getAuctionStatus();
            if (currentRunnerStatus.getUuid().equals(statusToCheck)) {
                sendStatusResponse(message, new MiningStatus(currentRunnerStatus.getParticipants(), "Obecnie trwa przetwarzanie"));
            } else if (miningRequests.stream().anyMatch(r -> r.getRequestId().equals(statusToCheck))) {
                sendStatusResponse(message, new MiningStatus(0, "Zapytanie oczekuje na przetwarzanie"));
            } else {
                sendStatusResponse(message, new MiningStatus(0, "Nic nam nie wiadomo o takim zapytaniu"));
            }
        }
    }

    private void sendStatusResponse(final ACLMessage message, final MiningStatus miningStatus) {
        send(MiningProtocol.sendStatus.toResponse(message, codec.encode(miningStatus)));
    }

    private void registerServices() {
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5),
                MiningProtocol.checkStatus.getTargetService(),
                MiningProtocol.sendRequest.getTargetService(),
                MiningProtocol.checkStatus.getTargetService());
    }
}
