package pl.wut.sag.knn.agent.clustering.algorithm


import pl.wut.sag.knn.ontology.object.ObjectWithAttributes
import spock.lang.Specification

class DefaultKNearestNeightboursTest extends Specification {

    private DistanceCalculator distanceCalculator = Mock()

    private final DefaultKNearestNeightbours calculator = new DefaultKNearestNeightbours(distanceCalculator)

    def 'Should choose nearest agents for voting'() {
        given:
        ObjectWithAttributes o1 = ObjectWithAttributes.of([1: "1"], 1)
        ObjectWithAttributes o2 = ObjectWithAttributes.of([1: "2"], 1)
        ObjectWithAttributes o3 = ObjectWithAttributes.of([1: "3"], 1)
        ObjectWithAttributes o4 = ObjectWithAttributes.of([1: "4"], 1)
        ObjectWithAttributes o5 = ObjectWithAttributes.of([1: "5"], 1)
        ObjectWithAttributes o6 = ObjectWithAttributes.of([1: "6"], 1)

        ObjectWithAttributes o7 = ObjectWithAttributes.of([7: "7"], 1)

        when:
        def result = calculator.runAndGetVotes([o1, o2, o3, o4, o5, o6] as Set, o7, 3)

        then:
        result.size() == 3
        result.keySet() == [o1, o3, o5] as Set

        distanceCalculator.calculateDistance(o1, o7) >> 1
        distanceCalculator.calculateDistance(o2, o7) >> 0
        distanceCalculator.calculateDistance(o3, o7) >> 3
        distanceCalculator.calculateDistance(o4, o7) >> 0
        distanceCalculator.calculateDistance(o5, o7) >> 5
        distanceCalculator.calculateDistance(o6, o7) >> 0
    }
}
