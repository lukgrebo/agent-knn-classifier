package pl.wut.sag.classification.agent.classification.knn;

import pl.wut.sag.classification.domain.object.ObjectWithAttributes;

import java.util.Map;
import java.util.Set;

public interface KNearestNeightbours {
    Map<ObjectWithAttributes, String> runAndGetVotes(final Set<ObjectWithAttributes> cluster,
                                                     final ObjectWithAttributes candidate,
                                                     int k);
}
