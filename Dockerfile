FROM ubuntu as base

FROM base as sonar
ARG SONAR_LOGIN
ARG SONAR_PULLREQUEST_KEY
ARG SONAR_PULLREQUEST_BRANCH
ARG SONAR_PULLREQUEST_BASE
ARG GITHUB_TOKEN
ENV GITHUB_TOKEN ${GITHUB_TOKEN}

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk11-jdk

echo mvn clean verify sonar:sonar -P sonar -Dsonar.login=$SONAR_LOGIN -Dsonar.pullrequest.key=$SONAR_PULLREQUEST_KEY -Dsonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH -Dsonar.pullrequest.base=$SONAR_PULLREQUEST_BASE