package com.swookiee.tools.com.swookiee.tools.deployplugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.swookiee.tools.client.SwookieClientBuilder;
import com.swookiee.tools.client.SwookieeClient;
import com.swookiee.tools.client.SwookieeClientException;

/**
 * 
 * This maven plugin executor serves as an intermediate layer between maven and the RFC-182 client. Via this tool we are
 * able to deploy jar files to a Swookiee instance.
 * 
 * @goal bundle-deploy
 * 
 */
public class SwookieeInstall extends AbstractMojo {

    /**
     * Hostname
     * 
     * @parameter default-value="localhost"
     */
    private String host;

    /**
     * HTTP port of swookiee instance.
     * 
     * @parameter default-value="8080"
     */
    private Integer port;

    /**
     * Admin Username.
     * 
     * @parameter default-value="admin"
     */
    private String username;

    /**
     * Admin Account Password.
     * 
     * @parameter default-value="admin123"
     */
    private String password;

    /**
     * Hostname
     * 
     * @parameter default-value="false"
     */
    private boolean useHttps;

    /**
     * The name of the generated JAR file.
     * 
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.jar"
     * @required
     */
    private String bundleFile;

    private SwookieeClient swookieeClient;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            this.swookieeClient = getSwookieeClient();
            forceInstallAndStartBundle(new File(this.bundleFile));
            this.swookieeClient.stop();
        } catch (final SwookieeClientException | IOException ex) {
            getLog().error("Could not deploy bundle: " + ex.getMessage(), ex);
        }
    }

    private SwookieeClient getSwookieeClient() throws SwookieeClientException {
        final SwookieClientBuilder swookieClientBuilder = SwookieClientBuilder.newTarget(this.host)
                .withPort(this.port)
                .withUsernamePassword(this.username, this.password);

        if (useHttps) {
            swookieClientBuilder.enableSelfSignedHttps();
        }

        return swookieClientBuilder.create();
    }

    public void forceInstallAndStartBundle(final File file) throws SwookieeClientException, IOException {
        getLog().info(
                String.format("Installing %s to %s", file.getAbsolutePath(), this.swookieeClient.getConfiguredTarget()));
        final String installedBundle = this.swookieeClient.installBundle(file, true);
        this.swookieeClient.startBundle(installedBundle);
    }
}