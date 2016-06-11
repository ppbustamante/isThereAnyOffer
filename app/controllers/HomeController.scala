package controllers

import javax.inject.Inject

import actors.SearchActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.daos.{GameDAO, GenreDAO, OfferDAO, PlatformDAO}
import models.entities.{Game, Offer}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, AnyContent, Controller, WebSocket}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class HomeController @Inject()(gameDAO: GameDAO,
                               offerDAO: OfferDAO,
                               platformDAO: PlatformDAO,
                               genreDAO: GenreDAO)(implicit ec:ExecutionContext, system: ActorSystem, mat:Materializer) extends Controller {

    def index() = Action.async { implicit request =>
        for {
            tuplesPlatformCount <- platformDAO.allPlatformsWithCount
            tuplesGenreCount <- genreDAO.allGenresWithCount
            tuplesOfferGamePLatform <- offerDAO.actualOffers  //obtencion de las ofertas
        } yield Ok(views.html.home(
            title = "Is There Any Offer - Inicio",
            tuplesPlatformCount = tuplesPlatformCount.toList,
            tuplesGenreCount = tuplesGenreCount.toList,
            tuplesOfferGame = tuplesOfferGamePLatform.toList
        ))
    }

    def search() = Action(parse.urlFormEncoded) { request =>
        Redirect(routes.ResultController.index(request.body.get("search").head.head))
    }

    // JsValue ~ JSON
    def socket = WebSocket.accept[JsValue, JsValue] { request =>
        ActorFlow.actorRef(out => SearchActor.props(out,gameDAO))
    }
}
