package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.token
import io.genanik.daHuo.utils.get

val bilibiliAPI = "https://api.bilibili.com/x/player/playurl?"
val bilibiliBangumiAPI = "https://api.bilibili.com/pgc/player/web/playurl?"
val bilibiliTokenAPI = "https://api.bilibili.com/x/player/playurl/token?"
val SearchAPI = "https://api.bilibili.com/x/web-interface/search/all/v2?"
val ViewAPI = "https://api.bilibili.com/x/web-interface/view?"

class Downloads(token: String){
    var utoken: String = token

    fun genAPI(aid: Int, cid: Int, quality: Int, bangumi: Boolean, cookie: String): String? {
        var baseAPIURL: String
        var params: String

        if (cookie != "" && utoken == "") {
            utoken = get("${bilibiliTokenAPI}aid=$aid&cid=$cid")
            var t = Gson().fromJson(utoken, token::class.javaObjectType)

            if (t.code != 0) {
                return null
            }
            utoken = t.data.token
        }

        var api: String
        if (bangumi) {
            // The parameters need to be sorted by name
            // qn=0 flag makes the CDN address different every time
            // quality=120(4k) is the highest quality so far
            params = "cid=$cid&bvid=&qn=$quality&type=&otype=json&fourk=1&fnver=0&fnval=16"
            baseAPIURL = bilibiliBangumiAPI
        } else {
            params = "avid=$aid&cid=$cid&bvid=&qn=$quality&type=&otype=json&fourk=1&fnver=0&fnval=16"
            baseAPIURL = bilibiliAPI
        }
        api = baseAPIURL + params
        // bangumi utoken also need to put in params to sign, but the ordinary video doesn't need
        if (!bangumi && utoken != "") {
            api = "$api&this=$utoken"
        }
        return api
    }
}