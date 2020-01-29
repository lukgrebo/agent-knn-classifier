package pl.wut.sag.knn.agent.data.classification;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import pl.wut.sag.knn.agent.data.DataAgent;
import pl.wut.sag.knn.infrastructure.codec.Codec;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.protocol.classy.ClassificationProtocol;
import pl.wut.sag.knn.protocol.classy.model.TrainingRequest;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    private final Map<String, AID> agentByClassName = new HashMap<>();
    private final DataAgent dataAgent;
    private final ClassificationAgentStarter starter = new ClassificationAgentStarter(new ServiceDiscovery(dataAgent));
    private final Codec codec = Codec.json();

    @Override
    public void processRequest(final TrainingRequest trainingRequest) {
        final String className = trainingRequest.getClassName();
        final AID agent = agentByClassName.computeIfAbsent(className, starter::start);
        final ACLMessage message = ClassificationProtocol.train.templatedMessage();
        message.setContent(codec.encode(trainingRequest));
        message.addReceiver(agent);
        dataAgent.send(message);
    }

}
