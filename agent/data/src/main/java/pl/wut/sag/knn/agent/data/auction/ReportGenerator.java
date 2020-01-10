package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import pl.wut.sag.knn.agent.data.model.ClusterSummaryWithObjects;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ReportGenerator {
    void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects);

    static ReportGenerator stringToConsole() {
        return new StringToConsoleReportGenerator();
    }
}


class StringToConsoleReportGenerator implements ReportGenerator {

    @Override
    public void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raport z wczytywania danych z ").append(location).append("\n");
        clusterSummaryWithObjects.forEach(ob -> writeAgentSummary(stringBuilder, ob.getAverageDistance(), ob.getAgent(), ob.getObjects()));

        System.out.println(stringBuilder.toString());
    }

    private void writeAgentSummary(final StringBuilder stringBuilder, final double averageDistance, final AID aid, final Set<ObjectWithAttributes> objects) {
        stringBuilder.append("Agent ").append(aid.getName()).append("\n")
                .append("Srednia odległość: ").append(averageDistance).append("\n")
                .append("Obiekty: [").append(objects.stream().map(ObjectWithAttributes::humanReadableString).collect(Collectors.joining(","))).append("]").append("\n");
    }
}

class FileReportGenerator {

}