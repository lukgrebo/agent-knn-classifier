package pl.wut.sag.knn.protocol.auction;

import jade.lang.acl.ACLMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.Protocol;
import pl.wut.sag.knn.protocol.ProtocolStep;
import pl.wut.sag.knn.protocol.ResponseStep;
import pl.wut.sag.knn.protocol.TargetedStep;
import pl.wut.sag.knn.service.ServiceDescriptionFactory;


/**
 * Basic auction protocol.
 * Consisting o 3 steps. Steps don't need to be repeated.
 * Steps are execute only once because bid values are indicators of how much object fits certain cluster.
 * Step 3 is optional and is execute only if highest bid is satisfactory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionProtocol extends Protocol {

    private static final AuctionProtocol instance = new AuctionProtocol();

    public static final TargetedStep<AuctionProtocol, ObjectWithAttributes> proposeObject =
            TargetedStep.<AuctionProtocol, ObjectWithAttributes>targetedBuilder()
                    .performative(ACLMessage.CFP)
                    .required(true)
                    .protocol(instance)
                    .messageClass(ObjectWithAttributes.class)
                    .stepName("Propose object")
                    .targetService(ServiceDescriptionFactory.name("clustering-service"))
                    .build();

    public static final ResponseStep<AuctionProtocol, Bid> sendBid = ResponseStep.<AuctionProtocol, Bid>responseStepBuilder()
            .performative(ACLMessage.PROPOSE)
            .required(true)
            .protocol(instance)
            .messageClass(Bid.class)
            .stepName("Send bid")
            .build();

    public static final ProtocolStep<AuctionProtocol, ObjectWithAttributes> acceptBidAndSendObject =
            ProtocolStep.<AuctionProtocol, ObjectWithAttributes>builder()
                    .performative(ACLMessage.ACCEPT_PROPOSAL)
                    .required(false)
                    .protocol(instance)
                    .messageClass(ObjectWithAttributes.class)
                    .stepName("Accept bid and send object")
                    .build();

    public static final TargetedStep<AuctionProtocol, ClusterSummary> requestSummary =
            TargetedStep.<AuctionProtocol, ClusterSummary>targetedBuilder()
                    .performative(ACLMessage.REQUEST)
                    .required(false)
                    .protocol(instance)
                    .messageClass(ClusterSummary.class)
                    .stepName("Request cluster summary")
                    .targetService(ServiceDescriptionFactory.name("auction-summary-provider"))
                    .build();


}
