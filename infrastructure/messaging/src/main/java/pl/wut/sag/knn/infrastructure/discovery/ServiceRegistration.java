package pl.wut.sag.knn.infrastructure.discovery;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.infrastructure.function.Result;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ServiceRegistration {

    public static Optional<FIPAException> register(final Agent agent, final ServiceDescription... serviceDescriptions) {
        final DFAgentDescription description = new DFAgentDescription();
        description.setName(agent.getAID());
        final List<ServiceDescription> services = Arrays.asList(serviceDescriptions);
        services.forEach(description::addServices);

        try {
            DFService.register(agent, description);
            log.info("Properly registered agent {} to service registry", agent.getAID());
            return Optional.empty();
        } catch (final FIPAException e) {
            return Optional.of(e);
        }
    }

    public static void registerRetryOnFailure(final Agent agent, final Duration retryInterval, final ServiceDescription... serviceDescriptions) {
        final Optional<FIPAException> maybeException = register(agent, serviceDescriptions);
        if (maybeException.isPresent()) {
            log.error("Could not register to whitepages, retry in: " + retryInterval, maybeException.get());
            agent.addBehaviour(new WakerBehaviour(agent, retryInterval.toMillis()) {
                @Override
                protected void onWake() {
                    registerRetryOnFailure(agent, retryInterval, serviceDescriptions);
                }
            });
        }
    }

    public static Result<Void, FIPAException> deregister(final Agent agent) {
        try {
            DFService.deregister(agent);
            return Result.empty();
        } catch (FIPAException e) {
            return Result.error(e);
        }
    }
}
