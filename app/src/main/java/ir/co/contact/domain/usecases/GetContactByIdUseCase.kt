package ir.co.contact.domain.usecases

import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.repositories.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactByIdUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(contactId: String): Flow<Contact?> {
        return contactRepository.getContactById(contactId)
    }
}

