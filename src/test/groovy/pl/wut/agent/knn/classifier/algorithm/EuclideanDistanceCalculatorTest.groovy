package pl.wut.agent.knn.classifier.algorithm

import pl.wut.agent.knn.classifier.definitions.classification.ObjectWithAttributes
import pl.wut.agent.knn.classifier.infrastructure.parser.DoubleParser
import spock.lang.Specification
import spock.lang.Unroll

class EuclideanDistanceCalculatorTest extends Specification {

    private final EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator(new DoubleParser())

    @Unroll
    def 'Should calculate distance between [#one] and [#other] as [#result]'() {
        expect:
        calculator.calculateDistance(one, other) == result

        where:
        one                                                  | other                                                | result
        //same String attributes
        ObjectWithAttributes.of([1: "x"])                    | ObjectWithAttributes.of(1: "x")                      | 0
        // same numeric attributes
        ObjectWithAttributes.of([1: "1.5"])                  | ObjectWithAttributes.of(1: "1.5")                    | 0
        //diferent String attributes
        ObjectWithAttributes.of([1: "x"])                    | ObjectWithAttributes.of(1: "d")                      | 1
        // different numeric attributes
        ObjectWithAttributes.of([1: "1"])                    | ObjectWithAttributes.of(1: "4")                      | 3
        // multidimensionals
        ObjectWithAttributes.of([1: "x", 2: "d", 3: "hehe"]) | ObjectWithAttributes.of([1: "x", 2: "d", 3: "hehe"]) | 0
        ObjectWithAttributes.of([1: "3", 2: "4"])            | ObjectWithAttributes.of([1: "0", 2: "0"])            | 5
        // missing attributes
        ObjectWithAttributes.of(1: "5", 2: "25")             | ObjectWithAttributes.of([:])                         | 0 //TODO, what if attributes missing
    }
}