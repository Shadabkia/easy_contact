package ir.co.contact.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.co.contact.data.source.remote.MainServices
import ir.co.contact.utils.WITHOUT_TOKEN_ANNOTATION
import ir.co.contact.utils.WITH_TOKEN_ANNOTATION
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideServiceWithToken(@Named(WITH_TOKEN_ANNOTATION) retrofit: Retrofit): MainServices =
        retrofit.create(MainServices::class.java)

}