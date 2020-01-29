package pl.wut.sag.knn.protocol.classy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequest implements Serializable {
    private String className;
    private Set<ObjectWithAttributes> objects;
}