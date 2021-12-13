package jwt

import com.klm.service.domain.UserModel
import com.klm.utils.jwt.JWTUtils
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.funspec.AnyFunSpecLike

import java.util.UUID
import scala.jdk.CollectionConverters.CollectionHasAsScala

class JWTUtilsSpec extends AnyFunSpecLike {

  val config: Config                      = ConfigFactory.load()
  val userId: UUID                        = UUID.randomUUID()
  val roles: List[String]                 = config.getStringList("authentication.roles").asScala.toList
  private val accessTokenExpiration: Int  = config.getInt("authentication.token.access")
  private val refreshTokenExpiration: Int = config.getInt("authentication.token.refresh")

  describe("JWT") {
    it("Successfully generate an Access Token") {
      val accessToken: UserModel.Token = JWTUtils.getAccessToken(userId, roles.head)
      assert(accessToken.token.startsWith("Bearer "))
      assert(accessToken.expiresIn == accessTokenExpiration)
    }
    it("Successfully generate an Refresh Token") {
      val accessToken: UserModel.Token = JWTUtils.getRefreshToken(userId, roles.head)
      assert(accessToken.token.startsWith("Bearer "))
      assert(accessToken.expiresIn == refreshTokenExpiration)
    }

  }

}
