package pl.wut.sag.classification.infrastructure.parser;


import pl.wut.sag.classification.infrastructure.function.Result;

public interface Parser<R> {
    Result<R, ParseError> parse(String input);
}
