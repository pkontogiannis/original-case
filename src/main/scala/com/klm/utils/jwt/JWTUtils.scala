package com.klm.utils.jwt

import com.klm.service.domain.UserModel.Token
import com.klm.service.errors.ServiceError
import com.klm.service.errors.ServiceError.AuthenticationError
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import pdi.jwt.{Jwt, JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.Clock
import java.util.UUID
import scala.util.{Failure, Success, Try}

object JWTUtils extends LazyLogging {

  val config: Config = ConfigFactory.load()

  private val tokenPrefix: String                              = config.getString("authentication.token.prefix")
  private val secretKey: String                                = config.getString("authentication.token.secret")
  private val algorithm: JwtAlgorithm.HS256.type               = JwtAlgorithm.HS256
  private val acceptedAlgorithms: Seq[JwtAlgorithm.HS256.type] = Seq(algorithm)
  private val accessTokenExpiration: Int                       = config.getInt("authentication.token.access")
  private val refreshTokenExpiration: Int                      = config.getInt("authentication.token.refresh")

  implicit val clock: Clock = Clock.systemDefaultZone()

  def getAccessToken(userId: UUID, role: String): Token = {
    val jwtClaim: JwtClaim = issueJWT(userId, role, accessTokenExpiration)
    val jwtToken: String   = Jwt.encode(jwtClaim, secretKey, JwtAlgorithm.HS256)
    logger.info(s"[${this.getClass.getSimpleName}] successfully generate an access token for the user with uuid: $userId")
    Token(s"$tokenPrefix$jwtToken", accessTokenExpiration)
  }

  def getRefreshToken(userId: UUID, role: String): Token = {
    val jwtClaim: JwtClaim = issueJWT(userId, role, refreshTokenExpiration)
    val jwtToken: String   = Jwt.encode(jwtClaim, secretKey, JwtAlgorithm.HS256)
    logger.info(s"[${this.getClass.getSimpleName}] successfully generate an refresh token for the user with uuid: $userId")
    Token(s"$tokenPrefix$jwtToken", refreshTokenExpiration)
  }

  private def issueJWT(userId: UUID, role: String, tokenExpiration: Int): JwtClaim =
    JwtClaim(subject = Some(userId.toString), issuer = Some(role)).issuedNow
      .expiresIn(tokenExpiration)

  def validateToken(token: String): Either[AuthenticationError, Boolean] =
    // If you only want to check if a token is valid without decoding it.
    // All good
    //    Jwt.validate(accessToken, secretKey, Seq(JwtAlgorithm.HS256))
    extractTokenBody(token) match {
      case Right(extractedToken) => tokenIsValid(extractedToken)
      case Left(_) => Left(AuthenticationError())
    }

  def tokenIsValid(token: String): Either[ServiceError.AuthenticationError, Boolean] =
    if (Jwt.isValid(token, secretKey, Seq(JwtAlgorithm.HS256))) {
      Right(true)
    } else {
      Left(AuthenticationError())
    }

  def decodeToken(token: String): Either[AuthenticationError, Claims] = {
    val extractedToken: Either[AuthenticationError, String] = extractTokenBody(token)
    Jwt.decodeRaw(
      extractedToken.getOrElse(""),
      secretKey,
      acceptedAlgorithms
    ) match {
      case Failure(_) => Left(AuthenticationError())
      case Success(jwtClaim) =>
        Claims(JwtCirce.parseClaim(jwtClaim)) match {
          case Left(error) => Left(error)
          case Right(claims) =>
            Right(claims)
        }
    }
  }

  def extractTokenBody(token: String): Either[AuthenticationError, String] = token match {
    case tok if tok.startsWith(tokenPrefix) =>
      Right(tok.substring(tokenPrefix.length()))
    case _ => Left(AuthenticationError())
  }

  def extractClaims(token: String): Option[Claims] =
    JwtCirce.decode(token, secretKey, Seq(algorithm)).toOption.flatMap { c =>
      for {
        userId <- c.subject.flatMap(s => Try(UUID.fromString(s.toString)).toOption)
        expiration <- c.expiration.filter(_ > currentTimeSeconds)
        issuedAt <- c.issuedAt.filter(_ <= System.currentTimeMillis())
        role <- c.issuer
      } yield Claims(userId, issuedAt, expiration, role)
    }

  private def currentTimeSeconds: Long = System.currentTimeMillis() / 1000

}
