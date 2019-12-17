package ru.netology.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import ru.netology.dto.PostRequestDto
import ru.netology.dto.PostResponseDto
import ru.netology.model.PostModel
import ru.netology.model.PostType
import ru.netology.repository.PostRepository


@KtorExperimentalAPI
class PostService(private val repo: PostRepository) {
    suspend fun getAll(): List<PostResponseDto> {
        return repo.getAll().reversed().map { PostResponseDto.fromModel(it) }
    }

    suspend fun getById(id: Long): PostResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun save(input: PostRequestDto, userId: Long): PostResponseDto {
        val model =
            if (!input.address.isNullOrEmpty() && input.coordinates != null) {
                PostModel(
                    id = input.id,
                    author = userId,
                    postType = PostType.EVENT,
                    content = input.content,
                    reposts = input.reposts,
                    address = input.address,
                    coordinates = input.coordinates
                )
            } else if (!input.youtubeURL.isNullOrEmpty()) {
                PostModel(
                    id = input.id,
                    author = userId,
                    postType = PostType.MEDIA,
                    content = input.content,
                    reposts = input.reposts,
                    youtubeURL = input.youtubeURL
                )
            } else {
                PostModel(
                    id = input.id,
                    author = userId,
                    postType = PostType.POST,
                    content = input.content,
                    reposts = input.reposts
                )
            }
        return PostResponseDto.fromModel(repo.save(model))
    }

    suspend fun removeById(id: Long) {
        repo.removeById(id)
    }

    suspend fun likeById(id: Long, userId: Long): PostResponseDto {
        val model = repo.likeById(id, userId) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun dislikeById(id: Long, userId: Long): PostResponseDto {
        val model = repo.dislikeById(id, userId) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun repostById(id: Long, userId: Long): List<PostResponseDto>? {
        val existingPost = repo.getById(id) ?: throw NullPointerException()
        return userId.let { repo.repostById(existingPost, it)?.map { PostResponseDto.fromModel(it) } }
    }
}