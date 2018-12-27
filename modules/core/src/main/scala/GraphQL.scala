// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

import cats._
import cats.effect._
import cats.implicits._
import io.circe._
import io.circe.optics.JsonPath._

/** An algebra of operations in F that evaluate GraphQL requests. */
trait GraphQL[F[_]] {

  /**
   * Executes a JSON-encoded request in the standard POST encoding, described thus in the spec:
   *
   * A standard GraphQL POST request should use the application/json content type, and include a
   * JSON-encoded body of the following form:
   *
   * {
   *   "query": "...",
   *   "operationName": "...",
   *   "variables": { "myVariable": "someValue", ... }
   * }
   *
   * `operationName` and `variables` are optional fields. `operationName` is only required if
   * multiple operations are present in the query.
   * @return either an error Json or result Json
   */
  def query(request: Json): F[Either[Json, Json]]

  /** Executes a request given a `query`, optional `operationName`, and `varianbles`. */
  def query(query: String, operationName: Option[String], variables: JsonObject): F[Either[Json, Json]]

}

object GraphQL {

  /** Constructor for a GraphQL instance based on Sangria. */
  object fromSangria {

    // Only the Sangria implementation needs these
    import sangria.execution._
    import sangria.marshalling.circe._
    import sangria.parser.{ QueryParser, SyntaxError }
    import sangria.schema._
    import sangria.ast._
    import sangria.execution.WithViolations
    import sangria.validation._
    import scala.concurrent.ExecutionContext
    import scala.util.{ Success, Failure }

    // Some circe lenses
    private val queryStringLens   = root.query.string
    private val operationNameLens = root.operationName.string
    private val variablesLens     = root.variables.obj

    private def formatSyntaxError(e: SyntaxError): Json = Json.obj(
      "errors" -> Json.arr(Json.obj(
        "message"   -> Json.fromString(e.getMessage),
        "locations" -> Json.arr(Json.obj(
          "line"   -> Json.fromInt(e.originalError.position.line),
          "column" -> Json.fromInt(e.originalError.position.column))))))

    private def formatWithViolations(e: WithViolations): Json = Json.obj(
      "errors" -> Json.fromValues(e.violations.map {
        case v: AstNodeViolation => Json.obj(
          "message"   -> Json.fromString(v.errorMessage),
          "locations" -> Json.fromValues(v.locations.map(loc => Json.obj(
            "line"   -> Json.fromInt(loc.line),
            "column" -> Json.fromInt(loc.column)))))
        case v => Json.obj(
          "message" -> Json.fromString(v.errorMessage))}))

    // Format a string as a GraphQL `errors`
    private def formatString(s: String): Json = Json.obj(
      "errors" -> Json.arr(Json.obj(
        "message" -> Json.fromString(s))))

    // Format an exception as a GraphQL `errors`
    private def formatThrowable(e: Throwable): Json = Json.obj(
      "errors" -> Json.arr(Json.obj(
        "class"   -> Json.fromString(e.getClass.getName),
        "message" -> Json.fromString(e.getMessage))))

    // Partially-applied constructor
    def apply[F[_]] = new Partial[F]
    class Partial[F[_]] {

      // The rest of the constructor
      def apply[A](schema: Schema[A, Unit], userContext: F[A], blockingExecutionContext: ExecutionContext)(
        implicit F: MonadError[F, Throwable],
                 L: LiftIO[F]
      ): GraphQL[F] =
        new GraphQL[F] {

          // Destructure `request` and delegat to the other overload.
          def query(request: Json): F[Either[Json, Json]] = {
            val queryString   = queryStringLens.getOption(request)
            val operationName = operationNameLens.getOption(request)
            val variables     = variablesLens.getOption(request).getOrElse(JsonObject())
            queryString match {
              case Some(qs) => query(qs, operationName, variables)
              case None     => fail(formatString("No 'query' property was present in the request."))
            }
          }

          // Parse `query` and execute.
          def query(query: String, operationName: Option[String], variables: JsonObject): F[Either[Json, Json]] = {
           val q =  QueryParser.parse(query)
            println(q)
           q match {
              case Success(ast) => exec(schema, userContext, ast, operationName, variables)(blockingExecutionContext)
              case Failure(e@SyntaxError(_, _, pe)) => fail(formatSyntaxError(e))
              case Failure(e) => fail(formatThrowable(e))
            }
          }

          // Lift a `Json` into the error side of our effect.
          def fail(j: Json): F[Either[Json, Json]] =
            F.pure(j.asLeft)

          // Execute a GraphQL query with Sangria, lifting into IO for safety and sanity.
          def exec(
            schema:        Schema[A, Unit],
            userContext:   F[A],
            query:         Document,
            operationName: Option[String],
            variables:     JsonObject
          )(implicit ec: ExecutionContext): F[Either[Json, Json]] =
            userContext.flatMap { ctx =>
              IO.fromFuture {
                IO {
                  Executor.execute(schema, query, ctx,
                    variables        = Json.fromJsonObject(variables),
                    operationName    = operationName,
                    exceptionHandler = ExceptionHandler {
                      case (_, e) ⇒ HandledException(e.getMessage)
                    }
                  )
                }
              } .to[F]
            } .attempt.flatMap {
              case Right(json)               => F.pure(json.asRight)
              case Left(err: WithViolations) => fail(formatWithViolations(err))
              case Left(err)                 => fail(formatThrowable(err))
            }

        }
      }

    }

}
