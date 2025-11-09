package ir.co.contact.domain.usecases

import android.content.ContentResolver
import ir.co.contact.domain.repositories.ContactRepository
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contentResolver: ContentResolver): Result<Unit> {
        return contactRepository.syncContactsFromPhone(contentResolver)
    }
}

