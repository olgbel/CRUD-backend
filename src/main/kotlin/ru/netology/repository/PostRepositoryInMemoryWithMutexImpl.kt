package ru.netology.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.netology.dto.PostRequestDto
import ru.netology.model.PostModel
import ru.netology.model.PostType
import ru.netology.model.UserModel
import java.lang.Integer.max

class PostRepositoryInMemoryWithMutexImpl : PostRepository {

    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> {
        mutex.withLock {
            return items.reversed()
        }
    }

    override suspend fun getRecentPosts(): List<PostModel> {
        mutex.withLock {
            return items.subList(max(items.lastIndex - 2, 0), items.lastIndex + 1)
        }
    }

    override suspend fun getPostsAfter(id: Long): List<PostModel>{
        mutex.withLock {
            val filteredList = items.filter { it.id > id } as MutableList<PostModel>
            return filteredList.reversed()
        }
    }

    override suspend fun getPostsBefore(id: Long): List<PostModel>{
        mutex.withLock {
            val filteredList = items.filter { it.id < id } as MutableList<PostModel>
            return filteredList.reversed()
        }
    }

    override suspend fun getById(id: Long): PostModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }
    private val log: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)

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

    override suspend fun repostById(item: PostModel, userId: Long, input: PostRequestDto): List<PostModel> {
        mutex.withLock {
            val index = items.indexOfFirst { it.id == item.id }

            val oldItem = items[index].copy(reposts = item.reposts + 1)
            items[index] = oldItem

            val newItem = item.copy(
                id = nextId++,
                content = input.content,
                postType = PostType.REPOST,
                author = userId,
                source = item,
                likes = setOf())

            items.add(newItem)
            return listOf(oldItem, newItem)
        }

    }
}