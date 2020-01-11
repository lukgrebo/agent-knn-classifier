package pl.wut.sag.knn.agent.user;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.user.api.UserAgentApi;
import pl.wut.sag.knn.agent.user.api.UserAgentApiHandle;
import pl.wut.sag.knn.agent.user.api.dto.MiningRequest;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.collection.CollectionUtil;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.ontology.MiningRequestType;
import pl.wut.sag.knn.protocol.MiningProtocol;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class UserAgent extends Agent implements UserAgentApiHandle {

    private final Codec codec = Codec.json();
    private final ServiceDiscovery serviceDiscovery = new ServiceDiscovery(this);

    @Override
    protected void setup() {
        final UserAgentDependencies dependencies = (UserAgentDependencies) getArguments()[0];
        log.info("Running user agent");
        UserAgentApi.start(dependencies.getApiConfig(), this, codec);

    }

    @Override
    public Result<String, ? extends Exception> processMiningRequest(final MiningRequest miningRequest) {
        log.info("Got mining request to process {}", miningRequest);

        return serviceDiscovery.findServices(MiningProtocol.sendRequest.getTargetService())
                .mapResult(CollectionUtil::firstElement)
                .mapResult(x -> sendRequest(x, miningRequest));
    }

    private String sendRequest(final Optional<DFAgentDescription> agentDescription, final MiningRequest miningRequest) {
        if (!agentDescription.isPresent()) {
            return "Nie znaleziono agenta danych gotowego zrealizować żądanie";
        }

        final pl.wut.sag.knn.ontology.MiningRequest request = mapMiningRequest(miningRequest);
        final ACLMessage message = MiningProtocol.sendRequest.templatedMessage();
        message.addReceiver(agentDescription.get().getName());
        message.setContent(codec.encode(request));
        send(message);

        return "Wysłano zlecenie do agenta danych, id zlecenia: " + request.getRequestId();
    }

    private pl.wut.sag.knn.ontology.MiningRequest mapMiningRequest(final MiningRequest rq) {
        return new pl.wut.sag.knn.ontology.MiningRequest(UUID.randomUUID(), rq.getMiningUrl(), MiningRequestType.URL, rq.getMinimalBid(), rq.getRefinementSize());

    }
}
