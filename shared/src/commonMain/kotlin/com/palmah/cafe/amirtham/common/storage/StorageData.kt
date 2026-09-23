package com.palmah.cafe.amirtham.common.storage

import dev.gitlive.firebase.storage.Data

/**
 * Wraps [ByteArray] into gitlive Firebase Storage's [Data] carrier, which has a different
 * constructor per platform (ByteArray on Android/JVM, Uint8Array on JS, NSData on iOS).
 */
expect fun ByteArray.toStorageData(): Data
