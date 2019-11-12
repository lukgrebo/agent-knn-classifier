package pl.wut.agent.knn.classifier;

import jade.Boot;
import pl.wut.agent.knn.classifier.agent.clustering.DefaultClusteringAgent;

public class AgentClassifierApplication {


    public static void main(final String[] args) {
        final String clusteringAgentArgument = "Clustering:" + DefaultClusteringAgent.class.getCanonicalName();
        Boot.main(new String[]{"-gui", "-agents", clusteringAgentArgument});
    }
}
