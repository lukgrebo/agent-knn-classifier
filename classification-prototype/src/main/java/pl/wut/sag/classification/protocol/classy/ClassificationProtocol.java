package pl.wut.sag.classification.protocol.classy;

import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest;
import pl.wut.sag.classification.infrastructure.messaging.ServiceDescriptionFactory;
import pl.wut.sag.classification.protocol.common.Protocol;
import pl.wut.sag.classification.protocol.common.ProtocolStep;
import pl.wut.sag.classification.protocol.common.ResponseStep;
import pl.wut.sag.classification.protocol.common.TargetedStep;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassificationProtocol extends Protocol {

    private static ClassificationProtocol INSTANCE = new ClassificationProtocol();

    public static ServiceDescription classificationAgentOfClassName(final String className) {
        return ServiceDescriptionFactory.nameAndProperties("class_agent_" + className, new Property("class", className));
    }

    public static ServiceDescription dataAgentOfContext(final String context) {
        return ServiceDescriptionFactory.nameAndProperties("data_agent_" + context, new Property("context", context));
    }

    public static ProtocolStep<ClassificationProtocol, OrderClassificationTrainingRequest> orderTraining =
            ProtocolStep.<ClassificationProtocol, OrderClassificationTrainingRequest>builder()
                    .protocol(INSTANCE)
                    .stepName("Order training")
                    .messageClass(OrderClassificationTrainingRequest.class)
                    .performative(ACLMessage.REQUEST)
                    .required(true)
                    .build();

    public static ProtocolStep<ClassificationProtocol, TrainingRequest> train = ProtocolStep.<ClassificationProtocol, TrainingRequest>builder()
            .protocol(INSTANCE)
            .stepName("train")
            .messageClass(TrainingRequest.class)
            .performative(ACLMessage.REQUEST)
            .required(true)
            .build();

    public static ProtocolStep<ClassificationProtocol, CheckDistanceRequest> checkDistance =
            ProtocolStep.<ClassificationProtocol, CheckDistanceRequest>builder()
                    .protocol(INSTANCE)
                    .stepName("check distance")
                    .messageClass(CheckDistanceRequest.class)
                    .performative(ACLMessage.CFP)
                    .required(false)
                    .build();

    public static ResponseStep<ClassificationProtocol, DistanceInfo> sendDistanceInfo =
            ResponseStep.<ClassificationProtocol, DistanceInfo>responseStepBuilder()
                    .protocol(INSTANCE)
                    .stepName("send distance")
                    .messageClass(DistanceInfo.class)
                    .performative(ACLMessage.PROPOSE)
                    .required(true)
                    .build();

    public static TargetedStep<ClassificationProtocol, ClassificationResult> sendResult =
            TargetedStep.<ClassificationProtocol, ClassificationResult>targetedBuilder()
                    .protocol(INSTANCE)
                    .stepName("send result")
                    .messageClass(ClassificationResult.class)
                    .performative(ACLMessage.INFORM)
                    .targetService(ServiceDescriptionFactory.name("result-gatherer"))
                    .required(false)
                    .build();
}
