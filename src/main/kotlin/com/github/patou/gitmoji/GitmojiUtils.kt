package com.github.patou.gitmoji

import com.intellij.openapi.util.SystemInfo

fun defaultDisplayType(): String {
    return if (SystemInfo.isWindows) "icon" else "emoji"
}

fun insertAt(target: String, position: Int, insert: String): String {
    val targetLen = target.length
    require(!(position < 0 || position > targetLen)) { "position=$position" }
    if (insert.isEmpty()) {
        return target
    }
    if (position == 0) {
        return insert + target
    } else if (position == targetLen) {
        return target + insert
    }
    val insertLen = insert.length
    val buffer = CharArray(targetLen + insertLen)
    target.toCharArray(buffer, 0, 0, position)
    insert.toCharArray(buffer, position, 0, insertLen)
    target.toCharArray(buffer, position + insertLen, position, targetLen)
    return String(buffer)
}