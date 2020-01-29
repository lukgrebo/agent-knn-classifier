package pl.wut.sag.classification.infrastructure.dataLoader;

import pl.wut.sag.classification.domain.object.ObjectWithAttributes;
import pl.wut.sag.classification.infrastructure.function.Result;
import pl.wut.sag.classification.infrastructure.parser.ParseError;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CsvObjectParser {

    public Result<ObjectWithAttributes, ParseError> parse(final String input, final int discriminatorColumnIndex) {
        final String[] split = input.split(",");

        final Map<Integer, String> attributes = IntStream.range(0, split.length)
                .boxed()
                .collect(Collectors.toMap(Function.identity(), i -> split[i]));

        return Result.ok(ObjectWithAttributes.of(attributes, discriminatorColumnIndex));
    }
}
