package utils

import cz.kamenitxan.jakon.utils.security.AesEncryptor
import org.scalatest.FunSuite

class AesEncryptorTest extends FunSuite {

	test("encrypt&decrypt") {
		val toenc = "slaninaabůček534534345354345345345345432543254432543254325"
		val enc = AesEncryptor.encrypt(toenc)
		val dec = AesEncryptor.decrypt(enc)
		assert(toenc == dec)
	}
}
