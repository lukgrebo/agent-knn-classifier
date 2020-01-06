package pl.wut.sag.knn.agent.data;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.agent.data.loader.CsvObjectParser;
import pl.wut.sag.knn.agent.data.loader.DataLoader;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.infrastructure.parser.ParseError;
import pl.wut.sag.knn.ontology.MiningRequest;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.MiningProtocol;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

@Slf4j
public class DataAgent extends Agent {

    private final Codec codec = Codec.json();
    private final CsvObjectParser csvObjectParser = new CsvObjectParser();
    private final Queue<MiningRequest> miningRequests = new ArrayDeque<>();

    @Override
    protected void setup() {
        final AuctionManagement auctionManagement = new AuctionManagement(new DataAgentConfiguration(), codec, new ServiceDiscovery(this));
        addBehaviour(new MessageHandler(MessageSpecification.of(MiningProtocol.sendRequest.toMessageTemplate(), this::startMining)));
        registerServices();
    }

    private void startMining(final ACLMessage message) {
        final MiningRequest request = codec.decode(message.getContent(), MiningProtocol.sendRequest.getMessageClass()).result();
        log.info("Got mining request", request);

        final DataLoader dataLoader = DataLoader.defaultLoader();
        final Result<Stream<String>, IOException> result = dataLoader.openDataStream(request.getMiningUrl());
        if (result.isError()) {
            log.info("Error loading data from" + request.getMiningUrl());
            return;
        }
        final Stream<Result<ObjectWithAttributes, ParseError>> parsedEntries = result.result()
                .map(csvObjectParser::parse);

        System.out.println("xd");
    }

    private void registerServices() {
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5),
                MiningProtocol.checkStatus.getTargetService(),
                MiningProtocol.sendRequest.getTargetService());
    }
}
