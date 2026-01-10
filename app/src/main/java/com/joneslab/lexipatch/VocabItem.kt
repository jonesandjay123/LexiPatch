package com.joneslab.lexipatch

data class VocabItem(
    val english: String,
    val chinese: String,
    val abbr: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "active"
)
