package pl.wut.sag.knn.agent.data;

import jade.core.Agent;
import pl.wut.sag.knn.infrastructure.discovery.ServiceRegistration;

public class DefaultDataAgent extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new DataAuctioner());
    }
}
