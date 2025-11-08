package ir.co.contact.data.repositories

import ir.co.contact.data.source.remote.MainServices
import ir.co.contact.data.source.remote.network.Resource
import ir.co.contact.data.source.remote.network.SafeApiRequest
import ir.co.contact.domain.repositories.MainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val mServices: MainServices
) : MainRepository, SafeApiRequest() {

}