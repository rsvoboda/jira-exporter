package io.quarkus.qe.metrics;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class JiraMetrics {

    @ConfigProperty(name = "jira.projects", defaultValue = "foo")
    public List<String> jiraProjects;

    @ConfigProperty(name = "jira.url", defaultValue = "https://issues.redhat.com")
    public String jiraURL;

    @ConfigProperty(name = "jira.auth.token")
    String token;

    @Inject
    MetricRegistry registry;

    @Inject
    JiraProjectBaseMetrics jiraProjectBaseMetrics;

    private static final Logger log = Logger.getLogger(JiraMetrics.class);

    void onStart(@Observes StartupEvent ev) throws IOException {
        log.info("The application is starting ...");

        boolean initiated = jiraProjectBaseMetrics.initiate(jiraURL, token);
        if (!initiated) {
            return;
        }
        for (String jiraProject : jiraProjects) {
            String projectName = jiraProject.trim();
            Tag repositoryTag = new Tag("repo", projectName);
            log.info("Processing: '" + projectName + "'");

            jiraProjectBaseMetrics.jiraReadinessMetrics(registry, projectName, repositoryTag);
        }
    }
}
