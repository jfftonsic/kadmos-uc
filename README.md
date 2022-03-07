# Modules

* savings-a
* savings-b
* api-gateway
* buildSrc: contains templates for re-usability on gradle scripts.

# Extra questions
## How would you test the timeouts?
## How to scale the API Gateway?
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