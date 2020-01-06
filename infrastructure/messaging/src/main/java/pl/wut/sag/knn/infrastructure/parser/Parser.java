package pl.wut.sag.knn.infrastructure.parser;


import pl.wut.sag.knn.infrastructure.function.Result;

public interface Parser<R> {
    Result<R, ParseError> parse(String input);
}
