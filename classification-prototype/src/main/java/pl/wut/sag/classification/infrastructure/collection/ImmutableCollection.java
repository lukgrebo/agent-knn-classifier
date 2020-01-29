package pl.wut.sag.classification.infrastructure.collection;

import java.util.stream.Stream;

public interface ImmutableCollection<T> {

    boolean contains(Object o);

    default boolean isEmpty() {
        return size() == 0;
    }

    Stream<T> stream();

    int size();

}
