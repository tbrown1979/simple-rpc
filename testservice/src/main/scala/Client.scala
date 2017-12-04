package com.tbrown

import cats.{Monad, MonadError}
import cats.implicits._

import freestyle.rpc.client._
import freestyle.rpc.RPCAsyncImplicits

import io.grpc.ManagedChannel

import journal.Logger

import protocol._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ClientService[F[_]](client: RPCService.Client[F])(implicit M: MonadError[F, Throwable]) {
  def getUserInfo(id: UserId): F[UserInfoResponse[UserInfo]] = client.getUserInfo(GetUserInfo(id))
}

object ClientConf extends RPCAsyncImplicits {
  import freestyle.config.implicits._
  import freestyle.implicits._

  val channelFor: ManagedChannelFor =
      ConfigForAddress[ChannelConfig.Op]("rpc.client.host", "rpc.client.port")
        .interpret[Try] match {
        case Success(c) => c
        case Failure(e) =>
          e.printStackTrace()
          throw new RuntimeException("Unable to load the client configuration", e)
      }

  val channelConfigList: List[ManagedChannelConfig] = List(UsePlaintext(true))

  val managedChannelInterpreter =
    new ManagedChannelInterpreter[Future](channelFor, channelConfigList)

  val channel: ManagedChannel = managedChannelInterpreter.build(channelFor, channelConfigList)

  implicit val S: monix.execution.Scheduler =
    monix.execution.Scheduler.Implicits.global

  val client: RPCService.Client[Future] = RPCService.client[Future](channel)
}

object ClientProgramStuff {
  import cats.syntax.all._

  def clientProgram[M[_]: Monad](APP: ClientService[M]): M[Unit] =
    APP.getUserInfo(UserId("1234")).map(resp => println(resp))
}

object ThisIsTheClientApp extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  val logger: Logger = Logger[this.type]

  logger.info(s"${Thread.currentThread().getName} Starting client, interpreting to Future ...")

  Await.result(ClientProgramStuff.clientProgram(new ClientService(ClientConf.client)), Duration.Inf)

  logger.info(s"${Thread.currentThread().getName} Finishing program interpretation ...")

  (): Unit
  System.in.read()
}
