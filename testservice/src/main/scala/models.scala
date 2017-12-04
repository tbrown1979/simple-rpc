package com.tbrown

import cats.implicits._

sealed trait UserInfoError
case object ItFailed extends UserInfoError
case object UnexpectedCase extends UserInfoError


case class UserInfoResponse[A](left: Option[UserInfoError], right: Option[A]) {
  def value: Either[UserInfoError, A] =
    left
      .map(_.asLeft[A])
      .orElse(right.map(_.asRight[UserInfoError]))
      .getOrElse(UnexpectedCase.asLeft[A])
}

object UserInfoResponse {
  def success[A](a: A): UserInfoResponse[A] = UserInfoResponse(None, Some(a))
  def fail[A](error: UserInfoError): UserInfoResponse[A] = UserInfoResponse(Some(error), None)
}