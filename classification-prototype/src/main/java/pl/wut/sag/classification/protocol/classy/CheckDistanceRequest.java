package pl.wut.sag.classification.protocol.classy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckDistanceRequest {
    private ObjectWithAttributes objectWithAttributes;
}
