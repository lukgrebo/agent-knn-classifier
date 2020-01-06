package pl.wut.sag.knn.ontology.auction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@ToString
@Getter
@RequiredArgsConstructor(staticName = "from")
public class Bid implements Serializable {
    private final double value;
    private final UUID objectUuid;
}

