package pl.wut.sag.knn.protocol;

import jade.lang.acl.ACLMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.wut.sag.knn.ontology.MiningRequest;
import pl.wut.sag.knn.ontology.MiningStatus;
import pl.wut.sag.knn.service.ServiceDescriptionFactory;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MiningProtocol extends Protocol {
    private static final MiningProtocol instance = new MiningProtocol();

    public static TargetedStep<MiningProtocol, MiningRequest> sendRequest = TargetedStep.<MiningProtocol, MiningRequest>targetedBuilder()
            .performative(ACLMessage.REQUEST)
            .messageClass(MiningRequest.class)
            .protocol(instance)
            .targetService(ServiceDescriptionFactory.name("data-miner"))
            .stepName("Send mining request")
            .required(true)
            .build();

    public static TargetedStep<MiningProtocol, UUID> checkStatus = TargetedStep.<MiningProtocol, UUID>targetedBuilder()
            .performative(ACLMessage.QUERY_IF)
            .messageClass(UUID.class)
            .protocol(instance)
            .targetService(ServiceDescriptionFactory.name("mining-info-provider"))
            .stepName("Check mining status")
            .required(false)
            .build();

    public static ResponseStep<MiningProtocol, MiningStatus> sendStatus = ResponseStep.<MiningProtocol, MiningStatus>responseStepBuilder()
            .performative(ACLMessage.INFORM)
            .messageClass(MiningStatus.class)
            .protocol(instance)
            .stepName("Send mining status")
            .required(false)
            .build();
}
