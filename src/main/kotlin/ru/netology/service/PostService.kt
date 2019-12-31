package ru.netology.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.netology.dto.PostRequestDto
import ru.netology.dto.PostResponseDto
import ru.netology.model.PostModel
import ru.netology.model.PostType
import ru.netology.repository.PostRepository
import ru.netology.repository.UserRepository


@KtorExperimentalAPI
class PostService(private val repo: PostRepository,
                  private val repoUser: UserRepository,
                  private val userService: UserService,
                  private val fcmService: FCMService) {
    suspend fun getAll(): List<PostResponseDto> {
        return repo.getAll().reversed().map { PostResponseDto.fromModel(it) }
    }

    suspend fun getRecentPosts(): List<PostResponseDto> {
        return repo.getRecentPosts().map { PostResponseDto.fromModel(it)}
    }

    suspend fun getPostsAfter(id: Long): List<PostResponseDto>  {
        return repo.getPostsAfter(id).map { PostResponseDto.fromModel(it) }
    }

    suspend fun getPostsBefore(id: Long): List<PostResponseDto> {
        return repo.getPostsBefore(id).map { PostResponseDto.fromModel(it) }
    }

    suspend fun getById(id: Long): PostResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun save(input: PostRequestDto, userId: Long, postId: Long): PostResponseDto {
        val model =
            if (!input.address.isNullOrEmpty() && input.coordinates != null) {
                PostModel(
                    id = postId,
                    author = userId,
                    postType = PostType.EVENT,
                    content = input.content,
                    reposts = input.reposts,
                    address = input.address,
                    coordinates = input.coordinates
                )
            } else if (!input.youtubeURL.isNullOrEmpty()) {
                PostModel(
                    id = postId,
                    author = userId,
                    postType = PostType.MEDIA,
                    content = input.content,
                    reposts = input.reposts,
                    youtubeURL = input.youtubeURL
                )
            } else {
                PostModel(
                    id = postId,
                    author = userId,
                    postType = PostType.POST,
                    content = input.content,
                    reposts = input.reposts,
                    attachment = input.attachment
                )
            }
        return PostResponseDto.fromModel(repo.save(model))
    }

    suspend fun removeById(id: Long) {
        repo.removeById(id)
    }

    suspend fun likeById(postId: Long, userId: Long): PostResponseDto {
        val model = repo.likeById(postId, userId) ?: throw NotFoundException()
        val likeText = if (model.content?.length ?: 0 > 15) model.content?.substring(0, 15) + "..." else model.content
        if (userId != model.author){
            val user = repoUser.getById(userId)
            sendSimplePush(model.author, "Your post liked", "${user?.username} лайкнул ваше сообщение '${likeText}'")
        }

        return PostResponseDto.fromModel(model)
    }

    suspend fun dislikeById(id: Long, userId: Long): PostResponseDto {
        val model = repo.dislikeById(id, userId) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun repostById(id: Long, userId: Long, input: PostRequestDto): List<PostResponseDto>? {
        val existingPost = repo.getById(id) ?: throw NotFoundException()
        return userId.let { repo.repostById(existingPost, it, input)?.map { PostResponseDto.fromModel(it) } }
    }

    suspend fun sendSimplePush(userId: Long, title: String, text: String) {
        println("send push")
        val model = userService.getModelById(userId)
        println("model: $model")
        if (model?.token != null) {
            println("token is not null")
            fcmService.send(userId, model.token.token, title, text)
        }
    }
}