package pl.wut.sag.knn.clustering.service

import pl.wut.sag.knn.agent.clustering.algorithm.EuclideanDistanceCalculator
import pl.wut.sag.knn.infrastructure.parser.DoubleParser
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes
import spock.lang.Specification
import spock.lang.Unroll

class EuclideanDistanceCalculatorTest extends Specification {

    private final EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator(new DoubleParser())

    @Unroll
    def 'Should calculate distance between [#one] and [#other] as [#result]'() {
        expect:
        calculator.calculateDistance(one, other) == result

        where:
        one                                                     | other                                                   | result
        //same String attributes
        ObjectWithAttributes.of([1: "x"], 0)                    | ObjectWithAttributes.of(1: "x", 0)                      | 0
        // same numeric attributes
        ObjectWithAttributes.of([1: "1.5"], 0)                  | ObjectWithAttributes.of(1: "1.5", 0)                    | 0
        //diferent String attributes
        ObjectWithAttributes.of([1: "x"], 0)                    | ObjectWithAttributes.of(1: "d", 0)                      | 1
        // different numeric attributes
        ObjectWithAttributes.of([1: "1"], 0)                    | ObjectWithAttributes.of(1: "4", 0)                      | 3
        // multidimensionals
        ObjectWithAttributes.of([1: "x", 2: "d", 3: "hehe"], 0) | ObjectWithAttributes.of([1: "x", 2: "d", 3: "hehe"], 0) | 0
        ObjectWithAttributes.of([1: "3", 2: "4"], 0)            | ObjectWithAttributes.of([1: "0", 2: "0"], 0)            | 5
        // missing attributes
        ObjectWithAttributes.of(1: "5", 2: "25", 0)             | ObjectWithAttributes.of([:], 0)                         | 0 //TODO, what if attributes missing
    }
}