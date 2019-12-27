package ru.netology.model

data class PostModel(
    val id: Long = -1,
    val author: Long,
    val content: String? = null,
    val created: Int = (System.currentTimeMillis() / 1000).toInt(),
    val likes: Set<Long> = setOf(),
    val reposts: Int = 0,
    val postType: PostType = PostType.POST,
    val source: PostModel? = null, // for reposts
    val youtubeURL: String? = null,
    val address: String? = null,
    val coordinates: Pair<Double, Double>? = null,
    val attachment: AttachmentModel? = null
)
enum class PostType {
    POST, REPOST, EVENT, MEDIA
}