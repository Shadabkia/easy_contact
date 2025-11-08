package ir.co.contact.data.source.remote.network

import android.app.Application
import ir.co.contact.data.source.local.DataStoreConstants
import ir.co.contact.data.source.local.DataStoreManager
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


class TokenAuthenticator @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val context: Application
) : Authenticator {

    @OptIn(DelicateCoroutinesApi::class)
    override fun authenticate(route: Route?, response: Response): Request {

        return response.request.newBuilder()
            .header("Accept", "application/json")
            .header(
                "Authorization",
                dataStoreManager.getData(DataStoreConstants.ACCESS_TOKEN).toString()
            )
            .build()
    }
}