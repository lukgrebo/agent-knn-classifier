package pl.wut.sag.knn.infrastructure;

import jade.lang.acl.ACLMessage;

public interface MessageSender {
    void send(ACLMessage aclMessage);
}
