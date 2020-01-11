package pl.wut.sag.knn.protocol.auction;

import jade.lang.acl.ACLMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.wut.sag.knn.ontology.auction.Bid;
import pl.wut.sag.knn.ontology.auction.ClusterSummary;
import pl.wut.sag.knn.ontology.auction.ClusterSummaryRequest;
import pl.wut.sag.knn.ontology.auction.RefinementSummary;
import pl.wut.sag.knn.ontology.auction.StartRefinementRequest;
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

    public static final TargetedStep<AuctionProtocol, StartRefinementRequest> startRefinementRequest =
            TargetedStep.<AuctionProtocol, StartRefinementRequest>targetedBuilder()
                    .performative(ACLMessage.REQUEST)
                    .required(false)
                    .protocol(instance)
                    .messageClass(StartRefinementRequest.class)
                    .stepName("Start refinement")
                    .targetService(ServiceDescriptionFactory.name("refinement-capable"))
                    .build();

    public static final ResponseStep<AuctionProtocol, RefinementSummary> refinementFinishedResponse =
            ResponseStep.<AuctionProtocol, RefinementSummary>responseStepBuilder()
                    .performative(ACLMessage.INFORM)
                    .required(false)
                    .protocol(instance)
                    .messageClass(RefinementSummary.class)
                    .stepName("Inform of finished refinement")
                    .build();

    public static final TargetedStep<AuctionProtocol, ClusterSummaryRequest> requestSummary =
            TargetedStep.<AuctionProtocol, ClusterSummaryRequest>targetedBuilder()
                    .performative(ACLMessage.REQUEST)
                    .required(false)
                    .protocol(instance)
                    .messageClass(ClusterSummaryRequest.class)
                    .stepName("Request cluster summary")
                    .targetService(ServiceDescriptionFactory.name("auction-summary-provider"))
                    .build();

    public static final ResponseStep<AuctionProtocol, ClusterSummary> summaryResponse =
            ResponseStep.<AuctionProtocol, ClusterSummary>responseStepBuilder()
                    .performative(ACLMessage.INFORM)
                    .required(false)
                    .protocol(instance)
                    .messageClass(ClusterSummary.class)
                    .stepName("Respond with cluster summary")
                    .build();

}
