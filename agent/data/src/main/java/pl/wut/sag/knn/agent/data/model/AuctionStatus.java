package pl.wut.sag.knn.agent.data.model;

import lombok.Value;

import java.util.UUID;

@Value
public class AuctionStatus {

    private UUID uuid;
    private int participants;
    private int objectsLeft;
    private boolean finished;
}
