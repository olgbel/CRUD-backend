package ru.netology.dto

import ru.netology.model.AttachmentModel
import ru.netology.model.MediaModel
import ru.netology.model.PostModel
import ru.netology.model.PostType

data class PostResponseDto(val id: Long,
                           val author: Long,
                           val content: String? = null,
                           val created: Int,
                           val likes: Set<Long> = setOf(),
                           val reposts: Int = 0,
                           val source: PostModel?,
                           val postType: PostType = PostType.POST,
                           val youtubeURL: String? = null,
                           val address: String? = null,
                           val coordinates: Pair<Double, Double>? = null,
                           val attachment: AttachmentModel? = null) {
    companion object {
        fun fromModel(model: PostModel) = PostResponseDto(
            id = model.id,
            author = model.author,
            content = model.content,
            created = model.created,
            likes = model.likes,
            reposts = model.reposts,
            source = model.source,
            postType = model.postType,
            youtubeURL = model.youtubeURL,
            address = model.address,
            coordinates = model.coordinates,
            attachment = model.attachment
        )
    }
}