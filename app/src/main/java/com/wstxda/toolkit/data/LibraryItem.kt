package com.wstxda.toolkit.data

data class LibraryItem(
    val name: String,
    val description: String? = null,
    val version: String? = null,
    val website: String? = null,
    val license: String? = null
)