package ir.co.contact.data.source.local.database

import ir.co.contact.domain.model.Address
import ir.co.contact.domain.model.AddressType
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.model.Email
import ir.co.contact.domain.model.EmailType
import ir.co.contact.domain.model.PhoneNumber
import ir.co.contact.domain.model.PhoneType
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactEntityMappingTest {

    @Test
    fun `entity to domain preserves all fields`() {
        val entity = ContactEntity(
            id = "1",
            name = "Alice",
            phoneNumber = "555-1000",
            isFavorite = true,
            phoneNumbers = listOf(PhoneNumber("555-1000", PhoneType.MOBILE)),
            emails = listOf(Email("alice@example.com", EmailType.PERSONAL)),
            addresses = listOf(Address("1 Infinite Loop", AddressType.WORK))
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.phoneNumber, domain.phoneNumber)
        assertEquals(entity.isFavorite, domain.isFavorite)
        assertEquals(entity.phoneNumbers, domain.phoneNumbers)
        assertEquals(entity.emails, domain.emails)
        assertEquals(entity.addresses, domain.addresses)
    }

    @Test
    fun `domain to entity preserves all fields`() {
        val domain = Contact(
            id = "2",
            name = "Bob",
            phoneNumber = "555-2000",
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber("555-2000", PhoneType.HOME)),
            emails = listOf(Email("bob@company.com", EmailType.WORK)),
            addresses = listOf(Address("42 Galaxy Way", AddressType.HOME))
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.phoneNumber, entity.phoneNumber)
        assertEquals(domain.isFavorite, entity.isFavorite)
        assertEquals(domain.phoneNumbers, entity.phoneNumbers)
        assertEquals(domain.emails, entity.emails)
        assertEquals(domain.addresses, entity.addresses)
    }
}

