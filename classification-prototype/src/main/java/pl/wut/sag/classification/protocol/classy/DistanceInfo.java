package pl.wut.sag.classification.protocol.classy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceInfo {
    private double averagePositiveDistance;
    private double averageNegativeDistance;
    private int nNeighboursPositive;
    private int nNeightboursNegative;
    private String className;
}
