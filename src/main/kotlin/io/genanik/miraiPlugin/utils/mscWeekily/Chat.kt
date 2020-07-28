package io.genanik.miraiPlugin.utils.mscWeekily

// line: [2020-07-23 21:15:45]ZaneLiao:ooooooooooooooooooooooooooooh

class MscChat(line: String) {
    // format date
    val msgDate = MsgDate(line)
    var msgUser: String
    var content: String

    init {
        val tmp = line.split("]")[1].split(":")
        msgUser = tmp[0]
        content = tmp[1]
    }
}

class MsgDate(line: String) {
    val msgCalendar = MsgCalendar(line)
    val msgTime = MsgTime(line)

//    val year = msgCalendar.year
//    val month = msgCalendar.month
//    val day = msgCalendar.day
//    val hour = msgTime.hour
//    val second = msgTime.second
//    val millisecond = msgTime.millisecond
}

class MsgCalendar(
    line: String = "",
    msgYear: Int = 1970,
    msgMonth: Int = 10,
    msgDay: Int = 10,
) {
    var year = msgYear
    var month = msgMonth
    var day = msgDay

    init{
        if (line != ""){
            val tmp = line.split("]")[0].replace("[", "").split(" ")
            year    =     Integer.getInteger(tmp[0].split("-")[0])
            month   =     Integer.getInteger(tmp[0].split("-")[1])
            day     =     Integer.getInteger(tmp[0].split("-")[2])
        }
    }
}

class MsgTime(line: String) {
    var hour = 10
    var second = 10
    var millisecond = 10

    init{
        val tmp = line.split("]")[0].replace("[", "").split(" ")
        hour    =     Integer.getInteger(tmp[1].split(":")[0])
        second  =     Integer.getInteger(tmp[1].split(":")[1])
        millisecond = Integer.getInteger(tmp[1].split(":")[2])
    }
}