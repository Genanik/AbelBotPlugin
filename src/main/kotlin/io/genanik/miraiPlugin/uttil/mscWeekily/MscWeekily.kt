package io.genanik.miraiPlugin.uttil.mscWeekily

import java.net.URL

class MscWeekily {
    private var Raw = URL("http://114.67.100.226:45777/?key=ForMscWeekily").readText().split("\n")
    private var mscChats = ArrayList<MscChat>()

    init {
        Raw.forEach { line ->
            mscChats.add(MscChat(line))
        }
    }

    // 根据日期获取符合的MscChat  TODO: days参数
    fun getWithCalendar(msgCalendar: MsgCalendar, days: Int = 1): ArrayList<MscChat>{
        val needYear = msgCalendar.year
        val needMonth = msgCalendar.month
        val needDay = msgCalendar.day
        val result = ArrayList<MscChat>()
        mscChats.forEach { chatMsg ->
            if ( chatMsg.msgDate.msgCalendar.year == needYear &&
                 chatMsg.msgDate.msgCalendar.month == needMonth &&
                 chatMsg.msgDate.msgCalendar.day == needDay
            ){
                result.add(chatMsg)
            }
        }
        return result
    }
}