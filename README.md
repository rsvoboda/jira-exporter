# jira-exporter
JIRA metrics exporter to MP / Prometheus metrics

```bash
JIRA_PROJECTS=QUARKUS JIRA_AUTH_USERNAME=foo JIRA_AUTH_PASSWORD=bar mvn quarkus:dev

curl http://0.0.0.0:8080/metrics/ 2>/dev/null | grep jira
```

## Docker image
https://hub.docker.com/r/rostasvo/jira-exporter

```
docker pull rostasvo/jira-exporter:1.0.0.Final

docker run --env JIRA_PROJECTS=QUARKUS --env JIRA_AUTH_USERNAME=foo --env JIRA_AUTH_PASSWORD=bar -i --rm -p 8080:8080 rostasvo/jira-exporter:1.0.0.Final
```

## Metrics
All metrics have type `gauge`.

```
# HELP jira_open_blocker Total number of open blocker issues for given project
# HELP jira_open_critical Total number of open critical issues for given project
# HELP jira_open_documentation Total number of open documentation bugs for given project
# HELP jira_open_regression Total number of open regression issues for given project
# HELP jira_open_regression_ha Total number of open ha/resiliency regression issues for given project
# HELP jira_open_regression_performance Total number of open performance/scalability regression issues for given project
# HELP jira_open_untriaged Total number of open untriaged bugs for given project
# HELP jira_verified_blocker Total number of verified blocker issues for given project
# HELP jira_verified_critical Total number of verified critical issues for given project
```

## Release
```bash
mvn release:prepare
mvn release:clean

git checkout $TAG

mvn clean package -Dnative -Dquarkus.native.container-build=true \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.username=rostasvo \
  -Dquarkus.container-image.password=$PASSWORD \
  -Dquarkus.container-image.registry=docker.io \
  -Dquarkus.container-image.group=rostasvo
```
