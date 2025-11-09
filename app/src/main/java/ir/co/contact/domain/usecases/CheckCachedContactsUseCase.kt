package ir.co.contact.domain.usecases

import ir.co.contact.domain.repositories.ContactRepository
import javax.inject.Inject

class CheckCachedContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(): Boolean {
        return contactRepository.hasCachedContacts()
    }
}

