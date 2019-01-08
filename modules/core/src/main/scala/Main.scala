// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo

import cats.effect._
import cats.implicits._
import demo.sangria.SangriaGraphQL
import doobie._
import doobie.util.ExecutionContexts
import io.circe.Json
import repo._
import schema._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  // Construct a transactor for connecting to the database.
  def transactor[F[_]: Async: ContextShift] =
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:world",
      "user",
      "password"
    )

  // Construct a GraphQL implementation based on our Sangria definitions.
  def graphQL[F[_]: Effect: ContextShift](
    transactor:      Transactor[F],
    blockingContext: ExecutionContext
  ): GraphQL[F] =
    SangriaGraphQL[F](
      CitySchema.schema,
      CityRepo.fromTransactor(transactor).pure[F],
      blockingContext
    )

  // Construct our routes, delegating real work to `graphQL`.
  def routes[F[_]: Sync: ContextShift](
    graphQL:         GraphQL[F],
    blockingContext: ExecutionContext
  ): HttpRoutes[F] = {

    object dsl extends Http4sDsl[F]; import dsl._

    HttpRoutes.of[F] {

      case GET -> Root / "playground.html" =>
        StaticFile
          .fromResource[F]("/assets/playground.html", blockingContext)
          .getOrElseF(NotFound())

      case req @ POST -> Root / "graphql" ⇒
        req.as[Json].flatMap(graphQL.query).flatMap {
          case Right(json) => Ok(json)
          case Left(json)  => BadRequest(json)
        }

      case _ =>
        PermanentRedirect(Location(Uri.uri("/playground.html")))

    }
  }

  // Resource that mounts the given `routes` and starts a server.
  def server[F[_]: ConcurrentEffect: ContextShift: Timer](
    routes: HttpRoutes[F]
  ): Resource[F, Server[F]] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes.orNotFound)
      .resource

  // Resource that constructs our final server.
  def resource[F[_]: ConcurrentEffect: ContextShift: Timer]: Resource[F, Server[F]] =
    for {
      bec <- ExecutionContexts.cachedThreadPool[F]
      xa   = transactor[F]
      gql  = graphQL[F](xa, bec)
      rts  = routes(gql, bec)
      svr <- server[F](rts)
    } yield svr

  // Our entry point starts the server and blocks forever.
  def run(args: List[String]): IO[ExitCode] =
    resource[IO].use(_ => IO.never.as(ExitCode.Success))

}

