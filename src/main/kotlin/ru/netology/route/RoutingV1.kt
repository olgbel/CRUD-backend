package ru.netology.route

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.features.ParameterConversionException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import ru.netology.dto.AuthenticationRequestDto
import ru.netology.dto.PostRequestDto
import ru.netology.dto.RegistrationRequestDto
import ru.netology.dto.UserResponseDto
import ru.netology.model.UserModel
import ru.netology.service.FileService
import ru.netology.service.PostService
import ru.netology.service.UserService


@KtorExperimentalAPI
class RoutingV1(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService
) {
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }

                route("") {
                    post("/registration") {
                        val input = call.receive<RegistrationRequestDto>()
                        val response = userService.registration(input)
                        call.respond(response)
                    }

                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                }

                authenticate {
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            call.respond(UserResponseDto.fromModel(me!!))
                        }
                    }

                    route("/posts") {
                        get {
                            //                            val me = call.authentication.principal<UserModel>()
                            val response = postService.getAll()
                            call.respond(response)
                        }

                        get("/{id}") {
                            //                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.getById(id)
                            call.respond(response)
                        }

                        post {
                            val input = call.receive<PostRequestDto>()
                            val me = call.authentication.principal<UserModel>()
                            val response = me?.id?.let { it1 -> postService.save(input, it1, -1) }
                            call.respond(response!!)
                        }

                        delete("/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val postId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val post = postService.getById(postId)

                            if (post.author != me.id)
                                throw ru.netology.exception.AccessDeniedException("You cannot delete another's post!")

                            val response = postService.removeById(postId)
                            call.respond(response)
                        }

                        put("/{id}"){
                            val me = call.authentication.principal<UserModel>()!!
                            val postId = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val post = postService.getById(postId)

                            if (post.author != me.id) {
                                throw ru.netology.exception.AccessDeniedException("You cannot update another's post")
                            }
                            val input = call.receive<PostRequestDto>()
                            val response = postService.save(input, me.id, postId)
                            call.respond(response)
                        }

                        post("/like/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<UserModel>()
                            val response = me?.id?.let { it1 -> postService.likeById(id, it1) }
                            call.respond(response!!)
                        }
                        post("/dislike/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<UserModel>()
                            val response = me?.id?.let { it1 -> postService.dislikeById(id, it1) }
                            call.respond(response!!)
                        }

                        post("/repost/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                            val me = call.authentication.principal<UserModel>()

                            val input = call.receive<PostRequestDto>()
                            val response = me?.id?.let { it1 -> postService.repostById(id, it1, input) }
                            call.respond(response!!)
                        }
                    }

                    route("/media") {
                        post {
                            val multipart = call.receiveMultipart()
                            val response = fileService.save(multipart)
                            call.respond(response)
                        }
                    }
                }
            }
        }
    }
}