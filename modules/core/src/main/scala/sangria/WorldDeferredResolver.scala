package demo.sangria

import cats.data._
import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import demo.repo._
import sangria.execution.deferred.{ Deferred, DeferredResolver }
import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.util.{ Success, Failure, Try }

trait WorldDeferredResolver[F[_]] extends DeferredResolver[MasterRepo[F]]

object WorldDeferredResolver {

  def apply[F[_]: Effect]: WorldDeferredResolver[F] =
    new WorldDeferredResolver[F] {

      def resolve(deferred: Vector[Deferred[Any]], ctx: MasterRepo[F], queryState: Any)(implicit ec: ExecutionContext): Vector[Future[Any]] = {

        // Ok this is kind of grim because there's no type safety and the return type forces us to
        // use promises and complete them asynchronously because there's no other way to get
        // batching with the desired output type.

        val promises: Map[Deferred[Any], Promise[Any]] =
          deferred.map(d => d -> Promise[Any]()).toMap

        val futures: Vector[Future[Any]] =
          deferred.map(promises(_).future)

        val countryCodes: List[String] =
          promises.keys.toList.collect {
            case CountryType.Deferred.ByCode(code) => code
            case x => sys.error(s"Unknown deferred value: $x")
          }

        // side-effect woo!
        ctx.country.fetchByCodes(countryCodes).evalMap { c =>
          Effect[F].delay { promises(CountryType.Deferred.ByCode(c.code)).complete(Success(c)) }
        } .compile.drain.toIO.unsafeToFuture

        // so the deal is, if any of the futures fail to complete we hang forever. how about that?

        futures
      }

    }

}