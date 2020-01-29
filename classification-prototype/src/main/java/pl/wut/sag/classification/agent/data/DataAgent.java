package pl.wut.sag.classification.agent.data;


import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.classification.agent.classification.ClassificationAgentDependencies;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.codec.Codec;
import pl.wut.sag.classification.infrastructure.dataLoader.CsvObjectParser;
import pl.wut.sag.classification.infrastructure.dataLoader.DataLoader;
import pl.wut.sag.classification.infrastructure.function.Result;
import pl.wut.sag.classification.infrastructure.messaging.MessageHandler;
import pl.wut.sag.classification.infrastructure.messaging.MessageSpecification;
import pl.wut.sag.classification.infrastructure.messaging.ServiceDiscovery;
import pl.wut.sag.classification.infrastructure.messaging.ServiceRegistration;
import pl.wut.sag.classification.infrastructure.messaging.WaitUntilAllRespondedMessageSpecification;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupInfo;
import pl.wut.sag.classification.infrastructure.startup.AgentStartupManager;
import pl.wut.sag.classification.protocol.classy.CheckDistanceRequest;
import pl.wut.sag.classification.protocol.classy.ClassificationProtocol;
import pl.wut.sag.classification.protocol.classy.ClassificationResult;
import pl.wut.sag.classification.protocol.classy.DistanceInfo;
import pl.wut.sag.classification.protocol.classy.TrainingRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DataAgent extends Agent {

    private String context;
    private final Map<String, AID> classificationAgentByClass = new HashMap<>();
    private final DataLoader dataLoader = DataLoader.defaultLoader();
    private final Codec codec = Codec.json();
    private final CsvObjectParser csvObjectParser = new CsvObjectParser();
    private final ClassificationAgentStarter starter = new ClassificationAgentStarter(new ServiceDiscovery(this));
    private final MessageHandler messageHandler = new MessageHandler(
            MessageSpecification.of(ClassificationProtocol.orderTraining.toMessageTemplate(), this::runTraining),
            MessageSpecification.of(ClassificationProtocol.checkDistance.toMessageTemplate(), this::checkDistance)
    );
    private AgentContainer classificatorsContainer;
    private final ServiceDiscovery discovery = new ServiceDiscovery(this);

    @Override
    protected void setup() {
        this.context = (String) getArguments()[0];

        addBehaviour(messageHandler);

        this.classificatorsContainer = new AgentStartupManager().startChildContainer(AgentStartupInfo.withDefaults("classificator-container-" + context));

        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5), ClassificationProtocol.dataAgentOfContext(context));
    }

    private void runTraining(final ACLMessage message) {
        log.info("Got training message: {}", message.getContent());
        final OrderClassificationTrainingRequest request = codec.decode(message.getContent(), ClassificationProtocol.orderTraining.getMessageClass()).result();
        final List<ObjectWithAttributes> objects = dataLoader.getData(request.getTrainingSetUrl()).getOrThrow(RuntimeException::new).stream()
                .map(s -> csvObjectParser.parse(s, request.getDiscriminatorColumn()))
                .filter(Result::isValid)
                .map(Result::result)
                .collect(Collectors.toList());

        final Map<String, Set<ObjectWithAttributes>> objectByClass = objects.stream()
                .filter(o -> o.getAsString(request.getDiscriminatorColumn()).isPresent())
                .collect(Collectors.groupingBy(o -> o.getAsString(request.getDiscriminatorColumn()).get(), Collectors.toSet()));


        objectByClass.forEach((className, set) -> {

            final AID agent = classificationAgentByClass.computeIfAbsent(className, c -> createClassificationAgent(c, request.getDiscriminatorColumn()));
            final ACLMessage msg = ClassificationProtocol.train.templatedMessage();
            msg.addReceiver(agent);
            msg.setContent(codec.encode(new TrainingRequest(className, set)));
            this.send(msg);
        });
    }

    private void checkDistance(final ACLMessage message) {
        message.clearAllReceiver();
        message.setSender(getAID());
        classificationAgentByClass.values().forEach(message::addReceiver);
        final CheckDistanceRequest originalRequest = codec.decode(message.getContent(), ClassificationProtocol.checkDistance.getMessageClass()).result();

        WaitUntilAllRespondedMessageSpecification.complexWithRegisterAndDeregister(
                new HashSet<>(classificationAgentByClass.values()),
                message,
                messageHandler,
                r -> codec.decode(r.getContent(), DistanceInfo.class).result(),
                responses -> handleAgentResponses(responses, originalRequest)
        );
        this.send(message);
    }

    private AID createClassificationAgent(final String className, final int discriminatorColumn) {
        return starter.run(new ClassificationAgentDependencies(className, discriminatorColumn), classificatorsContainer);
    }

    private void handleAgentResponses(final Map<AID, DistanceInfo> responses, final CheckDistanceRequest originalRequest) {
        final Map<String, Double> distanceByClass = responses.values().stream()
                .collect(Collectors.toMap(DistanceInfo::getClassName, DistanceInfo::getAveragePositiveDistance));
        final ClassificationResult result = ClassificationResult.of(originalRequest.getObjectWithAttributes(), distanceByClass);
        final ACLMessage message = ClassificationProtocol.sendResult.templatedMessage();
        discovery.findServices(ClassificationProtocol.sendResult.getTargetService()).result().stream()
                .map(DFAgentDescription::getName)
                .forEach(message::addReceiver);
        message.setContent(codec.encode(result));

        send(message);
    }

}
