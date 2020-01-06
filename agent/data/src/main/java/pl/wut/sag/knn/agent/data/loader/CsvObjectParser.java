package pl.wut.sag.knn.agent.data.loader;

import pl.wut.sag.knn.infrastructure.function.Result;
import pl.wut.sag.knn.infrastructure.parser.ParseError;
import pl.wut.sag.knn.infrastructure.parser.Parser;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CsvObjectParser implements Parser<ObjectWithAttributes> {

    @Override
    public Result<ObjectWithAttributes, ParseError> parse(final String input) {
        final String[] split = input.split(",");

        final Map<Integer, String> attributes = IntStream.range(0, split.length)
                .boxed()
                .collect(Collectors.toMap(Function.identity(), i -> split[i]));

        return Result.ok(ObjectWithAttributes.of(attributes));
    }
}
