package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import services.Mcrl2Actor

import javax.inject.{Inject, Singleton}

/**
  * Created by guillecledou on 16/01/2019
  */

@Singleton
class Mcrl2Controller @Inject()(cc:ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      Mcrl2Actor.props(out)
    }
  }

}
