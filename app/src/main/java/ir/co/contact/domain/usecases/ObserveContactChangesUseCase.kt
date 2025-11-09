package ir.co.contact.domain.usecases

import ir.co.contact.domain.repositories.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that observes changes to device contacts.
 * Emits a signal whenever contacts are added, deleted, or modified.
 */
class ObserveContactChangesUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(): Flow<Unit> {
        return contactRepository.observeContactChanges()
    }
}

