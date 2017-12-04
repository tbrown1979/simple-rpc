package com.tbrown

import cats.~>
import cats.implicits._

import freestyle._
import freestyle.rpc.RPCAsyncImplicits
import freestyle.rpc.server.{GrpcConfig, ServerConfig, ServerW}
import freestyle.rpc.server._
import freestyle.rpc.server.handlers.GrpcServerHandler

import journal.Logger

import protocol._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

//do we need to capture here?
//can we just do F.pure?
class RPCServer[F[_]](C: Capture[F]) extends RPCService[F] {

  def getUserInfo(userInfo: GetUserInfo): F[UserInfoResponse[UserInfo]] =
    if (userInfo.userId.value == "Hello")
      C.capture(UserInfoResponse.fail[UserInfo](ItFailed))
    else
      C.capture(UserInfoResponse.success(UserInfo(UserId(""), MemberNumber(""), Nil)))
}

object ServerConf {
  def getConf(grpcConfigs: List[GrpcConfig]): ServerW = {
    import cats.implicits._
    import freestyle.implicits._
    import freestyle.config.implicits._

    BuildServerFromConfig[ServerConfig.Op]("rpc.server.port", grpcConfigs)
      .interpret[Try] match {
      case Success(c) => c
      case Failure(e) =>
        e.printStackTrace()
        throw new RuntimeException("Unable to load the server configuration", e)
    }
  }
}

object ThisIsTheServer extends App with RPCAsyncImplicits {
  import freestyle.async.implicits._
  import freestyle.rpc.client.implicits._
  import freestyle.rpc.server.GrpcServerApp
  import freestyle.rpc.server.implicits._

  import freestyle.Capture._
  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  //needed for .bindService
  implicit val x: RPCService[Future] = new RPCServer[Future](Capture.freeStyleFutureCaptureInstance(ec))

  val grpcConfigs: List[GrpcConfig] = List(
    AddService(RPCService.bindService[Future])
  )

  implicit val grpcServerHandler: GrpcServer.Op ~> Future =
    new GrpcServerHandler[Future] andThen
      new GrpcKInterpreter[Future](ServerConf.getConf(grpcConfigs).server)


  val logger: Logger = Logger[this.type]


  logger.info(s"Server is starting ...")

  Await.result(server[GrpcServerApp.Op].bootstrapFuture, Duration.Inf)

}
