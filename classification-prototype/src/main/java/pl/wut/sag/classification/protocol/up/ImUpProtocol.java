package pl.wut.sag.classification.protocol.up;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.wut.sag.classification.protocol.common.Protocol;
import pl.wut.sag.classification.protocol.common.ProtocolStep;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImUpProtocol extends Protocol {

    private final static ImUpProtocol INSTANCE = new ImUpProtocol();

    public static ProtocolStep<ImUpProtocol, AID> imUp = ProtocolStep.<ImUpProtocol, AID>builder()
            .performative(ACLMessage.INFORM)
            .messageClass(AID.class)
            .protocol(INSTANCE)
            .stepName("step")
            .build();

}
