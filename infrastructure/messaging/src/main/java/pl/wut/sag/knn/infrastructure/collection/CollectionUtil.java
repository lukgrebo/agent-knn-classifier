package pl.wut.sag.knn.infrastructure.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtil {

    public static <T> Optional<T> firstElement(final Collection<T> coll) {
        return coll == null || coll.isEmpty() ? Optional.empty() : Optional.ofNullable(coll.iterator().next());
    }

    public static <T, R> Set<R> mapToSet(final Collection<T> coll, final Function<T, R> mapper) {
        return coll == null || coll.isEmpty() ? Collections.emptySet() : coll.stream().map(mapper).collect(Collectors.toSet());
    }
}
