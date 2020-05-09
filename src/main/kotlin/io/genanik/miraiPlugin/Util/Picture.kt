package io.genanik.miraiPlugin.Util

import com.sun.jna.Library
import com.sun.jna.Native
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.toExternalImage
import java.io.*
import java.net.URL

suspend fun mirrorImage(url: String, contact: Contact): net.mamoe.mirai.message.data.Image {
    val raw = File("srcImg")
    raw.writeBytes(URL(url).readBytes())
    if (libImage.INSTANCE.ConvertPic() != 0){
        throw Exception("图片错了。。")
    }
    return contact.uploadImage(File("dstImg").toExternalImage())
}

// 引用外部动态库 https://github.com/Genanik/Vertical-flip-of-Repeat-picture
interface libImage : Library {

    fun ConvertPic(): Int

    companion object {
        //懒加载的方式
        val INSTANCE by lazy { Native.load("libImage.so", libImage::class.java)  as libImage}
    }
}