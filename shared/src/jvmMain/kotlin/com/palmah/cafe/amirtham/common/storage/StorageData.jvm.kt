package com.palmah.cafe.amirtham.common.storage

import dev.gitlive.firebase.storage.Data

actual fun ByteArray.toStorageData(): Data = Data(this)
