package io.genanik.miraiPlugin.utils.mscWeekily

import java.net.URL

class MscWeekily {
    private var Raw = URL("http://114.67.100.226:45777/?key=ForMscWeekily").readText().split("\n")
    private var mscChats = ArrayList<MscChat>()

    init {
        Raw.forEach { line ->
            mscChats.add(MscChat(line))
        }
    }

    // 根据日期获取符合的MscChat
    fun getWithCalendar(msgCalendar: MsgCalendar, days: Int = 1): ArrayList<MscChat>{
        val needYear = msgCalendar.year
        val needMonth = msgCalendar.month
        var needDay = msgCalendar.day
        var needDays = days
        val result = ArrayList<MscChat>()
        if (days > 7){
            throw Exception("days不能大于7")
        }

        while (needDays != 0){
            mscChats.forEach { chatMsg ->
                // 添加一天
                if ( chatMsg.msgDate.msgCalendar.year == needYear &&
                    chatMsg.msgDate.msgCalendar.month == needMonth &&
                    chatMsg.msgDate.msgCalendar.day == needDay
                ){
                    result.add(chatMsg)
                }
            }
            // 改变needDay和needDays
            needDay++
            needDays--
        }
        return result
    }
}

fun main(){
    MscWeekily().getWithCalendar(MsgCalendar(msgYear=2020, msgMonth=7, msgDay=24))
}