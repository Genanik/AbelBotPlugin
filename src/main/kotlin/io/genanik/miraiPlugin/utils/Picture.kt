package io.genanik.miraiPlugin.utils

import com.sun.jna.Library
import com.sun.jna.Native
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.toExternalImage
import java.io.*
import java.net.URL

suspend fun mirrorImage(url: String, contact: Contact): net.mamoe.mirai.message.data.Image {
    val raw = File("srcImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.HorizontalFilpPic() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("dstImg").toExternalImage())
}

suspend fun reverseImage(url: String, contact: Contact): net.mamoe.mirai.message.data.Image {
    val raw = File("srcImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.ReverseGif() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("dstImg").toExternalImage())
}

suspend fun resizeImgToBig(url: String, contact: Contact): net.mamoe.mirai.message.data.Image {
    val raw = File("srcImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.ResizeImgToBig() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("dstImg").toExternalImage())
}

//suspend fun resizeImgToSmall(url: String, contact: Contact): net.mamoe.mirai.message.data.Image {
//    val raw = File("srcImg")
//    raw.writeBytes(URL(url).readBytes())
//    if (libImage.INSTANCE.ResizeImgToSmall() != 0){
//        throw Exception("图片错了。。")
//    }
//    return contact.uploadImage(File("dstImg").toExternalImage())
//}

// 引用外部动态库 https://github.com/Genanik/Vertical-flip-of-Repeat-picture
interface libImage : Library {

    fun ResizeImgToBig(): Int

//    fun ResizeImgToSmall(): Int

    fun ReverseGif(): Int

    fun HorizontalFilpPic(): Int

    companion object {
        //懒加载的方式
        val INSTANCE by lazy { Native.load("libImage.so", libImage::class.java)  as libImage}
    }
}