#!/usr/bin/env bash

mkdir microservices
# shellcheck disable=SC2164
cd microservices

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=player-service \
  --package-name=se.magnus.microservices.core.player \
  --groupId=se.magnus.microservices.core.player \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  player-service

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=team-service \
  --package-name=se.magnus.microservices.core.team \
  --groupId=se.magnus.microservices.core.team \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  team-service

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=nationality-service \
  --package-name=se.magnus.microservices.core.nationality \
  --groupId=se.magnus.microservices.core.nationality \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  nationality-service

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=nationalteam-service \
  --package-name=se.magnus.microservices.core.nationalteam \
  --groupId=se.magnus.microservices.core.nationalteam \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  nationalteam-service

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=league-service \
  --package-name=se.magnus.microservices.core.league \
  --groupId=se.magnus.microservices.core.league \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  league-service

spring init \
  --boot-version=2.1.0.RELEASE \
  --build=gradle \
  --java-version=1.8 \
  --packaging=jar \
  --name=player-composite-service \
  --package-name=se.magnus.microservices.composite.player \
  --groupId=se.magnus.microservices.composite.player \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  player-composite-service

# shellcheck disable=SC2103
cd ..
