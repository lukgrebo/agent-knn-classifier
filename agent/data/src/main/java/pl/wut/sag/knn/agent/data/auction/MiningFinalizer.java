package pl.wut.sag.knn.agent.data.auction;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.agent.data.DataAgent;
import pl.wut.sag.knn.agent.data.model.ClusterSummaryWithObjects;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.collection.CollectionUtil;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.infrastructure.message_handler.IMessageSpecification;
import pl.wut.sag.knn.infrastructure.message_handler.MessageSpecification;
import pl.wut.sag.knn.ontology.auction.ClusterSummaryRequest;
import pl.wut.sag.knn.ontology.auction.StartRefinementRequest;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.auction.AuctionProtocol;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface MiningFinalizer {
    void finalizeMining();

    static DefaultMiningFinalizer finalizer(final URL miningUrl, final ServiceDiscovery serviceDiscovery, final DataAgent dataAgent, final Codec codec, Map<UUID, ObjectWithAttributes> allObjects) {
        return new DefaultMiningFinalizer(miningUrl, serviceDiscovery, dataAgent, codec, allObjects);
    }
}

@Slf4j
@RequiredArgsConstructor
class DefaultMiningFinalizer implements MiningFinalizer {

    private final URL miningUrl;
    private final ServiceDiscovery serviceDiscovery;
    private final DataAgent dataAgent;
    private final Codec codec;
    private final Map<UUID, ObjectWithAttributes> allObjects;
    private final ReportGenerator reportGenerator = ReportGenerator.stringToConsole();

    @Override
    public void finalizeMining() {
        log.info("Starting finalization of all, sending requests to clustering agents");
        final List<DFAgentDescription> result = serviceDiscovery.findServices(AuctionProtocol.requestSummary.getTargetService()).result();
        log.info("Sending cluster summary request to {} agents", result.size());
        final ACLMessage message = AuctionProtocol.requestSummary.templatedMessage();
        message.setContent(codec.encode(ClusterSummaryRequest.ofRandomUUID()));
        result.stream().map(DFAgentDescription::getName).forEach(message::addReceiver);

        log.info("Registering response handler");
        dataAgent.messageHandler.add(new IMessageSpecification() {
            final AuctionState auctionState = AuctionState.newState(result.size());

            @Override
            public MessageTemplate getTemplateToMatch() {
                return AuctionProtocol.summaryResponse.toMessageTemplate();
            }

            @Override
            public void processMessage(final ACLMessage msg) {
                auctionState.register(msg.getSender(), codec.decode(msg.getContent(), AuctionProtocol.summaryResponse.getMessageClass()).result());
                if (auctionState.isGatheringFinished() && !auctionState.isRefinementStarted()) {
                    final List<ClusterSummaryWithObjects> clusterSummaryWithObjects =
                            auctionState.getSummary().entrySet().stream()
                                    .map(e -> new ClusterSummaryWithObjects(e.getKey(),
                                            e.getValue().getAverageDistance(),
                                            CollectionUtil.mapToSet(e.getValue().getObjectsIds(), allObjects::get)))
                                    .collect(Collectors.toList());
                    reportGenerator.generate(miningUrl, clusterSummaryWithObjects);

                    final ACLMessage startRefinementMsg = AuctionProtocol.startRefinementRequest.templatedMessage();
                    serviceDiscovery.findServices(AuctionProtocol.startRefinementRequest.getTargetService()).result().stream()
                            .map(DFAgentDescription::getName)
                            .forEach(startRefinementMsg::addReceiver);
                    startRefinementMsg.setContent(codec.encode(new StartRefinementRequest(UUID.randomUUID())));

                    dataAgent.messageHandler.add(MessageSpecification.of(
                            AuctionProtocol.refinementFinishedResponse.toMessageTemplate(),
                            msage -> {
                                auctionState.refinementFinished(msage.getSender());
                                if (auctionState.isRefinementDone()) {
                                    log.info("REFINEMENT IS DONE");
                                }
                            })
                    );
                    auctionState.startRefinement();
                    dataAgent.send(startRefinementMsg);
                }
            }

        });

        dataAgent.send(message);
        log.info("Message send!");
    }

}
