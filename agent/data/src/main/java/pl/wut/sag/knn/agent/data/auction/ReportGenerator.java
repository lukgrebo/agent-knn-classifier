package pl.wut.sag.knn.agent.data.auction;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import pl.wut.sag.knn.agent.data.model.ClusterSummaryWithObjects;
import pl.wut.sag.knn.infrastructure.discovery.ServiceDiscovery;
import pl.wut.sag.knn.ontology.MiningReport;
import pl.wut.sag.knn.ontology.object.ObjectWithAttributes;
import pl.wut.sag.knn.protocol.MiningProtocol;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ReportGenerator {
    void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects);

    static ReportGenerator stringToConsole() {
        return new StringToConsoleReportGenerator();
    }

    static ReportGenerator file() {
        return new FileReportGenerator();
    }

    static ReportGenerator sendToUserAgent(final UUID miningRequestUuid,
                                           final Function<MiningReport, String> mapper,
                                           final ServiceDiscovery serviceDiscovery,
                                           final Consumer<ACLMessage> send) {
        return new SendToUserAgentGenerator(miningRequestUuid, mapper, serviceDiscovery, send);
    }
}

class ReportTextGenerator {
    String generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raport z wczytywania danych z ").append(location).append("\n");
        clusterSummaryWithObjects.forEach(ob -> writeAgentSummary(stringBuilder, ob.getAverageDistance(), ob.getAgent(), ob.getObjects()));

        return stringBuilder.toString();
    }

    private void writeAgentSummary(final StringBuilder stringBuilder, final double averageDistance, final AID aid, final Set<ObjectWithAttributes> objects) {
        stringBuilder.append("Agent ").append(aid.getName()).append("\n")
                .append("Srednia odległość: ").append(averageDistance).append("\n")
                .append("Obiekty: [").append(objects.stream().map(ObjectWithAttributes::humanReadableString).collect(Collectors.joining(","))).append("]").append("\n");
    }
}

class StringToConsoleReportGenerator implements ReportGenerator {

    private final ReportTextGenerator reportTextGenerator = new ReportTextGenerator();

    @Override
    public void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects) {
        System.out.println(reportTextGenerator.generate(location, clusterSummaryWithObjects));
    }

}

class FileReportGenerator implements ReportGenerator {

    private final ReportTextGenerator reportTextGenerator = new ReportTextGenerator();
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss");

    @Override
    public void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects) {
        final Path path = Paths.get("").toAbsolutePath().resolve("report").resolve(formatter.format(LocalDateTime.now()));
        final String report = reportTextGenerator.generate(location, clusterSummaryWithObjects);
        try {
            path.getParent().toFile().mkdirs();
            Files.write(path, report.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class SendToUserAgentGenerator implements ReportGenerator {

    private final UUID miningRequestUuid;
    private final Function<MiningReport, String> mapper;
    private final ServiceDiscovery serviceDiscovery;
    private final Consumer<ACLMessage> send;
    private final ReportTextGenerator reportTextGenerator = new ReportTextGenerator();

    public SendToUserAgentGenerator(final UUID miningRequestUuid, final Function<MiningReport, String> mapper, final ServiceDiscovery serviceDiscovery, final Consumer<ACLMessage> send) {
        this.miningRequestUuid = miningRequestUuid;
        this.mapper = mapper;
        this.serviceDiscovery = serviceDiscovery;
        this.send = send;
    }

    @Override
    public void generate(final URL location, final List<ClusterSummaryWithObjects> clusterSummaryWithObjects) {
        final String report = reportTextGenerator.generate(location, clusterSummaryWithObjects);
        final String reportJson = mapper.apply(MiningReport.of(miningRequestUuid, report));

        final ACLMessage message = MiningProtocol.sendReport.templatedMessage();
        message.setContent(reportJson);
        final List<DFAgentDescription> userAgents = serviceDiscovery.findServices(MiningProtocol.sendReport.getTargetService()).result();
        userAgents.stream().map(DFAgentDescription::getName).forEach(message::addReceiver);

        send.accept(message);
    }
}