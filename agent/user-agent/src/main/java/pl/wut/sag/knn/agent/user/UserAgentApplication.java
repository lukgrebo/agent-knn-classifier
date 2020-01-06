package pl.wut.sag.knn.agent.user;

import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import pl.wut.sag.knn.agent.user.api.ApiConfig;
import pl.wut.sag.knn.infrastructure.startup.AgentStartupManager;


public class UserAgentApplication {
    public static final int BUFFER_SIZE = 1024;
    public static final int PORT = 8081;

    public static void main(final String[] args) throws StaleProxyException {
        final AgentStartupManager startupManager = new AgentStartupManager();
        final AgentContainer mainContainer = startupManager.startMainContainer("sag-knn", true);

        final UserAgentDependencies dependencies = new UserAgentDependencies(new ApiConfig(PORT));

        startupManager.startAgent(mainContainer, UserAgent.class, "User agent", dependencies);
    }

//
//    public static void main(String[] args) throws IOException {
//        final URL url = new URL("https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data");
//        try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
//            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
//            byte dataBuffer[] = new byte[BUFFER_SIZE];
//            int read;
//            while ((read = in.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
//                out.write(dataBuffer, 0, read);
//            }
//
//            System.out.println(new String(out.toByteArray()));
//        } catch (final IOException e) {
//            throw new RuntimeException();
//        }
//    }
}
