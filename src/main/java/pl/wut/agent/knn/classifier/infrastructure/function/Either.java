package pl.wut.agent.knn.classifier.infrastructure.function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Either<L, R> {

    @Getter
    private final L left;

    @Getter
    private final R right;

    public static <L, R> Either<L, R> left(final L left) {
        return new Either<>(Objects.requireNonNull(left), null);
    }

    public static <L, R> Either<L, R> right(final R right) {
        return new Either<>(null, Objects.requireNonNull(right));
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

}
