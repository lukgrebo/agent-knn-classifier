package pl.wut.sag.knn.agent.clustering.config;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;
import lombok.experimental.Accessors;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

@Getter
@Accessors(fluent = true)
public class MessageTemplates {

    private final MessageTemplate biddingOfferTemplate;
    private final MessageTemplate offerAcceptationTemplate;

    public MessageTemplates(final ClusteringAgentConfig config) {
        this.biddingOfferTemplate = and(MatchPerformative(ACLMessage.CFP), MatchOntology(config.objectMarketOntology()));
        this.offerAcceptationTemplate = and(MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MatchOntology(config.objectMarketOntology()));
    }
}
