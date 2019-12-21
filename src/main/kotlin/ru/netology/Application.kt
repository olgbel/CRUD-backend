package ru.netology

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.cio.EngineMain
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.kodein.di.generic.*
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import ru.netology.dto.ErrorResponseDto
import ru.netology.repository.PostRepository
import ru.netology.repository.PostRepositoryInMemoryWithMutexImpl
import ru.netology.repository.UserRepository
import ru.netology.repository.UserRepositoryInMemoryWithMutexImpl
import ru.netology.route.RoutingV1
import ru.netology.service.FileService
import ru.netology.service.JWTTokenService
import ru.netology.service.PostService
import ru.netology.service.UserService
import javax.naming.ConfigurationException

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@UseExperimental(KtorExperimentalAPI::class)
fun Application.module() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    install(StatusPages) {
        exception<NotImplementedError> {
            call.respond(HttpStatusCode.NotImplemented)
        }
        exception<ParameterConversionException> {
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> {
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(it.message.toString()))
        }
        exception<ru.netology.exception.AccessDeniedException>{
            call.respond(HttpStatusCode.Forbidden, ErrorResponseDto(it.message.toString()))
        }
        exception<NotFoundException>{
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(it.message.toString()))
        }
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }

    }

    install(KodeinFeature) {
        constant(tag ="upload-dir") with "./uploads"//(environment.config.propertyOrNull("ncraft.upload.dir")?.getString() ?: throw ConfigurationException("Upload dir is not specified"))
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService() }
        bind<PostRepository>() with eagerSingleton { PostRepositoryInMemoryWithMutexImpl() }
        bind<PostService>() with eagerSingleton { PostService(instance()) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithMutexImpl() }
        bind<UserService>() with eagerSingleton { UserService(instance(), instance(), instance()).apply {
            //            runBlocking {
//                this@apply.save("vasya", "password")
//                this@apply.save("login", "password")
//            }
        } }
        bind<RoutingV1>() with eagerSingleton { RoutingV1(instance(tag = "upload-dir"), instance(), instance(), instance()) }
    }

    install(Authentication) {
        jwt {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()

            validate {
                val id = it.payload.getClaim("id").asLong()
                userService.getModelById(id)
            }
        }
    }

    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}