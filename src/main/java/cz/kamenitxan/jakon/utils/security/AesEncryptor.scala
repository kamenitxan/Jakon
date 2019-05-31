package cz.kamenitxan.jakon.utils.security

import java.nio.ByteBuffer
import java.security.{GeneralSecurityException, SecureRandom}
import java.util.Base64

import com.google.common.primitives.Bytes
import cz.kamenitxan.jakon.core.configuration.Settings
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}


object AesEncryptor {
	// TODO: longer keys
	var skeySpec: SecretKeySpec = new SecretKeySpec(Settings.getEncryptionSecret.getBytes("UTF-8"), "AES")
	val AES_KEYLENGTH = 128
	val UTF8 = "UTF-8"


	def encrypt(textToEncrypt: String): String = {
		if (textToEncrypt == null || textToEncrypt.isEmpty) throw new GeneralSecurityException("Decryption failed - invalid input")
		val iv: Array[Byte] = Array.fill[Byte](AES_KEYLENGTH / 8)(0)
		val prng = new SecureRandom()
		prng.nextBytes(iv)

		val aesCipherForEncryption: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv))
		val byteDataToEncrypt = textToEncrypt.getBytes(UTF8)
		val byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt)
		val cipherTextLength = ByteBuffer.allocate(4).putInt(byteCipherText.length).array()
		val encArr = Bytes.concat(cipherTextLength, byteCipherText, iv)
		Base64.getEncoder.encodeToString(encArr)
	}

	def decrypt(textToDecrypt: Array[Byte], iv: Array[Byte]): String = {
		val aesCipherForDecryption: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		aesCipherForDecryption.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv))
		val byteDecryptedText = aesCipherForDecryption.doFinal(textToDecrypt)
		new String(byteDecryptedText)
	}

	def decrypt(textToDecrypt: String): String = {
		if (textToDecrypt == null || textToDecrypt.isEmpty || !org.apache.commons.codec.binary.Base64.isBase64(textToDecrypt.getBytes())) {
			throw new GeneralSecurityException("Decryption failed - invalid input")
		}
		val data = Base64.getDecoder.decode(textToDecrypt)
		if (data.length < 4) {
			throw new GeneralSecurityException("Encrypted array is invalid. Length: " + data.length)
		}

		val part0 = Array.fill[Byte](4)(0)
		System.arraycopy(data, 0, part0, 0, part0.length)
		val cipherTextLength = ByteBuffer.wrap(part0).getInt()
		val part1 = Array.fill[Byte](cipherTextLength)(0)
		val part2 = Array.fill[Byte](16)(0)

		if (data.length < 4 + cipherTextLength + 16) {
			throw new GeneralSecurityException("Encrypted array is invalid. Length: " + data.length + ", Expected:" + (4 + cipherTextLength + 16))
		}
		System.arraycopy(data, part0.length, part1, 0, part1.length)
		System.arraycopy(data, part0.length + part1.length, part2, 0, part2.length)

		AesEncryptor.decrypt(part1, part2)
	}
}