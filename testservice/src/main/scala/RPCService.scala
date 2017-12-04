package com.tbrown

import freestyle.rpc.protocol._

object protocol {
  case class UserId(value: String)
  case class AccountId(value: String)

  case class MemberNumber(value: String)
  case class Balance(value: BigDecimal)
  case class Account(accountId: AccountId, balance: Balance)

  case class GetUserInfo(userId: UserId)
  case class UserInfo(userId: UserId, memberNumber: MemberNumber, accounts: List[Account])
}

import protocol._

@service
trait RPCService[F[_]] {
  @rpc(Avro) def getUserInfo(point: GetUserInfo): F[UserInfoResponse[UserInfo]]
}