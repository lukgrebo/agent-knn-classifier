package pl.wut.sag.knn.agent.user.api.dto;

import lombok.Data;

import java.net.URL;

@Data
public class MiningRequest {
    private URL miningUrl;
    private double minimalBid;
    private Integer refinementSize;
    private Integer discriminatorColumn;
}
