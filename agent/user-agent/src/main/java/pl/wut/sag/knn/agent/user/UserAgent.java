package pl.wut.sag.knn.agent.user;

import jade.core.Agent;
import jade.domain.FIPAException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.user.api.UserAgentApi;
import pl.wut.sag.knn.agent.user.api.UserAgentApiHandle;
import pl.wut.sag.knn.agent.user.api.dto.MiningRequest;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.collection.CollectionUtil;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.protocol.MiningProtocol;

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
        final UUID uuid = UUID.randomUUID();

        final Result<String, FIPAException> stringFIPAExceptionResult = serviceDiscovery.findServices(MiningProtocol.sendRequest.getTargetService())
                .mapResult(CollectionUtil::firstElement)
                .mapResult(Object::toString);

        return stringFIPAExceptionResult;

    }
}
