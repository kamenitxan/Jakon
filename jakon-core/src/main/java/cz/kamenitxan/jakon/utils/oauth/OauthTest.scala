package cz.kamenitxan.jakon.utils.oauth

import java.util.Scanner

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.{OAuthRequest, Verb}
import cz.kamenitxan.jakon.utils.security.oauth.Google

import scala.util.Random

object OauthTest {

	def main(args: Array[String]): Unit = {
		magic()
	}

	def magic() = {
		val clientId = Google.clientId
		val clientSecret = Google.clientSecret
		val secretState = "secret" + new Random().nextInt(999)

		val service = new ServiceBuilder(clientId)
		  .apiSecret(clientSecret)
		  .defaultScope("email")
		  .callback("http://localhost:4567/callback")
		  .build(GoogleApi20.instance)

		import com.github.scribejava.core.model.OAuth2AccessToken
		// Obtain the Authorization URL// Obtain the Authorization URL

		System.out.println("Fetching the Authorization URL...")
		//pass access_type=offline to get refresh token
		//https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
		val additionalParams = new java.util.HashMap[String, String]
		additionalParams.put("access_type", "offline")
		//force to reget refresh token (if usera are asked not the first time)
		additionalParams.put("prompt", "consent")
		val authorizationUrl: String = service.createAuthorizationUrlBuilder.state(secretState).additionalParams(additionalParams).build
		System.out.println("Got the Authorization URL!")
		System.out.println("Now go and authorize ScribeJava here:")
		System.out.println(authorizationUrl)
		System.out.println("And paste the authorization code here")
		System.out.print(">>")

		val in = new Scanner(System.in, "UTF-8")

		val code: String = in.nextLine
		System.out.println()

		System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.")
		System.out.print(">>")
		val value: String = in.nextLine
		if (secretState == value) {
			System.out.println("State value does match!")
		}
		else {
			System.out.println("Ooops, state value does not match!")
			System.out.println("Expected = " + secretState)
			System.out.println("Got      = " + value)
			System.out.println()
		}

		// Trade the Request Token and Verfier for the Access Token
		System.out.println("Trading the Request Token for an Access Token...")
		var accessToken: OAuth2AccessToken = service.getAccessToken(code)
		System.out.println("Got the Access Token!")
		System.out.println("(The raw response looks like this: " + accessToken.getRawResponse + "')")

		System.out.println("Refreshing the Access Token...")
		accessToken = service.refreshAccessToken(accessToken.getRefreshToken)
		System.out.println("Refreshed the Access Token!")
		System.out.println("(The raw response looks like this: " + accessToken.getRawResponse + "')")

		val req = new OAuthRequest(Verb.GET, "https://openidconnect.googleapis.com/v1/userinfo?email")
		service.signRequest(accessToken, req)

		val response = service.execute(req)
		println(response)
		System.out.println(response.getCode)
		System.out.println(response.getBody)
	}
}
