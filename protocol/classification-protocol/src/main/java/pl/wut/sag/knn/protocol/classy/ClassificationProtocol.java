package pl.wut.sag.knn.protocol.classy;

import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import pl.wut.sag.knn.protocol.Protocol;
import pl.wut.sag.knn.protocol.ProtocolStep;
import pl.wut.sag.knn.protocol.classy.model.CheckDistanceRequest;
import pl.wut.sag.knn.protocol.classy.model.DistanceInfo;
import pl.wut.sag.knn.protocol.classy.model.TrainingRequest;
import pl.wut.sag.knn.service.ServiceDescriptionFactory;

public class ClassificationProtocol extends Protocol {

    public static ServiceDescription agentOfClassName(final String className) {
        return ServiceDescriptionFactory.nameAndProperties("class_agent_" + className, new Property("class", className));
    }

    public static ProtocolStep<ClassificationProtocol, TrainingRequest> train = ProtocolStep.<ClassificationProtocol, TrainingRequest>builder()
            .stepName("train")
            .messageClass(TrainingRequest.class)
            .performative(ACLMessage.REQUEST)
            .required(true)
            .build();

    public static ProtocolStep<ClassificationProtocol, CheckDistanceRequest> checkDistance =
            ProtocolStep.<ClassificationProtocol, CheckDistanceRequest>builder()
                    .stepName("check distance")
                    .messageClass(CheckDistanceRequest.class)
                    .performative(ACLMessage.CFP)
                    .required(false)
                    .build();

    public static ProtocolStep<ClassificationProtocol, DistanceInfo> sendDistanceInfo =
            ProtocolStep.<ClassificationProtocol, DistanceInfo>builder()
                    .stepName("send distance")
                    .messageClass(DistanceInfo.class)
                    .performative(ACLMessage.PROPOSE)
                    .required(true)
                    .build();

}
