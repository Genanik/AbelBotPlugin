package io.genanik.plugin.Util.translate.Web

import java.security.MessageDigest

/**
 * MD5编码相关的类
 *
 * @author Genanik
 */
object MessageDigestUtils {
    /**
     * md5加密字符串
     * md5使用后转成16进制变成32个字节
     */
    fun md5(str: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(str.toByteArray())
        //转成16进制后是32字节
        return toHex(result)
    }

    private fun toHex(byteArray: ByteArray): String {
        //转成16进制后是32字节
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }

}