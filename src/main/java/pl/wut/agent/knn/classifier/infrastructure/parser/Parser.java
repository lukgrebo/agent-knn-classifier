package pl.wut.agent.knn.classifier.infrastructure.parser;

import pl.wut.agent.knn.classifier.infrastructure.function.Either;

public interface Parser<R> {
    Either<R, ParseError> parse(String input);
}
