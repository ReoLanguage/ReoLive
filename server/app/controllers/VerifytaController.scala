package controllers

import java.io.File

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import services.VerifytaActor

/**
  * Created by guillecledou on 2019-10-07
  */

@Singleton
class VerifytaController@Inject()(cc:ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      VerifytaActor.props(out)
    }
  }

}