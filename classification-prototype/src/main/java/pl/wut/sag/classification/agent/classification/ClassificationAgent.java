package pl.wut.sag.classification.agent.classification;


import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import pl.wut.sag.classification.agent.classification.distance.DistanceCalculator;
import pl.wut.sag.classification.agent.classification.distance.EuclideanDistanceCalculator;
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
import java.util.Set;

public class ClassificationAgent extends Agent {

    private String className;
    private int discriminatorColumn;
    private final DistanceCalculator distanceCalculator = new EuclideanDistanceCalculator(new DoubleParser());
    private final Codec codec = Codec.json();
    private final Set<ObjectWithAttributes> trainingSet = new HashSet<>();

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
    }

    private void checkDistance(final ACLMessage message) {
        final CheckDistanceRequest request = codec.decode(message.getContent(), ClassificationProtocol.checkDistance.getMessageClass()).result();
        final ObjectWithAttributes object = request.getObjectWithAttributes();
        object.setDiscriminatorColumn(discriminatorColumn);
        final double averageDistance = distanceCalculator.calculateAverageDistance(ImmutableList.of(trainingSet), object);

        final DistanceInfo distanceInfo = new DistanceInfo(averageDistance, className);
        final ACLMessage reply = ClassificationProtocol.sendDistanceInfo.toResponse(message, codec.encode(distanceInfo));
        send(reply);
    }
}
