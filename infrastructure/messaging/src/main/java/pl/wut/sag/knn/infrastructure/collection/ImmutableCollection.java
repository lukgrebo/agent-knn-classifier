package pl.wut.sag.knn.infrastructure.collection;

import java.util.stream.Stream;

public interface ImmutableCollection<T> {

    boolean contains(Object o);

    Stream<T> stream();

    int size();

}
