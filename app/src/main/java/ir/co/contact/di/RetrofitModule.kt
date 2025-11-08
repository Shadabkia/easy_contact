package ir.co.contact.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.co.contact.data.source.remote.network.HeaderInterceptor
import ir.co.contact.data.source.local.DataStoreManager
import ir.co.contact.utils.BASE_URL
import ir.co.contact.utils.WITHOUT_TOKEN_ANNOTATION
import ir.co.contact.utils.WITH_TOKEN_ANNOTATION
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }


    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor =
        ChuckerInterceptor.Builder(context).build()

    @Singleton
    @Provides
    fun provideHeaderInterceptor(dataStoreManager: DataStoreManager) =
        HeaderInterceptor(dataStoreManager)

    @Named(WITH_TOKEN_ANNOTATION)
    @Singleton
    @Provides
    fun provideOkHttpClientWithToken(
        chuckerInterceptor: ChuckerInterceptor, headerInterceptor: HeaderInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .addInterceptor(headerInterceptor)
            .addInterceptor(chuckerInterceptor)
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Named(WITHOUT_TOKEN_ANNOTATION)
    @Singleton
    @Provides
    fun provideOkHttpClientWithoutToken(
        chuckerInterceptor: ChuckerInterceptor, headerInterceptor: HeaderInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .addInterceptor(chuckerInterceptor)
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory {
        val json = Json { ignoreUnknownKeys = true }
        return json.asConverterFactory("application/json".toMediaType())
    }

    @Named(WITH_TOKEN_ANNOTATION)
    @Singleton
    @Provides
    fun provideRetrofitWithToken(
        @Named(WITH_TOKEN_ANNOTATION) okHttpClient: OkHttpClient,
        converter: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addConverterFactory(converter)
            .client(okHttpClient)
            .build()

    @Named(WITHOUT_TOKEN_ANNOTATION)
    @Singleton
    @Provides
    fun provideRetrofitWithoutToken(
        @Named(WITHOUT_TOKEN_ANNOTATION) okHttpClient: OkHttpClient,
        converter: Converter.Factory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addConverterFactory(converter)
            .client(okHttpClient)
            .build()

}