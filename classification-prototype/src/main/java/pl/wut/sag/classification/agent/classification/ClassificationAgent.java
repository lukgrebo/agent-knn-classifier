package pl.wut.sag.classification.agent.classification;


import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import pl.wut.sag.classification.agent.classification.distance.DistanceCalculator;
import pl.wut.sag.classification.agent.classification.distance.EuclideanDistanceCalculator;
import pl.wut.sag.classification.agent.classification.knn.DefaultKNearestNeightbours;
import pl.wut.sag.classification.agent.classification.knn.KNearestNeightbours;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.codec.Codec;
import pl.wut.sag.classification.infrastructure.collection.ImmutableList;
import pl.wut.sag.classification.infrastructure.messaging.MessageHandler;
import pl.wut.sag.classification.infrastructure.messaging.MessageSpecification;
import pl.wut.sag.classification.infrastructure.messaging.ServiceRegistration;
import pl.wut.sag.classification.infrastructure.parser.DoubleParser;
import pl.wut.sag.classification.protocol.classy.CheckDistanceRequest;
import pl.wut.sag.classification.protocol.classy.ClassificationProtocol;
import pl.wut.sag.classification.protocol.classy.DistanceInfo;
import pl.wut.sag.classification.protocol.classy.TrainingRequest;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassificationAgent extends Agent {

    private String className;
    private int discriminatorColumn;
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final KNearestNeightbours kNearestNeightbours = new DefaultKNearestNeightbours(distanceCalculator);
    private final Codec codec = Codec.json();
    private final Set<ObjectWithAttributes> trainingSet = new HashSet<>();
    private final Set<ObjectWithAttributes> negastiveSet = new HashSet<>();

    @Override
    protected void setup() {
        final ClassificationAgentDependencies dependencies = (ClassificationAgentDependencies) getArguments()[0];
        this.className = dependencies.getClassName();
        this.discriminatorColumn = dependencies.getDiscriminatorColumn();
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(1), ClassificationProtocol.classificationAgentOfClassName(className));

        addBehaviour(new MessageHandler(MessageSpecification.of(ClassificationProtocol.train.toMessageTemplate(), this::train),
                MessageSpecification.of(ClassificationProtocol.checkDistance.toMessageTemplate(), this::checkDistance)));
    }

    private void train(final ACLMessage aclMessage) {
        final TrainingRequest request = codec.decode(aclMessage.getContent(), ClassificationProtocol.train.getMessageClass()).result();
        trainingSet.addAll(request.getTrainingSet());
        negastiveSet.addAll(request.getNegativeSet());
    }

    private void checkDistance(final ACLMessage message) {
        final CheckDistanceRequest request = codec.decode(message.getContent(), ClassificationProtocol.checkDistance.getMessageClass()).result();
        final ObjectWithAttributes object = request.getObjectWithAttributes();
        object.setDiscriminatorColumn(discriminatorColumn);
        final double averagePositiveDistance = distanceCalculator.calculateAverageDistance(ImmutableList.of(trainingSet), object);
        final double averageNegativeDistance = distanceCalculator.calculateAverageDistance(ImmutableList.of(negastiveSet), object);
        final Map<String, Long> counts = kNearestNeightbours.runAndGetVotes(all(), object, 10).entrySet().stream()
                .map(e -> e.getKey().getClassname().get())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        final int positiveN = Optional.ofNullable(counts.get(className)).map(Long::intValue).orElse(0);
        final int negativeN = (int) counts.values().stream().mapToLong(i -> i).sum() - positiveN;

        final DistanceInfo distanceInfo = new DistanceInfo(averagePositiveDistance, averageNegativeDistance, positiveN, negativeN, className);
        final ACLMessage reply = ClassificationProtocol.sendDistanceInfo.toResponse(message, codec.encode(distanceInfo));
        send(reply);
    }

    private Set<ObjectWithAttributes> all() {
        return Stream.concat(negastiveSet.stream(), trainingSet.stream()).collect(Collectors.toSet());
    }
}
