# Modules

* savings-a
* savings-b
* api-gateway
* buildSrc: contains templates for re-usability on gradle scripts.

# Execution

## Building and running the complete environment on docker

## Running the apps through gradle and the databases on docker

<details>
  <summary>click to toggle expand</summary>

Starting the databases:
```shell
cd docker/env-without-apps
docker compose -p temp up
```

Then you choose to
* start all the apps in one go (console logs become mixed):
  * `./gradlew b --parallel --max-workers=4 -x checkstyleMain -x checkstyleTest -x spotbugsMain -x spotbugsTest -x test bootRun -PjvmArgs="-Dreactor.netty.http.server.accessLogEnabled=true"`
* or start each separately:
  * `./gradlew savings-a::bootRun`
  * `./gradlew savings-b::bootRun`
  * `./gradlew api-gateway::bootRun -PjvmArgs="-Dreactor.netty.http.server.accessLogEnabled=true"`

</details>

# Cleaning up

<details>
  <summary>click to toggle expand</summary>

```shell
# at the docker compose folder you used the up command
docker compose -p temp down
```

</details>

# Utilities scripts

<details>
  <summary>click to toggle expand</summary>

At folder `scripts/`
* [api-get-balance.sh](scripts/api-get-balance.sh): send get balance request to the api gateway
  * args:
    * `a` or `b` depending on the module you want to reach
  * example:
    * `scripts/api-get-balance.sh b`
* [api-update-balance.sh](scripts/api-update-balance.sh): send update balance request to the api gateway
  * args:
    * `a` or `b` depending on the module you want to reach
    * amount you want to add or subtract
  * example:
    * `scripts/api-update-balance.sh a -12.32`
* [get-balance.sh](scripts/get-balance.sh): send get balance request to a specific savings server
  * args:
    * port: depending on the module you want to reach
  * example:
    * `scripts/get-balance.sh 8081`
* [update-balance.sh](scripts/update-balance.sh): send update balance request to a specific savings server
  * args:
    * `a` or `b` depending on the module you want to reach
    * amount you want to add or subtract
  * example:
    * `scripts/update-balance.sh 8081 -12.32`

</details>

# Extra questions
## How would you test the timeouts?
## How to scale the API Gateway?
* If you are not using a cloud environment
  * You are going to need a dns server returning IPs pointing to multiple HAProxies, these proxies 
  will be the entry point for the requests incoming from public internet. 
    * this provides client side fail-over
  * Those HAProxies will load balance to multiple instances of the spring cloud gateway application.

## How to monitor uptime, so you can sleep at night?

# Improvements

# Exercise Definition
<details>
  <summary><b>Things from the exercise definition</b></summary>

# Requirements

## API Gateway
- run on port 8080
- respond in less than 5 seconds, else throw a timeout.
- log to file all incoming requests as info level


## Savings A
- run on port 8081
- return the current balance for account A
- increase/decrease the balance for account A
- persist the balance in a PostgreSQL database. Feel free to define your schema.

## Savings B
- run on port 8082
- return the current balance for account B
- increase/decrease the balance for account B

# Sequence Diagrams

## Savings A

![](documentation/image/sequence-diagram-savings-a.png "sequence diagram savings a")

## Savings B

![](documentation/image/sequence-diagram-savings-b.png "sequence diagram savings b")

</details>