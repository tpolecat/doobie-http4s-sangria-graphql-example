## doobie-http4s-sangria-grapgql-example

Example app that uses doobie, http4s, Cats effect 3 and Sangria to serve GraphQL.

This was bootstrapped from an [example](https://github.com/OlegIlyenko/sangria-http4s-graalvm-example) by @OlegIlyenko. Thanks!

### Quick Start

Start up the demo database with

    docker-compose up -d

Then do

    sbt core/run

or if you have bloop set up you can do

    bloop run core

The go to http://localhost:8080 and play around.
For example try this query, to see a list of cities starting with "Bei":

    query {
        cities(namePattern: "Bei%") {
            name
        }
    }

When you're done, ^C to kill the Scala server and

    docker-compose down

to shut down the database.
