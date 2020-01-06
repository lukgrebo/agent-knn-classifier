package pl.wut.sag.knn.agent.data;

import jade.core.Agent;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;
import pl.wut.sag.knn.infrastructure.message_handler.MessageHandler;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.protocol.MiningProtocol;

import java.time.Duration;

public class DefaultDataAgent extends Agent {

    @Override
    protected void setup() {
        final BidHandler bidHandler = new BidHandler(new DataAgentConfiguration(), Codec.json(), new ServiceDiscovery(this));
        addBehaviour(new MessageHandler(MessageSpecification.of(MiningProtocol.sendRequest.toMessageTemplate(), bidHandler::handleBid)));
        registerServices();
    }

    private void registerServices() {
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5),
                MiningProtocol.checkStatus.getTargetService(),
                MiningProtocol.sendRequest.getTargetService());
    }
}
