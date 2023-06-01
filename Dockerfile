FROM ubuntu:22.04 as base

FROM base as sonar
ARG SONAR_LOGIN
ARG SONAR_PULLREQUEST_KEY
ARG SONAR_PULLREQUEST_BRANCH
ARG SONAR_PULLREQUEST_BASE
ARG GITHUB_TOKEN
ENV GITHUB_TOKEN ${GITHUB_TOKEN}

# Install Java: https://adoptium.net/blog/2021/12/eclipse-temurin-linux-installers-available/
RUN apt-get update && \
    apt-get install -y wget apt-transport-https gnupg && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends temurin-11-jdk

RUN mvn clean verify sonar:sonar -P sonar -Dsonar.login=$SONAR_LOGIN -Dsonar.pullrequest.key=$SONAR_PULLREQUEST_KEY -Dsonar.pullrequest.branch=$SONAR_PULLREQUEST_BRANCH -Dsonar.pullrequest.base=$SONAR_PULLREQUEST_BASE