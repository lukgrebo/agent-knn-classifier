package pl.wut.sag.knn.ontology.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class TraningSetEntry {
    private final String classification;
    private final ObjectWithAttributes object;
}
