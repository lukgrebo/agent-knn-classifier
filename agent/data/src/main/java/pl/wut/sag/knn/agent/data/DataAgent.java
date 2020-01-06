package pl.wut.sag.knn.agent.data;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.ontology.MiningRequest;
import pl.wut.sag.knn.protocol.MiningProtocol;

import java.time.Duration;

@Slf4j
public class DataAgent extends Agent {

    private final Codec codec = Codec.json();

    @Override
    protected void setup() {
        final BidHandler bidHandler = new BidHandler(new DataAgentConfiguration(), codec, new ServiceDiscovery(this));
        addBehaviour(new MessageHandler(MessageSpecification.of(MiningProtocol.sendRequest.toMessageTemplate(), this::startMining)));
        registerServices();
    }

    private void startMining(ACLMessage message) {
        final MiningRequest request = codec.decode(message.getContent(), MiningProtocol.sendRequest.getMessageClass()).result();
        log.info("Got mining request", request);


    }

    private void registerServices() {
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5),
                MiningProtocol.checkStatus.getTargetService(),
                MiningProtocol.sendRequest.getTargetService());
    }
}
