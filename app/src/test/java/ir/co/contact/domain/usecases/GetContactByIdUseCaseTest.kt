package ir.co.contact.domain.usecases

import ir.co.contact.domain.model.Address
import ir.co.contact.domain.model.AddressType
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.model.Email
import ir.co.contact.domain.model.EmailType
import ir.co.contact.domain.model.PhoneNumber
import ir.co.contact.domain.model.PhoneType
import ir.co.contact.domain.repositories.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetContactByIdUseCaseTest {

    private val sampleContact = Contact(
        id = "123",
        name = "Jane Doe",
        phoneNumber = "555-0000",
        isFavorite = true,
        phoneNumbers = listOf(PhoneNumber("555-0000", PhoneType.MOBILE)),
        emails = listOf(Email("jane@sample.com", EmailType.WORK)),
        addresses = listOf(Address("1234 Sample Street", AddressType.HOME))
    )

    private val fakeRepository = object : ContactRepository {
        override fun getContacts(): Flow<List<Contact>> = flowOf(listOf(sampleContact))

        override fun getContactById(contactId: String): Flow<Contact?> {
            return flowOf(if (contactId == sampleContact.id) sampleContact else null)
        }

        override suspend fun syncContactsFromPhone(contentResolver: android.content.ContentResolver) =
            error("Not needed for this test")

        override suspend fun hasCachedContacts(): Boolean = false

        override fun observeContactChanges(): Flow<Unit> = flowOf(Unit)
    }

    private val useCase = GetContactByIdUseCase(fakeRepository)

    @Test
    fun `invoke returns contact when id exists`() = runBlocking {
        val result = useCase(sampleContact.id)

        assertEquals(sampleContact, result.first())
    }

    @Test
    fun `invoke returns null when contact missing`() = runBlocking {
        val result = useCase("missing-contact")

        assertEquals(null, result.first())
    }
}

