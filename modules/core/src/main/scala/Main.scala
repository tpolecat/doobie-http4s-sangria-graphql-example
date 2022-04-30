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
import repo._
import _root_.sangria.schema._
import cats.effect.std.Dispatcher
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl._
import org.http4s.headers.Location
import org.http4s.server.Server
import cats.effect.unsafe
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4s.implicits._

object Main extends IOApp {
  implicit val ioRuntime = unsafe.IORuntime
  implicit val ec        = ioRuntime.global.compute

  val hikariDataSourceConfig = {
    val config = new HikariConfig()
    config.setDriverClassName("org.postgresql.Driver")
    config.setJdbcUrl("jdbc:postgresql:world")
    config.setUsername("user")
    config.setPassword("password")
    config.setMaximumPoolSize(5)
    config
  }

  def transactor[F[_]: Async]: IO[Transactor[F]] =
    IO.pure(
      HikariTransactor
        .apply[F](new HikariDataSource(hikariDataSourceConfig), ioRuntime.global.compute)
    )

  // Construct a GraphQL implementation based on our Sangria definitions.
  def graphQLServerFor[F[_]: Async](
    dispatcher: Dispatcher[F],
    transactor: Transactor[F]
  ): GraphQL[F] =
    SangriaGraphQL[F](
      Schema(query = QueryType.apply[F](dispatcher), mutation = Some(MutationType[F](dispatcher))),
      WorldDeferredResolver[F],
      MasterRepo.fromTransactor(transactor).pure[F],
      ec
    )

  // Playground or else redirect to playground
  def playgroundOrElse[F[_]: Async]: HttpRoutes[F] = {
    object dsl extends Http4sDsl[F];
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "playground.html" =>
        StaticFile.fromResource[F]("/assets/playground.html").getOrElseF(NotFound())

      case _ => PermanentRedirect(Location(uri"/playground.html"))

    }
  }

  // Resource that mounts the given `routes` and starts a server.
  def server[F[_]: Async](routes: HttpRoutes[F]): Resource[F, Server] =
    BlazeServerBuilder[F]
      .withExecutionContext(ioRuntime.global.compute)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .resource

  // Our entry point starts the server and blocks forever.
  def run(args: List[String]): IO[ExitCode] = {
    val resource = for {
      xa           <- Resource.eval(transactor[IO])
      dispatcher   <- Dispatcher[IO]
      graphQlServer = graphQLServerFor[IO](dispatcher, xa)
      routes        = GraphQLRoutes[IO](graphQlServer) <+> playgroundOrElse
      server       <- server[IO](routes)
    } yield server

    resource.use(_ => IO.never.as(ExitCode.Success))
  }

}
