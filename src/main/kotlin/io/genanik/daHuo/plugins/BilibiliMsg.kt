package io.genanik.daHuo.plugins

import com.google.gson.Gson
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.get
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadAsImage
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BilibiliMsg {

    fun trigger(abelPM: AbelPlugins, controller: GroupMessageSubscribersBuilder){
        controller.always {
            // 是否开启
            if (!abelPM.getStatus("bilibili", this.group.id)) {
                return@always
            }
            // bilibili
            val id = getAvBvFromMsg(message) ?: return@always
            reply(VideoInfo(id).beautyMsg(group))
        }
    }

    class VideoInfo(id: String) {
        private var param: String = id
        private var data: BiliData

        init {
            val requestBody = get("https://api.bilibili.com/x/web-interface/view?$param")
            data = Gson().fromJson(requestBody, BiliResponse::class.javaObjectType).data
        }

        data class BiliResponse(
            val data: BiliData
        )

        data class BiliData(
            val bvid: String,
            val aid: Int,
            val pic: String,
            val title: String,
            val owner: BilOwner,
            val stat: BiliStat
        )

        data class BilOwner(
            val name: String
        )

        data class BiliStat(
            val view: Int,      // 播放
            val danmaku: Int,   // 弹幕
            val favorite: Int,  // 收藏
            val like: Int       // 点赞
        )

        suspend fun beautyMsg(contact: Contact): MessageChain {
            val result = MessageChainBuilder()
            result.add( URL(data.pic).uploadAsImage(contact) + "\n" )
            result.add( data.title + "\n")
            result.add( "UP: ${data.owner.name}\n" )
            result.add( "${toHumanNum(data.stat.view)}播放 ${toHumanNum(data.stat.danmaku)}弹幕\n" )
            result.add( "https://www.bilibili.com/video/av${data.aid}" )
            return result.asMessageChain()
        }

        private fun toHumanNum(num: Int): String {
            return if (num >= 10000) {
                val numStr = num.toDouble()
                String.format("%.2f",(numStr/10000)) + "万"
            } else {
                num.toString()
            }
        }
    }

    class SearchVideo(keyword: String) {
        private var param: String = "keyworld=$keyword"

        data class BiliRequest(
            val data: BiliData
        )

        data class BiliData(
            val result: List<BliResult>
        )

        data class BliResult(
            val result_type: String,
            val data: List<ResultData>
        )

        data class ResultData(
            val id: Int // aid
        )

        fun getAid(): Int {
            // 返回第一个内容的aid
            val requestBody = get("https://api.bilibili.com/x/web-interface/search/all/v2?$param")
            val bean = Gson().fromJson(requestBody, BiliRequest::class.javaObjectType)
            return bean.data.result[0].data[0].id
        }

    }

    private fun getAvBvFromNormalLink(link: String): String{
        val rules = Regex("""bilibili\.com\/video\/(?:[Aa][Vv]([0-9]+)|([Bb][Vv][0-9a-zA-Z]+))""")
        val search = rules.find(link) ?: throw Exception("[Bilibili] 获取视频ID错误")
        return if (search.groupValues[1] == ""){
            "bvid=${search.groupValues[2]}"
        }else{
            "aid=${search.groupValues[1]}"
        }
    }

    private fun shortToLongLink(shortLink: String): String {
        var conn: HttpURLConnection? = null
        try {
            conn = URL(shortLink).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            e.printStackTrace()
        }
        conn!!.instanceFollowRedirects = false
        conn!!.connectTimeout = 5000
        val url: String = conn.getHeaderField("Location")
        conn.disconnect()
        return url
    }


    private fun getAvBvFromMsg(msg: MessageChain): String? {
        val plainText: PlainText = msg.firstIsInstanceOrNull() ?: return null
        var maybeLink = shortToLongLink(plainText.content)

        // normalLink
        var rule = Regex("""bilibili\.com\/video\/(?:[Aa][Vv]([0-9]+)|([Bb][Vv][0-9a-zA-Z]+))""")
        if (rule.containsMatchIn(maybeLink)){
            return getAvBvFromNormalLink(maybeLink)
        }

        // shortLink
        rule = Regex("""(b23|acg)\.tv\/[0-9a-zA-Z]+""")
        if (rule.containsMatchIn(maybeLink)){
            maybeLink = shortToLongLink(maybeLink)
            return getAvBvFromNormalLink(maybeLink)
        }

        return null
    }
/*

async function antiBiliMiniApp(context, replyFunc) {
    const msg = context.message;
    let title = null;
    if (msg.startsWith('[CQ:rich,') && msg.indexOf('QQ小程序') !== -1 && msg.indexOf('哔哩哔哩') !== -1) {
        if (setting.despise) {
            replyFunc(context, CQ.img('https://i.loli.net/2020/04/27/HegAkGhcr6lbPXv.png'));
        }
        const search = /"desc":"(.+?)"(?:,|})/.exec(CQ.unescape(msg));
    if (search) title = search[1].replace(/\\"/g, '"');
}
if (setting.getVideoInfo) {
    const param = await getAvBvFromMsg(msg);
    if (param) {
        const { aid, bvid } = param;
        if (cache.has(aid) || cache.has(bvid)) return;
        if (aid) cache.set(aid, true);
        if (bvid) cache.set(bvid, true);
        const reply = await getVideoInfo(param);
        if (reply) {
            replyFunc(context, reply);
            return;
        }
    }
    const isBangumi = /bilibili\.com\/bangumi|(b23|acg)\.tv\/(ep|ss)/.test(msg);
    if (title && !isBangumi) {
        const reply = await getSearchVideoInfo(title);
        if (reply) {
            replyFunc(context, reply);
            return;
        }
    }
}
}

export default antiBiliMiniApp;
}
     */
}