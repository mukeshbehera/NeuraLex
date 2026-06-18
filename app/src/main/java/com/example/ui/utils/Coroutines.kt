package com.example.ui.utils

import androidx.compose.runtime.withFrameNanos

suspend fun awaitFrame() {
    withFrameNanos { }
}
