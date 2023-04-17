## MMO Server implementation (work in progress)

Currently working on:
- Receiving info from UE server about mobs to process and pass to player clients

# Documentation within blog:
Each of the posts contains a video overview.

## Character creation examples:
- Part 1: overview using N-Hance Assets: https://ylazarev.com/2022/08/10/20-unreal-engine-mmo-style-character-creation-with-n-hance-assets/
- Part 2: Complex nested options: https://ylazarev.com/2022/08/17/21-unreal-engine-character-creation-part-2-complex-nested-options/
- Part 3: Apply nested options to meshes: https://ylazarev.com/2022/08/27/22-unreal-engine-character-creation-part-3-apply-options-to-mesh-n-hance/
- Part 4: Connecting implementation with the server: https://ylazarev.com/2022/09/29/23-unreal-engine-character-creation-part-4-connect-to-custom-server/
- Part 5: Select character screen connected with server: https://ylazarev.com/2022/10/08/24-unreal-engine-character-creation-part-5-select-character-with-server/

## Logging into the game:
- Part 1: https://ylazarev.com/2022/10/19/24-unreal-engine-logging-into-a-custom-server/
- Part 2: https://ylazarev.com/2022/10/26/26-unreal-engine-synchronize-motion-with-a-custom-server/
- part 3: https://ylazarev.com/2022/11/14/27-unreal-engine-display-nearby-players-with-custom-server/
- part 4: https://ylazarev.com/2022/11/24/28-unreal-engine-synchronize-nearby-player-appearance/

## Inventory Implementation
- Base implementation: https://ylazarev.com/2022/05/11/creating-mmo-rpg-style-inventory-system-with-java-and-mongodb/
- Base integration with UE: https://ylazarev.com/2022/05/27/14-mmo-rpg-inventory-in-unreal-engine-server/
- Customise inventory: https://ylazarev.com/2022/06/12/16-ue-customized-mmo-rpg-inventory/
- Equipping items example: https://ylazarev.com/2022/07/02/17-java-mmo-rpg-inventory-implementation-equipping-items/
- Equipping items continued: https://ylazarev.com/2022/07/24/19-unreal-engine-equip-inventory-items-with-custom-server/

## Base components, JWT no longer in use, in favor of using Cognito or alternatives
- Configuring MongoDB & creation of characters for account: https://ylazarev.com/2021/05/08/7-java-micronaut-with-mongodb-character-controller/

- Setting up Micronaut, jooq, flyway:
https://ylazarev.com/2021/04/18/4-java-micronaut-dockered-postgres/

- Setting up JWT Authentication:
https://ylazarev.com/2021/04/26/5-java-micronaut-jwt-authentication/

- Encode password for user, integrate with JWT: https://ylazarev.com/2021/05/04/6-java-secure-encode-user-credential/

This project branches from the Micronaut template found here:
https://github.com/yazoo321/micronaut_template

To see where components are configured, see mini tutorial on youtube:
https://www.youtube.com/watch?v=PF_Bg6CgMts&t=16s

JWT Authentication described here on YouTube tutorial here:
https://www.youtube.com/watch?v=acAFcDWuxhI&t=8s

## Dependencies
In this project we rely on Docker, you can download it here:
https://docs.docker.com/desktop/windows/install/
Docker will be used to spin up the dependencies, including any DBs (postgres, mongodb) and
now Kafka + Zookeeper.

## Kafka
More in-depth Kafka instructions here: 
https://unreal-mmo-dev.com/2023/04/17/37-how-to-connect-your-java-micronaut-springboot-with-kafka-sasl/

There was a very useful video covering some details that can be found here:
https://www.youtube.com/watch?v=bj5SKXanaAI

Kafka is spun up using 'docker-compose' so check the relevant entries there.
the './configs' directory contains the necessary configs for SASL enabled Kafka
Do note that you will need to change these for production use.

Micronaut also offers some useful documentation: https://guides.micronaut.io/latest/micronaut-kafka-maven-java.html


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
