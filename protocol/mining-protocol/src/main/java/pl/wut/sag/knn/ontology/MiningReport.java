package pl.wut.sag.knn.ontology;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class MiningReport {

    private UUID requestId;
    private String reportContent;
}
