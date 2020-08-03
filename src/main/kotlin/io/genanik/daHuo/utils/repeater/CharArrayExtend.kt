package io.genanik.daHuo.utils.repeater

fun <Char> Array<Char>.findOrNull(targetChar: Char): Int? {
    for ((index, i) in this.withIndex()){
        if (i == targetChar){
            return index
        }
    }
    return null
}