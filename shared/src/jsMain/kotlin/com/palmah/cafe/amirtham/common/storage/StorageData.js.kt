package com.palmah.cafe.amirtham.common.storage

import dev.gitlive.firebase.storage.Data
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set

actual fun ByteArray.toStorageData(): Data {
    val uint8Array = Uint8Array(size)
    forEachIndexed { index, byte -> uint8Array[index] = byte }
    return Data(uint8Array)
}
