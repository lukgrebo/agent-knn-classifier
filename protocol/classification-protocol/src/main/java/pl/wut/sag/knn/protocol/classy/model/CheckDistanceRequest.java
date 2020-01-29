package pl.wut.sag.knn.protocol.classy.model;

import lombok.Data;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

@Data
public class CheckDistanceRequest {
    private ObjectWithAttributes objectWithAttributes;
}
