package pl.wut.sag.classification

import com.google.gson.reflect.TypeToken
import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectRequest
import pl.wut.sag.classification.agent.user.interfaces.web.dto.CheckObjectResponse
import pl.wut.sag.classification.agent.user.interfaces.web.dto.OrderClassificationTrainingRequest
import pl.wut.sag.classification.domain.object.ObjectWithAttributes
import pl.wut.sag.classification.infrastructure.dataLoader.CsvObjectParser
import pl.wut.sag.classification.protocol.classy.ClassificationResult
import pl.wut.sag.test.feature.TestRestClient

import java.util.stream.Collectors

import static java.util.stream.Collectors.toMap


class ApplicationTest extends spock.lang.Specification {

    private final TestRestClient testRestClient = new TestRestClient()
    private final URI baseUri = URI.create("http://localhost:8080/")

    private static final URI IRIS_URI = URI.create('https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data')

    def 'Should get valid response after train call'() {
        given:
        final uri = baseWithPath('train')
        final body = new OrderClassificationTrainingRequest('iris', 4, IRIS_URI.toURL(), 0.75)

        when:
        def response = testRestClient.post(body, uri, String.class)

        then:
        noExceptionThrown()
        response == "Zlecono uczenie, kontekst: ${body.context}"
    }

    def 'Check iris dataset prediction'() {
        given:
        final int discriminatorColumn = 4
        final URI getResultUri = baseWithPath('results/iris')

        final List<ObjectWithAttributes> objects = testRestClient.get(IRIS_URI, { it })
                .readLines().stream()
                .filter({ !it.trim().isEmpty() })
                .map { new CsvObjectParser().parse(it.toString(), discriminatorColumn).result() }
                .collect(Collectors.toList())


        when:
        final Map<UUID, ObjectWithAttributes> newMapping = objects.stream().collect(toMap({ final ObjectWithAttributes ob ->
            callForCheck(ob, 'iris').getObjectId()
        }, { it as ObjectWithAttributes }))
        Thread.sleep(2000)

        final result = testRestClient.get(getResultUri, new TypeToken<List<ClassificationResult>>() {})
        Map<Boolean, List<ClassificationResult>> partition = result.stream()
                .collect(Collectors.partitioningBy({ final ClassificationResult r ->
            newMapping.get(r.getObject().getId()).getClassname().get() == r.getClassName()
        }))

        println "Valid predictions ${partition[true].size()} invalid predictions ${partition[false].size()}"

        then:
        true
        partition.get(true).size() > 4 * partition.get(false).size()
    }

    private CheckObjectResponse callForCheck(final ObjectWithAttributes object, final String context) {
        return testRestClient.post(new CheckObjectRequest(context, object.attributesAndValues), baseWithPath("check"), CheckObjectResponse.class)
    }

    def 'Create abalone dataset'() {
        given:
        final uri = baseWithPath('train')
        final URL abaloneUrl = new URL('https://archive.ics.uci.edu/ml/machine-learning-databases/abalone/abalone.data')
        final body = new OrderClassificationTrainingRequest('abalone', 8, abaloneUrl, 0.75)

        when:
        def response = testRestClient.post(body, uri, { it })

        then:
        noExceptionThrown()
        response == "Zlecono uczenie, kontekst: ${body.context}"
    }

    def 'Download abalone'() {
        when:
        println testRestClient.get(
                new URI('https://archive.ics.uci.edu/ml/machine-learning-databases/abalone/abalone.data'), { it }
        ).readLines().collect { it.split(',') }.collect { it[8] }.stream()
                .collect(Collectors.groupingBy({ it }, Collectors.counting()))

        then:
        true
    }

    private URI baseWithPath(final String path) {
        return URI.create("${baseUri.toString()}${path}")
    }
}
