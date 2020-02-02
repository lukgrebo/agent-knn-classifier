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
import pl.wut.sag.classification.protocol.up.ImUpProtocol;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class DataAgent extends Agent {

    private String context;
    private AID userAgent;
    private final int agentsPerClass = 4;
    private final Map<String, List<AID>> classificationAgentByClass = new HashMap<>();
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
        final DataAgentDependencies dependencies = (DataAgentDependencies) getArguments()[0];
        this.context = dependencies.getContext();
        this.userAgent = dependencies.getUserAgent();

        addBehaviour(messageHandler);

        this.classificatorsContainer = new AgentStartupManager().startChildContainer(AgentStartupInfo.withDefaults("classificator-container-" + context));

        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(5), ClassificationProtocol.dataAgentOfContext(context));
        final ACLMessage imUpMessage = ImUpProtocol.imUp.templatedMessage();
        imUpMessage.addReceiver(userAgent);
        send(imUpMessage);
    }

    private void runTraining(final ACLMessage message) {
        final OrderClassificationTrainingRequest request = codec.decode(message.getContent(), ClassificationProtocol.orderTraining.getMessageClass()).result();
        log.info("Got training message: {}", message.getContent());

        log.info("Loading data from {}", request.getTrainingSetUrl());
        final List<ObjectWithAttributes> objects = dataLoader.getData(request.getTrainingSetUrl()).getOrThrow(RuntimeException::new).stream()
                .map(s -> csvObjectParser.parse(s, request.getDiscriminatorColumn()))
                .filter(Result::isValid)
                .map(Result::result)
                .collect(Collectors.toList());
        log.info("Data loading done");

        final Map<String, Set<ObjectWithAttributes>> objectByClass = objects.stream()
                .filter(o -> o.getAsString(request.getDiscriminatorColumn()).isPresent())
                .collect(Collectors.groupingBy(o -> o.getAsString(request.getDiscriminatorColumn()).get(), Collectors.toSet()));
        final int totalSize = objectByClass.values().stream().mapToInt(Set::size).sum();

        objectByClass.forEach((className, set) -> {
            final Set<ObjectWithAttributes> negativeSet = objectByClass.keySet().stream()
                    .filter(key -> !key.equals(className))
                    .map(objectByClass::get)
                    .flatMap(Collection::stream)
                    .limit((int) ((totalSize - set.size()) * request.getTraningSetWeight()))
                    .collect(Collectors.toSet());

            final List<AID> agents = classificationAgentByClass.computeIfAbsent(className, k -> IntStream.range(0, agentsPerClass).mapToObj(i -> createClassificationAgent(className, request.getDiscriminatorColumn(), i))
                    .collect(Collectors.toList()));
            for (final AID agent : agents) {
                final ACLMessage msg = ClassificationProtocol.train.templatedMessage();
                msg.addReceiver(agent);
                msg.setContent(codec.encode(new TrainingRequest(className, randomChoice(set, 0.7), randomChoice(negativeSet, request.getTraningSetWeight()))));
                this.send(msg);
            }
        });
    }

    private void checkDistance(final ACLMessage message) {
        message.clearAllReceiver();
        message.setSender(getAID());
        classificationAgentByClass.values().stream().flatMap(Collection::stream).forEach(message::addReceiver);
        final CheckDistanceRequest originalRequest = codec.decode(message.getContent(), ClassificationProtocol.checkDistance.getMessageClass()).result();

        final List<AID> allAgents = classificationAgentByClass.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        WaitUntilAllRespondedMessageSpecification.complexWithRegisterAndDeregister(
                new HashSet<>(allAgents),
                message,
                messageHandler,
                r -> codec.decode(r.getContent(), DistanceInfo.class).result(),
                responses -> handleAgentResponses(responses, originalRequest)
        );
        this.send(message);
    }

    private AID createClassificationAgent(final String className, final int discriminatorColumn, final int index) {
        return starter.run(new ClassificationAgentDependencies(className, discriminatorColumn, index), classificatorsContainer);
    }

    private void handleAgentResponses(final Map<AID, DistanceInfo> responses, final CheckDistanceRequest originalRequest) {
        final Map<String, List<DistanceInfo>> distanceByClass = responses.values().stream()
                .collect(Collectors.groupingBy(DistanceInfo::getClassName));

        final ClassificationResult result = ClassificationResult.of(originalRequest.getObjectWithAttributes(), average(distanceByClass));
        final ACLMessage message = ClassificationProtocol.sendResult.templatedMessage();
        discovery.findServices(ClassificationProtocol.sendResult.getTargetService()).result().stream()
                .map(DFAgentDescription::getName)
                .forEach(message::addReceiver);
        message.setContent(codec.encode(result));

        send(message);
    }

    final Map<String, DistanceInfo> average(final Map<String, List<DistanceInfo>> distancesByClass) {
        return distancesByClass.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> summary(e.getValue()))
        );
    }

    private DistanceInfo summary(List<DistanceInfo> distances) {
        final int positiveSum = distances.stream().mapToInt(DistanceInfo::getNNeighboursPositive).sum();
        final int negativeSum = distances.stream().mapToInt(DistanceInfo::getNNeightboursNegative).sum();
        final double averagePositive = distances.stream().mapToDouble(DistanceInfo::getAveragePositiveDistance).average().getAsDouble();
        final double averageNegative = distances.stream().mapToDouble(DistanceInfo::getAverageNegativeDistance).average().getAsDouble();

        return new DistanceInfo(averagePositive, averageNegative, positiveSum, negativeSum, distances.get(0).getClassName());
    }

    private <T> Set<T> randomChoice(final Collection<T> collection, final double percentage) {
        final List<T> original = new ArrayList<>(collection);
        final int size = (int)(percentage * original.size());
        final Random rand = new Random();
        return IntStream.range(1, size).map(rand::nextInt).mapToObj(original::get).collect(Collectors.toSet());
    }
}
