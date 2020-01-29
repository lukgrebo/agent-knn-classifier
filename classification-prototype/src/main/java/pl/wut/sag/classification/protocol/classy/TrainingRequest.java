package pl.wut.sag.classification.protocol.classy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequest {
    private String className;
    private Set<ObjectWithAttributes> trainingSet;
    private Set<ObjectWithAttributes> negativeSet;
}
