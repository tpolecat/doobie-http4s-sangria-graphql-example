// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo

import io.circe.Json
import io.circe.JsonObject

/**
 * An algebra of operations in F that evaluate GraphQL requests.
 */
trait GraphQL[F[_]] {

  /**
   * Executes a JSON-encoded request in the standard POST encoding, described
   * thus in the spec:
   *
   * A standard GraphQL POST request should use the application/json content
   * type, and include a JSON-encoded body of the following form:
   *
   * { "query": "...", "operationName": "...", "variables": { "myVariable":
   * "someValue", ... } }
   *
   * `operationName` and `variables` are optional fields. `operationName` is
   * only required if multiple operations are present in the query.
   * @return
   *   either an error Json or result Json
   */
  def query(request: Json): F[Either[Json, Json]]

  /**
   * Executes a request given a `query`, optional `operationName`, and
   * `varianbles`.
   * @return
   *   either an error Json or result Json
   */
  def query(
    query: String,
    operationName: Option[String],
    variables: JsonObject
  ): F[Either[Json, Json]]

}
