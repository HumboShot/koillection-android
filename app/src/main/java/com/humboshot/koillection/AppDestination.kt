package com.humboshot.koillection

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object MainColletion
}