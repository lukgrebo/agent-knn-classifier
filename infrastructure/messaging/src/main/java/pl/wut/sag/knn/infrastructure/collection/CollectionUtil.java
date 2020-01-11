package pl.wut.sag.knn.infrastructure.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtil {

    public static <T> Optional<T> firstElement(final Collection<T> coll) {
        return coll == null || coll.isEmpty() ? Optional.empty() : Optional.ofNullable(coll.iterator().next());
    }

    public static <T, R> Set<R> mapToSet(final Collection<T> coll, final Function<T, R> mapper) {
        return coll == null || coll.isEmpty() ? Collections.emptySet() : coll.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();

        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static <T> Set<T> mergeToSet(final Collection<T> t1, final Collection<T> t2) {
        return Stream.concat(t1.stream(), t2.stream()).collect(Collectors.toSet());
    }

    public static <C extends Collection<T>, T> C initializedMutableCollection(final Supplier<C> sup, final T... elements) {
        final C coll = sup.get();
        coll.addAll(Arrays.asList(elements));

        return coll;
    }

}
