package io.genanik.plugin.Util

import com.madgag.gif.fmsware.AnimatedGifEncoder
import com.madgag.gif.fmsware.GifDecoder
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import javax.imageio.ImageIO


fun isGif(url: String):Boolean{
//     设置新的pic
//    var gd = GifDecoder()
//    要处理的图片
//    var status = gd.read(url)
//    if (status != GifDecoder.STATUS_OK) {
//        return false
//    }
//    return true
    return false
}

// 镜像BufferedImage的图
fun mirror(pic: BufferedImage): BufferedImage{
    val width: Int = pic.width
    val height: Int = pic.height

    // 读取出图片的所有像素
    val rgbs: IntArray = pic.getRGB(0, 0, width, height, null, 0, width)

    // 对图片的像素矩阵进行水平镜像
    for (row in 0 until height) {
        for (col in 0 until width / 2) {
            val temp = rgbs[row * width + col]
            rgbs[row * width + col] = rgbs[row * width + (width - 1 - col)]
            rgbs[row * width + (width - 1 - col)] = temp
        }
    }

    // 把水平镜像后的像素矩阵设置回 pic
    pic.setRGB(0, 0, width, height, rgbs, 0, width)
    return pic
}

// 镜像gif
fun mirrorGif(inputURL: String, outputFileName: String): File {
    // 设置新的pic
    val gd = GifDecoder()
    //要处理的图片
    val status = gd.read(inputURL)
    if (status != GifDecoder.STATUS_OK) {
        throw IOException("read image first.gif error!")
    }

    val ge =  AnimatedGifEncoder()

    ge.start(outputFileName)
    ge.setRepeat(gd.loopCount)

    for(i in (0 until gd.frameCount)){
        var frame = gd.getFrame(i)
        // mirror
        frame = mirror(frame)

        ge.setDelay(gd.getDelay(i))
        ge.addFrame(frame)
    }

    //输出图片
    ge.finish()
    return File(outputFileName)
}

// 倒放gif
fun reverseGif(outputFileName: String): File {

    val decoder = GifDecoder()
    val status = decoder.read("first.gif")
    if (status != GifDecoder.STATUS_OK) {
        throw IOException("read image first.gif error!")
    }
    // 拆分一帧一帧的压缩之后合成
    val encoder = AnimatedGifEncoder()
    encoder.start(outputFileName)
    encoder.setRepeat(decoder.loopCount)
    for (i in decoder.frameCount - 1 downTo 0) {
        encoder.setDelay(decoder.getDelay(i)) // 设置播放延迟时间
        val bufferedImage = decoder.getFrame(i) // 获取每帧BufferedImage流
        val height = bufferedImage.height
        val width = bufferedImage.width
        val zoomImage = BufferedImage(width, height, bufferedImage.type)
        val image: Image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val gc: Graphics = zoomImage.graphics
        gc.setColor(Color.WHITE)
        gc.drawImage(image, 0, 0, null)
        encoder.addFrame(zoomImage)
    }
    encoder.finish()
    return File("tmp.gif")
}

fun mirrorImage(url: String): File{
    return if (isGif(url)){
        mirrorGif(url, "optGif")
        File("optGif")
    }else{
        ImageIO.write(mirror(ImageIO.read(URL(url))), "png", File("optImg"))
        File("optImg")
    }
}