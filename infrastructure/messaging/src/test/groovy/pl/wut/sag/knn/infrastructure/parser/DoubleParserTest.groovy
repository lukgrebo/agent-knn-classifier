package pl.wut.sag.knn.infrastructure.parser


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class DoubleParserTest extends Specification {

    @Subject
    private final DoubleParser doubleParser = new DoubleParser()

    @Unroll
    def 'Should return parse error parsing[#input]]'() {
        when:
        final parseResult = doubleParser.parse(input)

        then:
        !parseResult.isValid()
        parseResult.getError()
        final error = parseResult.getError()
        error.cause != null || input == null

        where:
        input << [null, 'xd']
    }

    @Unroll
    def 'Should succesfully parse [#input]'() {
        when:
        final parseResult = doubleParser.parse(input)

        then:
        parseResult.isValid()
        parseResult.isValid() == expectedValue

        where:
        input   | expectedValue
        '1.0'   | 1d
        '1'     | 1d
        '02.30' | 2.3d
        '11'    | 11d
        '1,1'   | 1.1d
    }

}
