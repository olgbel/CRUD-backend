package ru.netology.dto

import ru.netology.model.PostType

data class PostRequestDto(
    val author: Long,
    val postType: PostType = PostType.POST,
    val content: String? = null,
    val address: String? = null,
    val coordinates: Pair<Double, Double>? = null,
    val youtubeURL: String? = null,
    val reposts: Int = 0
)