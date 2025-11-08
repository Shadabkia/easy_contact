package ir.co.contact.data.source.remote.network

import ir.co.contact.data.source.local.DataStoreConstants.ACCESS_TOKEN
import ir.co.contact.data.source.local.DataStoreManager
import ir.co.contact.utils.APPLICATION_JSON_HEADER_KEY
import ir.co.contact.utils.AUTHORIZATION_HEADER_KEY
import ir.co.contact.utils.CONTENT_TYPE_HEADER_KEY
import ir.co.contact.utils.TOKEN_PREFIX
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()


        requestBuilder.addHeader(
            CONTENT_TYPE_HEADER_KEY, APPLICATION_JSON_HEADER_KEY
        )

        val token = runBlocking {
            dataStoreManager.getData(ACCESS_TOKEN).firstOrNull()
        }

        token?.let {
            requestBuilder.addHeader(AUTHORIZATION_HEADER_KEY, "$TOKEN_PREFIX $it")
            Timber.tag("okhttpp").d("Token: Bearer $it")
        } ?: Timber.tag("okhttpp").d("Token not found")

//        requestBuilder.addHeader(
//            AUTHORIZATION_HEADER_KEY, "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5ZWQ2ZGZkNy02NWRkLTRkY2MtYTc0My02MjMwOTU1ZDBiODYiLCJqdGkiOiJkZmNmYTJiMGZlYzk3MDQ4MGNiYzQ3ZTI5MTZiMzQ3ZjNmMTI4ZmIwMjFlMzBiZDRjZjAzNDhjY2VjMzlhNWJjMjAzMzUzOTMwNTA4OWM3YSIsImlhdCI6MTc0NzA0MjAyMi43NDczNywibmJmIjoxNzQ3MDQyMDIyLjc0NzM3NCwiZXhwIjoxNzYyOTM5NjIyLjczNzA1OCwic3ViIjoiMTMiLCJzY29wZXMiOltdfQ.KAQ7y_dbkKI8P14qaPs0t2OIBP7NHOzp3z5VWJ0eA60_mbgti94rWFq03O-aVx13kXXUwv-fnqN3NpabbyFfxQP_oh6uREU5-TI3GBIMCZKBYT-S_KPZjTmC0Ee84ApoKoYQcAPVY5kj2_8-ZaLd1mMqmhKgHrj89ZJVDl46R3ZuJM5ujlVO211ZFaC0pnZJ0vFiHdb-GCS6XuP677o8wyMIn6PdJv0c2LWI2hd65vOO-kLwzeU6Slj3ZEE6WluitCSttS2X9zY7npigI9EzEWyACL7zpUE2rM18LO9OtTiJ4AiCYfBDWdo_As0-9g0lnMh6wLlYu9v0yeFi-h0o1nvX4qLGwLdzfOHLLTJYnQH2EmdoWe6evTBgLQgZ8Fk1Ibn7w6NO9weq-xPo6ohGc_oY-PUWyaOgZbJXDb-ORK0mVFr2NdwCiAvnD_bTjWjp7AsFCLMrq54NCtFvOevcqELV4uC6lZJpTYG8FvoFJB1aL3u9mN04XcORaCbFQziYi5tdFqTTXEwsnfWsgkihslfR3abzbbDiXwLMrz-dJ_Fp1JUYFMbgRJXf74dietwaOwLBv_tu0t-qxeG8z-gPDS7oe6K6OTNp1UvI2RRiVX5oY6gw9ZPis6cf9gUXzztsWBWym9w-VX3o68Z6wUeRYf_fNwLFyUYSJ6DSFCcwRKY"        )
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}