package ir.co.contact.data.source.remote.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import ir.co.contact.domain.model.AuthErrorResponse
import ir.co.contact.domain.model.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

abstract class SafeApiRequest {

    fun <T : Any> apiRequest(call: suspend () -> Response<T>): Flow<Resource<T>> {
        return flow {
            emit(Resource.Loading())
            try {
                // Do call and wait for response
                val response = call()
                if (response.isSuccessful) {
                    if (response.headers()["Content-Type"]?.contains("application/json") == true) {
                        // Safe to parse with Moshi
                        emit(Resource.Success(response.body()!!))
                    } else {
                        // Handle unexpected HTML or plain text
                        val errorBody = response.errorBody()?.string()
                        Timber.tag("API").e("Unexpected response: $errorBody")
                    }
                } else {
                    response.errorBody()?.string()?.let { errorBody ->
                        when (isJSONValid(errorBody)) {
                            true -> try {
                                // Convert Error ro ErrorResponse
                                val moshi: Moshi = Moshi.Builder().build()
                                val errorAdapter: JsonAdapter<ErrorResponse> = moshi.adapter(
                                    ErrorResponse::class.java)
                                val authErrorAdapter: JsonAdapter<AuthErrorResponse> = moshi.adapter(
                                    AuthErrorResponse::class.java)

                                val message = errorAdapter.fromJson(
                                    errorBody,
                                )?.message
                                    // Convert Error to AuthErrorResponse (for keycloak) when cant convert to ErrorResponse
                                    ?: authErrorAdapter.fromJson(
                                    errorBody,
                                ) ?.message ?: "An unexpected error occurred"

                                Timber.e("Error message: $message")

                                emit(Resource.Error(message))
                            // Json format is different with kotlin class
                            } catch (e: JSONException) {
                                emit(Resource.Error("Message is not compatible with ErrorResponse class!!"))
                            }
                            // Bad gate way error (returns html)
                            false -> emit(Resource.Error("Server error"))
                        }
                    }
                }
            } catch (ex: NoInternetException) {
                emit(Resource.Error(ex.message!!))
            } catch (ex: HttpException) {
                emit(Resource.Error(ex.localizedMessage ?: "An unexpected error occurred 2"))
            } catch (ex: IOException) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }!!
        }
    }
}

suspend fun ResponseBody.stringSuspending() =
    withContext(Dispatchers.IO) { string() }

// Check input is json or not
fun isJSONValid(input: String): Boolean {
    try {
        JSONObject(input)
    } catch (ex: JSONException) {
        try {
            JSONArray(input)
        } catch (ex: JSONException) {
            return false
        }
    }
    return true
}

// inline fun <reified T> Moshi.jsonToObject(json: String): T? =
//    adapter(T::class.java).fromJson(json)