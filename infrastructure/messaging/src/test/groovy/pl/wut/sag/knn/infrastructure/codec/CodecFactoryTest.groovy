package pl.wut.sag.knn.infrastructure.codec


import spock.lang.Specification

class CodecFactoryTest extends Specification {

    private final CodecFactory codecFactory = CodecFactory.instance()

    def 'Should serialize object with attributes using json'() {
        given:
        final ObjectWithAttributes object = ObjectWithAttributes.of([1: "5", 2: "10"])
        final kodec = codecFactory.forKodec('json')
        assert kodec.isPresent()

        when:
        def json = kodec.get().encode(object)

        then:
        noExceptionThrown()
        json != null
        assert json == """{
  "id": "${object.id.toString()}",
  "attributesAndValues": {
    "1": "5",
    "2": "10"
  }
}"""
    }

    def 'Should deserialize object with attributes using json'() {
        given:
        final UUID uuid = UUID.randomUUID()
        final json = """{
  "id": "${uuid.toString()}",
  "attributesAndValues": {
    "1": "5",
    "2": "10"
  }
}"""
        final kodec = codecFactory.forKodec('json')
        assert kodec.isPresent()

        when:
        final deserialized = kodec.get().decode(json, ObjectWithAttributes.class)

        then:
        deserialized != null
        deserialized.isValid()
        final deserializedObject = deserialized.isValid() as ObjectWithAttributes
        deserializedObject.id == uuid
        deserializedObject.getAsString(1).get() == "5"
        deserializedObject.getAsString(2).get() == "10"
    }

}
