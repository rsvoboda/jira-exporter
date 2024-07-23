package io.quarkus.qe.metrics;

import io.smallrye.metrics.ExtendedMetadataBuilder;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Metrics based on direct curl-like interaction with JIRA
 *
 */
@ApplicationScoped
public class JiraProjectBaseMetrics {

    private static final Logger log = Logger.getLogger(JiraProjectBaseMetrics.class);
    private String jiraURL;
    private String authHeaderValue;

    public boolean initiate(String jiraURL, String token) {
        this.jiraURL = jiraURL;
        authHeaderValue = "Bearer " + token;

        return getCountFor("resolution = Unresolved") > 0;
    }

    public void jiraReadinessMetrics(MetricRegistry registry, String projectName, Tag... tags) {

        registerMetric("jira_open_blocker", "Total number of open blocker issues for given project",
                "project = " + projectName + " and priority = blocker and resolution = Unresolved", registry, tags);

        registerMetric("jira_verified_blocker", "Total number of verified blocker issues for given project",
                "project = " + projectName + " and priority = blocker and resolution = Done AND status in (Closed, Verified)", registry, tags);

        registerMetric("jira_open_critical", "Total number of open critical issues for given project",
                "project = " + projectName + " and priority = critical and resolution = Unresolved", registry, tags);

        registerMetric("jira_verified_critical", "Total number of verified critical issues for given project",
                "project = " + projectName + " and priority = critical and resolution = Done AND status in (Closed, Verified)", registry, tags);


        registerMetric("jira_open_regression", "Total number of open regression issues for given project",
                "project = " + projectName + " and labels = regression and resolution = Unresolved", registry, tags);

        registerMetric("jira_open_regression_performance", "Total number of open performance/scalability regression issues for given project",
                "project = " + projectName + " and labels = performance and labels = regression and resolution = Unresolved", registry, tags);

        registerMetric("jira_open_regression_ha", "Total number of open ha/resiliency regression issues for given project",
                "project = " + projectName + " and labels = ha and labels = regression and resolution = Unresolved", registry, tags);


        registerMetric("jira_open_documentation", "Total number of open documentation bugs for given project",
                "project = " + projectName + " and type = Bug and component = Documentation and resolution = Unresolved", registry, tags);

        registerMetric("jira_open_untriaged", "Total number of open untriaged bugs for given project",
                "project = " + projectName + " and type = Bug and status = \"To Do\" and assignee is EMPTY and issuefunction not in hasComments()", registry, tags);

    }

    private void registerMetric(String name, String description, String jql, MetricRegistry registry, Tag... tags) {
        log.debug("Registering metric for " + jql);
        registry.register(
                new ExtendedMetadataBuilder()
                        .withName(name)
                        .withType(MetricType.GAUGE)
                        .withDescription(description)
                        .skipsScopeInOpenMetricsExportCompletely(true)
                        .prependsScopeToOpenMetricsName(false)
                        .build(),
                (Gauge<Number>) () -> getCountFor(jql),
                tags);
    }

    private int getCountFor(String jql) {
        int count = 0;
        HttpURLConnection con = null;
        try {
            URL url = new URL(jiraURL + "/rest/api/2/search?fields=key,components&maxResults=0&jql=" +
                    URLEncoder.encode(jql, StandardCharsets.UTF_8));
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "jira-metrics");
            con.setRequestProperty("Authorization", authHeaderValue);
            JsonReader jsonReader = Json.createReader(con.getInputStream());
            JsonObject rootJSON = jsonReader.readObject();
            count = rootJSON.getInt("total");
        } catch (IOException e) {
            log.error("Unable to get expected data for jql: " + jql, e);
            dumpHeaders(con);
        } finally {
            con.disconnect();
        }
        log.debug(jql + " - " + count);
        return count;
    }

    private void dumpHeaders(HttpURLConnection con) {
        con.getHeaderFields().forEach((key,value)-> {
            System.out.println(key + ": " + value);
        });
    }
}
