# Distribuirani informacioni sistemi 2020/21.

## Opis poslovne logike mikroservisnog sistema

U ovoj aplikaciji je implementiran mikroservisni sistem za upravljanje igračima fudbalskog tima. Mikroservisni sistem se sastoji od ukupno pet mikroservisa: Igrač, Tim, Nacionalnost, Reprezentacija i Liga. Igrač predstavlja glavni tj. kompozitni mikroservis koji je povezan sa svim ostalim servisima, odnosno svaki igrač ima svoj tim, nacionalnost, reprezentaciju i ligu. Upravljanje ovim sistemom je omogućeno izvršavanjem implementiranih CRUD operacija nad mikroservisnim instancama.

## Dijagrami mikroservisnog sistema

### Dijagram baze podataka

![1](https://i.ibb.co/J53j6m0/database-diagram.png)

### Dijagram mikroservisne arhitekture

![2](https://i.ibb.co/FJbYBWx/architecture-diagram.png)

## Uputstvo za upravljanje pipeline-om

### Build/Test/Deploy

```
/gradlew clean build && docker-compose build && docker-compose up -d
```
