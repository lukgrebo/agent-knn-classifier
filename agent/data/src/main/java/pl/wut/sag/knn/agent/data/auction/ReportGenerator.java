package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface ReportGenerator {
    void generate(final URL location, final Map<AID, Set<ObjectWithAttributes>> objectsByAgent);

    static ReportGenerator stringToConsole() {
        return new StringToConsoleReportGenerator();
    }
}


class StringToConsoleReportGenerator implements ReportGenerator {

    @Override
    public void generate(final URL location, final Map<AID, Set<ObjectWithAttributes>> objectsByAgent) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raport z wczytywania danych z ").append(location).append("\n");
        objectsByAgent.forEach((aid, objects) -> writeAgentSummary(stringBuilder, aid, objects));

        System.out.println(stringBuilder.toString());
    }

    private void writeAgentSummary(final StringBuilder stringBuilder, final AID aid, final Set<ObjectWithAttributes> objects) {
        stringBuilder.append("Agent ").append(aid.getName()).append("\n")
                .append("Obiekty: [").append(objects.stream().map(ObjectWithAttributes::toString).collect(Collectors.joining(","))).append("]").append("\n");
    }
}

class FileReportGenerator {

}