## Micronaut + Postgres (on docker) + Jooq + Flyway + Lombok template

To see where components are configured, see mini tutorial on youtube:
https://www.youtube.com/watch?v=PF_Bg6CgMts&t=16s

## Micronaut 2.4.1 Documentation

- [User Guide](https://docs.micronaut.io/2.4.1/guide/index.html)
- [API Reference](https://docs.micronaut.io/2.4.1/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/2.4.1/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

## Getting started

In order to build the project run:
`./gradlew build`

In order to remove the docker container and its database, run
`docker rm -f -v <container_name>`

This project is a base template for getting started with 
micronaut + postgres + jooq + flyway + lombok.

It contains a very simple migration file (`resources/db/postgres/V1__create_user_table.sql`) 
to create a base users table and 
an entry with a user. Modify this based on your requirements.

