# Distributed information systems 2020/21.

## Description of business logic of microservice system

This application implements a microservice system for managing football team players. The microservice system consists of a total of five microservices: Player, Team, Nationality, National Team, and League. The player represents the main ie. composite microservice that is connected to all other services, ie each player has his own team, nationality, national team and league. Management of this system is enabled by performing implemented CRUD operations on microservice instances.

## Microservice system diagrams

### Database diagram

![1](https://i.ibb.co/J53j6m0/database-diagram.png)

### Microservice architecture diagram

![2](https://i.ibb.co/FJbYBWx/architecture-diagram.png)

## Pipeline Management Guide

### Build/Test/Deploy

```
./gradlew clean build && docker-compose build && docker-compose up -d
```
