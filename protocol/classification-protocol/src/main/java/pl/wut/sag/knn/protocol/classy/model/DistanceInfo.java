package pl.wut.sag.knn.protocol.classy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceInfo {
    private double averageDistanceInCluster;
    private String className;
}
