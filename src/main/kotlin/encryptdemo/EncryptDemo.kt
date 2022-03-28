package encryptdemo

import java.security.Key
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*


/**
 * 加解密类demo
 */
class EncryptDemo {

    /**
     * 生成RSA密钥对
     */
    fun rsaKeyProduct() {
        val pair: Pair<String, String> = initRsaKey()
        println("rsa public key:\n${pair.first}")
        println("rsa private key:\n${pair.second}")
    }

    /**
     * 初始化RSA公私密钥对
     */
    private fun initRsaKey(): Pair<String, String> {
        //获得对象 KeyPairGenerator 参数 RSA 1024个字节
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(1024)
        //通过对象 KeyPairGenerator 获取对象KeyPair
        val keyPair = keyPairGen.generateKeyPair()

        //通过对象 KeyPair 获取RSA公私钥对象RSAPublicKey RSAPrivateKey
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        return Pair(getRsaPublicKeyString(publicKey), getRsaPrivateKeyString(privateKey))
    }

    /**
     * 获取密钥字符串
     */
    private fun getRsaPublicKeyString(key: Key): String {
        return "-----BEGIN PUBLIC KEY-----\n" + encryptBASE64(key.encoded) + "\n-----END PUBLIC KEY-----"
    }


    /**
     * 获取密钥字符串
     */
    private fun getRsaPrivateKeyString(key: Key): String {
        return "-----BEGIN PRIVATE KEY-----\n" + encryptBASE64(key.encoded) + "\n-----END PRIVATE KEY-----"
    }

    //解码返回byte
    private fun decryptBASE64(key: String): ByteArray {
        return Base64.getDecoder().decode(key)
    }

    //编码返回字符串
    private fun encryptBASE64(key: ByteArray): String {
        val temp = Base64.getEncoder().encodeToString(key)
        val stringBuilder = StringBuilder()
        val quotient = temp.length / 64
        val remainder = temp.length % 64
        for (i in 0 until quotient) {
            stringBuilder.apply {
                append(temp.substring(i * 64, (i + 1) * 64))
                append("\n")
            }
        }
        if (remainder != 0) {
            stringBuilder.append(temp.substring(quotient * 64))
        }
        return stringBuilder.toString()
    }


}