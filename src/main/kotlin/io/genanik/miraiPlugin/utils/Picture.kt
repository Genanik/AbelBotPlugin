package io.genanik.miraiPlugin.utils

import com.sun.jna.Library
import com.sun.jna.Native
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.toExternalImage
import java.io.*
import java.net.URL

fun createAbelPicFolder(){
    val folder = File("AbelPic/")
    if (!folder.exists()){
        folder.mkdir()
    }
}

suspend fun mirrorImage(url: String, contact: Contact): Image {
    val raw = File("AbelPic/mirrorImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.HorizontalFilpPic() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("AbelPic/mirrorImgDst").toExternalImage())
}

suspend fun reverseImage(url: String, contact: Contact): Image {
    val raw = File("AbelPic/reverseImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.ReverseGif() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("AbelPic/reverseImgDst").toExternalImage())
}

class ResizePic(url: String) {
    private val raw = File("AbelPic/resizeImg")
    init {
        raw.writeBytes(URL(url).readBytes())
    }

    suspend fun ToBigger(contact: Contact): Image {
        if (libImage.INSTANCE.ResizeImgToBig() != 0){
            throw Exception("在放大图片时出现了未知的错误")
        }
        return uploadResizeImg(contact)
    }

    suspend fun ToSmaller(contact: Contact): Image {
        if (libImage.INSTANCE.ResizeImgToSmall() != 0){
            throw Exception("在缩小图片时出现了未知的错误")
        }
        return uploadResizeImg(contact)
    }

    private suspend fun uploadResizeImg(contact: Contact): Image {
        return contact.uploadImage(File("resizeImgDst").toExternalImage())
    }
}

fun isGIF(url: String): Boolean {
    val raw = File("AbelPic/isGIF")
    var gif = false
    raw.writeBytes(URL(url).readBytes())
    gif = when (libImage.INSTANCE.ResizeImgToBig()){
        0 -> true
        1 -> false
        else -> throw Exception("在判断是否为GIF时出现了未知的错误")
    }
    return gif
}

// 引用外部动态库 https://github.com/Genanik/Vertical-flip-of-Repeat-picture
interface libImage : Library {

    fun IsGIF(): Int

    fun ResizeImgToBig(): Int

    fun ResizeImgToSmall(): Int

    fun ReverseGif(): Int

    fun HorizontalFilpPic(): Int

    companion object {
        //懒加载的方式
        val INSTANCE by lazy { Native.load("libImage.so", libImage::class.java)  as libImage}
    }
}