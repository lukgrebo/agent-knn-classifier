package pl.wut.sag.knn.infrastructure.collection;

import java.util.Collection;
import java.util.Optional;

public class CollectionUtil {

    public static <T> Optional<T> firstElement(final Collection<T> coll) {
        return coll == null || coll.isEmpty() ? Optional.empty() : Optional.ofNullable(coll.iterator().next());
    }
}
