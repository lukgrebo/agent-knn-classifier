package pl.wut.sag.classification.domain.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class IndexedAttribute {
    private final int index;
    private final String value;
}
