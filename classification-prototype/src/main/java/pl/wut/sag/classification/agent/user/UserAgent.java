package pl.wut.sag.classification.agent.user;


import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import pl.wut.sag.classification.agent.user.interfaces.web.UserAgentWebApi;
import pl.wut.sag.classification.agent.user.interfaces.web.UserApiWebHandle;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectRequest;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectResponse;
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.codec.Codec;
import pl.wut.sag.classification.infrastructure.messaging.MessageHandler;
import pl.wut.sag.classification.infrastructure.messaging.MessageSpecification;
import pl.wut.sag.classification.infrastructure.messaging.ServiceDiscovery;
import pl.wut.sag.classification.infrastructure.messaging.ServiceRegistration;
import pl.wut.sag.classification.protocol.classy.CheckDistanceRequest;
import pl.wut.sag.classification.protocol.classy.ClassificationProtocol;
import pl.wut.sag.classification.protocol.classy.ClassificationResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class UserAgent extends Agent {
    private final Map<String, AID> dataAgentByContext = new HashMap<>();
    private final ServiceDiscovery serviceDiscovery = new ServiceDiscovery(this);
    private final DataAgentStarter dataAgentStarter = new DataAgentStarter(serviceDiscovery);
    private final Map<String, List<ClassificationResult>> resultsByContext = new HashMap<>();
    private final Codec codec = Codec.json();

    @Override
    protected void setup() {
        UserAgentWebApi.setupApi(8080, new AgentWebHandler());
        ServiceRegistration.registerRetryOnFailure(this, Duration.ofSeconds(1), ClassificationProtocol.sendResult.getTargetService());
        addBehaviour(new MessageHandler(MessageSpecification.of(ClassificationProtocol.sendResult.toMessageTemplate(), this::handleResult)));
    }

    private void handleResult(final ACLMessage message) {
        final ClassificationResult parsedResult = codec.decode(message.getContent(), ClassificationProtocol.sendResult.getMessageClass()).result();
        final AID sender = message.getSender();
        final String context = dataAgentByContext.entrySet().stream().filter(e -> e.getValue().equals(sender)).findFirst().get().getKey();
        resultsByContext.computeIfAbsent(context, __ -> new ArrayList<>()).add(parsedResult);
    }

    private class AgentWebHandler implements UserApiWebHandle {

        @Override
        public String processTrainingRequest(final OrderClassificationTrainingRequest request) {
            if (request.getContext() == null) {
                return "Należy wskazać kontekst zapytania";
            } else if (request.getDiscriminatorColumn() == null) {
                return "Należy wskazać indeks kolumny, w której znajdują się atrybuty oznaczający klasę";
            } else if (request.getTrainingSetUrl() == null) {
                return "Należy podać poprawny url zbioru treningowego";
            } else if (request.getTraningSetWeight() < 0.1 || request.getTraningSetWeight() > 1) {
                return "Wielkość wagi setu powinna być pomiędzy 0.1, a 1";
            }
            final AID aid = dataAgentByContext.computeIfAbsent(request.getContext(), dataAgentStarter::run);

            final ACLMessage message = ClassificationProtocol.orderTraining.templatedMessage();
            message.addReceiver(aid);
            message.setContent(codec.encode(request));
            UserAgent.this.send(message);

            return "Zlecono uczenie, kontekst: " + request.getContext();
        }

        @Override
        public List<String> getContexts() {
            return new ArrayList<>(dataAgentByContext.keySet());
        }

        @Override
        public String checkObjectClass(final CheckObjectRequest request) {
            if (dataAgentByContext.get(request.getContext()) == null) {
                return "NIe znaleziono kontekstu o nazwie " + request.getContext();
            }
            final ObjectWithAttributes object = ObjectWithAttributes.of(request.getAttributesByIndex(), -1);
            final ACLMessage message = ClassificationProtocol.checkDistance.templatedMessage();
            final CheckDistanceRequest checkDistanceRequest = new CheckDistanceRequest(object);
            message.setContent(codec.encode(checkDistanceRequest));
            message.addReceiver(dataAgentByContext.get(request.getContext()));
            message.setConversationId(UUID.randomUUID().toString());
            send(message);

            return codec.encode(CheckObjectResponse.valid(object.getId()));
        }

        @Override
        public String getResults(final String context) {
            return ensureContextFoundAnd(context, c -> codec.encode(resultsByContext.computeIfAbsent(c, _k -> new ArrayList<>())));
        }

        @Override
        public String getResult(final String context, final UUID id) {
            if (!dataAgentByContext.containsKey(context)) {
                return "Nie znaleziono kontektu " + context;
            }

            return resultsByContext.get(context).stream().filter(o -> o.getObject().getId().equals(id)).findFirst()
                    .map(codec::encode)
                    .orElse("Nie ma jeszcze rezultatu dla obiektu o id " + id);
        }

        @Override
        public String clearResults(final String context) {
            return ensureContextFoundAnd(context, c -> {
                Optional.ofNullable(resultsByContext.get(c)).ifPresent(List::clear);
                return "Poprawnie wyczyszczono dane kontekstu";
            });
        }

        private String ensureContextFoundAnd(final String context, Function<String, String> doWithContext) {
            if (!dataAgentByContext.containsKey(context)) {
                return "Nie znaleziono kontektu " + context;
            }
            return doWithContext.apply(context);
        }
    }
}
