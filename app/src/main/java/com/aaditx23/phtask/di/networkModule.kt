package com.aaditx23.phtask.di

import com.aaditx23.phtask.BuildConfig
import com.aaditx23.phtask.data.repository.CourseRepositoryImpl
import com.aaditx23.phtask.data.remote.service.CourseApiService
import com.aaditx23.phtask.domain.repository.ICourseRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single(named("baseUrl")) {
        BuildConfig.BASE_URL
    }

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
        }
    }

    single {
        CourseApiService(
            client = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    single<ICourseRepository> {
        CourseRepositoryImpl(
            apiService = get(),
            courseDao = get(),
            ioDispatcher = Dispatchers.IO
        )
    }

}