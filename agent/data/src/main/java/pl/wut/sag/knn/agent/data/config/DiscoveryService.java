package pl.wut.sag.knn.agent.data.config;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.extern.slf4j.Slf4j;
import pl.wut.sag.knn.infrastructure.function.Result;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DiscoveryService {

    public static Result<List<DFAgentDescription>, FIPAException> search(final Agent agent, final ServiceDescription serviceDescription) {
        final DFAgentDescription description = new DFAgentDescription();
        description.addServices(serviceDescription);
        try {
            return Result.ok(Arrays.asList(DFService.search(agent, description)));
        } catch (final FIPAException e) {
            return Result.error(e);
        }
    }
}
