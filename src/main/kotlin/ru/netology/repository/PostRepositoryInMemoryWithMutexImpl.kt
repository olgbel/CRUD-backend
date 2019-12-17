package ru.netology.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.netology.model.PostModel
import ru.netology.model.PostType

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> {
        mutex.withLock {
            return items.reversed()
        }
    }

    override suspend fun getById(id: Long): PostModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++, postType = item.postType)
                    items.add(copy)
                    copy
                }
                else -> {
                    val oldItem = items[index].copy(content = item.content)
                    items[index] = oldItem
                    oldItem
                }
            }
        }
    }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(id: Long, userId: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(likes = item.likes.plus(userId))
                    try {
                        items[index] = copy
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        println("size: ${items.size}")
                        println(index)
                    }
                    copy
                }
            }
        }
    }

    override suspend fun dislikeById(id: Long, userId: Long): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    val copy = item.copy(likes = item.likes.minus(userId))
                    try {
                        items[index] = copy
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        println("size: ${items.size}")
                        println(index)
                    }
                    copy
                }
            }
        }
    }

    override suspend fun repostById(item: PostModel, userId: Long): List<PostModel> {
        mutex.withLock {
            val index = items.indexOfFirst { it.id == item.id }

            val oldItem = items[index].copy(reposts = item.reposts + 1)
            items[index] = oldItem

            val newItem = item.copy(
                id = nextId++,
                content = item.content,
                postType = PostType.REPOST,
                author = userId,
                sourceId = item.id,
                likes = setOf())

            items.add(newItem)
            return listOf(oldItem, newItem)
        }

    }
}