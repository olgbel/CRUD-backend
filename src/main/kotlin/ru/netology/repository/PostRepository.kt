package ru.netology.repository

import ru.netology.dto.PostRequestDto
import ru.netology.model.PostModel

interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getRecentPosts(): List<PostModel>
    suspend fun getPostsAfter(id: Long): List<PostModel>
    suspend fun getPostsBefore(id: Long): List<PostModel>
    suspend fun getById(id: Long): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, userId: Long): PostModel?
    suspend fun dislikeById(id: Long, userId: Long): PostModel?
    suspend fun repostById(item: PostModel, userId: Long, input: PostRequestDto): List<PostModel>?
}
