package tech.ikora.selenium.locator.evolution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.ikora.selenium.locator.evolution.process.BDConnectLauncher;
import tech.ikora.selenium.locator.evolution.process.MavenLauncher;
import tech.ikora.selenium.locator.evolution.versions.Version;
import tech.ikora.selenium.locator.evolution.versions.VersionProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Runner {
    private static final Logger logger = LogManager.getLogger(Runner.class);

    private final VersionProvider versionProvider;
    private final File agentJar;
    private final File dbConnectJar;
    private final File database;
    private final int port;

    public Runner(VersionProvider versionProvider, File agentJar, File dbConnectJar, File database, int port) {
        this.versionProvider = versionProvider;
        this.agentJar = agentJar;
        this.dbConnectJar = dbConnectJar;
        this.database = database;
        this.port = port;
    }

    public void execute() throws IOException {
        try(final BDConnectLauncher dbConnect = new BDConnectLauncher(dbConnectJar, database, port)){
            for(Version version: versionProvider){
                logger.info(String.format("Start execution for %s [%s]",
                        version.getLocation(),
                        version.getCommitId()
                ));

                try {
                    final String agent = String.format("%s=%d",
                            agentJar.getAbsolutePath(),
                            port
                    );

                    final String result = new MavenLauncher()
                            .withExtraParameter("forkMode", "never")
                            .withMavenOptions("javaagent", agent)
                            .inDirectory(version.getLocation())
                            .execute();
                } catch (IOException | InterruptedException | TimeoutException e) {
                    logger.error(String.format("Failed to execute mvn for %s [%s]: %s",
                            version.getLocation(),
                            version.getCommitId(),
                            e.getMessage()
                    ));
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Something went wrong: %s", e.getMessage()));
        }

    }
}
