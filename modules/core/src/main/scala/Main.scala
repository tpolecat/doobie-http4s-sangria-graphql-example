// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo

import cats.effect._
import cats.implicits._
import demo.sangria.SangriaGraphQL
import demo.schema._
import doobie._
import doobie.hikari._
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import repo._
import sangria._
import _root_.sangria.schema._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext

object Main extends IOApp {

  // Construct a transactor for connecting to the database.
  def transactor[F[_]: Async: ContextShift](
    bec: ExecutionContext
  ): Resource[F, HikariTransactor[F]] =
    ExecutionContexts.fixedThreadPool[F](10).flatMap { ce =>
      HikariTransactor.newHikariTransactor(
        "org.postgresql.Driver",
        "jdbc:postgresql:world",
        "user",
        "password",
        ce,
        bec
      )
    }

  // Construct a GraphQL implementation based on our Sangria definitions.
  def graphQL[F[_]: Effect: ContextShift: Logger](
    transactor:      Transactor[F],
    blockingContext: ExecutionContext
  ): GraphQL[F] =
    SangriaGraphQL[F](
      Schema(
        query    = QueryType[F],
        mutation = Some(MutationType[F])
      ),
      WorldDeferredResolver[F],
      MasterRepo.fromTransactor(transactor).pure[F],
      blockingContext
    )

  // Playground or else redirect to playground
  def playgroundOrElse[F[_]: Sync: ContextShift](
    blockingContext: ExecutionContext
  ): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "playground.html" =>
        StaticFile
          .fromResource[F]("/assets/playground.html", blockingContext)
          .getOrElseF(NotFound())

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
  def resource[F[_]: ConcurrentEffect: ContextShift: Timer](
    implicit L: Logger[F]
  ): Resource[F, Server[F]] =
    for {
      bec <- ExecutionContexts.cachedThreadPool[F]
      xa  <- transactor[F](bec)
      gql  = graphQL[F](xa, bec)
      rts  = GraphQLRoutes[F](gql) <+> playgroundOrElse(bec)
      svr <- server[F](rts)
    } yield svr

  // Our entry point starts the server and blocks forever.
  def run(args: List[String]): IO[ExitCode] = {
    implicit val log = Slf4jLogger.getLogger[IO]
    resource[IO].use(_ => IO.never.as(ExitCode.Success))
  }

}

