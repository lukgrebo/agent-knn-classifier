package pl.wut.sag.knn.agent.data.auction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.config.DataAgentConfiguration;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.ArrayDeque;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AuctionRunnerFactory {

    private final DataAgentConfiguration config;
    private final Codec codec;
    private final ServiceDiscovery serviceDiscovery;

    public AuctionRunner newRunner(final UUID requestUuid, final Set<ObjectWithAttributes> objects) {
        final ArrayDeque<ObjectWithAttributes> queue = new ArrayDeque<>(objects);
        log.info("Creatng new auction runner with {} objects to process", objects.size());

        return new DefaultAuctionRunner(requestUuid, queue, serviceDiscovery);
    }
}
