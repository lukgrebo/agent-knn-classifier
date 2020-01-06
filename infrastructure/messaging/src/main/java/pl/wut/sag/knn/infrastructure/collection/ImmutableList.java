package pl.wut.sag.knn.infrastructure.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ImmutableList<T> implements ImmutableCollection<T> {

    private final List<T> internalList;
    private final int size;

    public static <T> ImmutableList<T> of(final Collection<T> elements) {
        return new ImmutableList<>(elements);
    }

    private ImmutableList(final Collection<T> elements) {
        this.internalList = Collections.unmodifiableList(new ArrayList<>(elements));
        this.size = this.internalList.size();
    }


    @Override
    public boolean contains(final Object o) {
        return internalList.contains(o);
    }

    @Override
    public Stream<T> stream() {
        return internalList.stream();
    }

    @Override
    public int size() {
        return size;
    }
}
