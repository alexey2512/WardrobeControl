package com.myapp.api

import cats.effect.Async
import cats.syntax.functor._
import com.myapp.business._
import com.myapp.config.AppConfig
import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import org.http4s.HttpRoutes
import pureconfig.module.catseffect.syntax._
import pureconfig._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import tofu.logging.Loggable._

class Router[F[_]: Async](
  orgLogic: OrgLogic[F],
  personLogic: PersonLogic[F],
  wardrobeLogic: WardrobeLogic[F],
  hookLogic: HookLogic[F],
  helpers: LogHelpers[F]
) {
  import helpers._

  private val swaggerEndpoints: F[List[ServerEndpoint[Any, F]]] =
    ConfigSource.default.loadF[F, AppConfig]().map { conf =>
      SwaggerInterpreter().fromEndpoints(
        List(
          OrgEndpoints.orgRegEndpoint,
          OrgEndpoints.orgInfoEndpoint,
          OrgEndpoints.orgDelEndpoint,
          PersonEndpoints.personRegEndpoint,
          PersonEndpoints.personInfoEndpoint,
          PersonEndpoints.personDelEndpoint,
          WardrobeEndpoints.wardrobeRegEndpoint,
          WardrobeEndpoints.wardrobeInfoEndpoint,
          WardrobeEndpoints.wardrobeDelEndpoint,
          HookEndpoints.enableHookEndpoint,
          HookEndpoints.disableHookEndpoint,
          HookEndpoints.hookActionEndpoint
        ),
        conf.name,
        conf.version
      )
    }

  private val orgEndpoints = List(
    OrgEndpoints.orgRegEndpoint
      .serverSecurityLogic[Unit, F](
        logWrapper[OwnerToken, ApiError, Unit](
          orgLogic.orgRegSecurityLogic,
          "org/reg[SECURITY]"
        )
      )
      .serverLogic(_ =>
        logWrapper[OrgRegRequest, ApiError, OrgRegResponse](
          orgLogic.orgRegBusinessLogic,
          "org/reg[BUSINESS]"
        )
      ),
    OrgEndpoints.orgInfoEndpoint
      .serverSecurityLogic[OrgInfoResponse, F](
        logWrapper[(OwnerToken, OrgToken), ApiError, OrgInfoResponse](
          (orgLogic.orgInfoSecurityLogic _).tupled,
          "org/info[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[OrgInfoResponse, ApiError, OrgInfoResponse](
            orgLogic.orgInfoBusinessLogic,
            "org/info[BUSINESS]"
          ).apply(res)
      ),
    OrgEndpoints.orgDelEndpoint
      .serverSecurityLogic[OrgInfoResponse, F](
        logWrapper[(OwnerToken, OrgToken), ApiError, OrgInfoResponse](
          (orgLogic.orgDelSecurityLogic _).tupled,
          "org/del[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[OrgInfoResponse, ApiError, OrgInfoResponse](
            orgLogic.orgDelBusinessLogic,
            "org/del[BUSINESS]"
          ).apply(res)
      )
  )

  private val personEndpoints = List(
    PersonEndpoints.personRegEndpoint
      .serverSecurityLogic[OrgId, F](
        logWrapper[OrgToken, ApiError, OrgId](
          personLogic.personRegSecurityLogic,
          "person/reg[SECURITY]"
        )
      )
      .serverLogic(orgId =>
        personReq =>
          logWrapper[(OrgId, PersonRegRequest), ApiError, PersonRegResponse](
            (personLogic.personRegBusinessLogic _).tupled,
            "person/reg[BUSINESS]"
          ).apply((orgId, personReq))
      ),
    PersonEndpoints.personInfoEndpoint
      .serverSecurityLogic[PersonInfoResponse, F](
        logWrapper[(OrgToken, PersonToken), ApiError, PersonInfoResponse](
          (personLogic.personInfoSecurityLogic _).tupled,
          "person/info[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[PersonInfoResponse, ApiError, PersonInfoResponse](
            personLogic.personInfoBusinessLogic,
            "person/info[BUSINESS]"
          ).apply(res)
      ),
    PersonEndpoints.personDelEndpoint
      .serverSecurityLogic[PersonInfoResponse, F](
        logWrapper[(OrgToken, PersonToken), ApiError, PersonInfoResponse](
          (personLogic.personDelSecurityLogic _).tupled,
          "person/del[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[PersonInfoResponse, ApiError, PersonInfoResponse](
            personLogic.personDelBusinessLogic,
            "person/del[BUSINESS]"
          ).apply(res)
      )
  )

  private val wardrobeEndpoints = List(
    WardrobeEndpoints.wardrobeRegEndpoint
      .serverSecurityLogic[OrgId, F](
        logWrapper[OrgToken, ApiError, OrgId](
          wardrobeLogic.wardrobeRegSecurityLogic,
          "wardrobe/reg[SECURITY]"
        )
      )
      .serverLogic(org =>
        req =>
          logWrapper[
            (OrgId, WardrobeRegRequest),
            ApiError,
            WardrobeRegResponse
          ](
            (wardrobeLogic.wardrobeRegBusinessLogic _).tupled,
            "wardrobe/reg[BUSINESS]"
          ).apply((org, req))
      ),
    WardrobeEndpoints.wardrobeInfoEndpoint
      .serverSecurityLogic[WardrobeInfoResponse, F](
        logWrapper[(OrgToken, WardrobeToken), ApiError, WardrobeInfoResponse](
          (wardrobeLogic.wardrobeInfoSecurityLogic _).tupled,
          "wardrobe/info[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[WardrobeInfoResponse, ApiError, WardrobeInfoResponse](
            wardrobeLogic.wardrobeInfoBusinessLogic,
            "wardrobe/info[BUSINESS]"
          ).apply(res)
      ),
    WardrobeEndpoints.wardrobeDelEndpoint
      .serverSecurityLogic[WardrobeInfoResponse, F](
        logWrapper[(OrgToken, WardrobeToken), ApiError, WardrobeInfoResponse](
          (wardrobeLogic.wardrobeDelSecurityLogic _).tupled,
          "wardrobe/del[SECURITY]"
        )
      )
      .serverLogic(res =>
        _ =>
          logWrapper[WardrobeInfoResponse, ApiError, WardrobeInfoResponse](
            wardrobeLogic.wardrobeDelBusinessLogic,
            "wardrobe/del[BUSINESS]"
          ).apply(res)
      )
  )

  private val hookEndpoints = List(
    HookEndpoints.enableHookEndpoint
      .serverSecurityLogic[WardrobeId, F](
        logWrapper[WardrobeToken, ApiError, WardrobeId](
          hookLogic.enableHookSecurityLogic,
          "hook/enable[SECURITY]"
        )
      )
      .serverLogic(wid =>
        req =>
          logWrapper[(WardrobeId, HookNumberRequest), ApiError, Unit](
            (hookLogic.enableHookBusinessLogic _).tupled,
            "hook/enable[BUSINESS]"
          ).apply((wid, req))
      ),
    HookEndpoints.disableHookEndpoint
      .serverSecurityLogic[WardrobeId, F](
        logWrapper[WardrobeToken, ApiError, WardrobeId](
          hookLogic.disableHookSecurityLogic,
          "hook/disable[SECURITY]"
        )
      )
      .serverLogic(wid =>
        req =>
          logWrapper[(WardrobeId, HookNumberRequest), ApiError, Unit](
            (hookLogic.disableHookBusinessLogic _).tupled,
            "hook/disable[BUSINESS]"
          ).apply((wid, req))
      ),
    HookEndpoints.hookActionEndpoint
      .serverSecurityLogic[(WardrobeId, PersonId), F](
        logWrapper[
          (WardrobeToken, PersonToken),
          ApiError,
          (WardrobeId, PersonId)
        ](
          (hookLogic.hookActionSecurityLogic _).tupled,
          "hook/action[SECURITY]"
        )
      )
      .serverLogic { case (wrId, psId) =>
        req =>
          logWrapper[
            (WardrobeId, PersonId, HookActionRequest),
            ApiError,
            HookActionResponse
          ](
            (hookLogic.hookActionBusinessLogic _).tupled,
            "hook/action[BUSINESS]"
          ).apply((wrId, psId, req))
      }
  )

  val httpRoutes: F[HttpRoutes[F]] = for {
    swg <- swaggerEndpoints
    routes = Http4sServerInterpreter[F]().toRoutes(
      orgEndpoints ++
        personEndpoints ++
        wardrobeEndpoints ++
        hookEndpoints ++
        swg ++
        List(PrometheusMetrics.default[F]().metricsEndpoint)
    )
  } yield routes

}
