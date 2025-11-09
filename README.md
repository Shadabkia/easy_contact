## EasyContact

EasyContact is a lean contacts client that mirrors the phone book, persists it locally, and keeps itself current every time the app wakes up.

It focuses on surfacing the essentials—name, phone, email, and addresses—through a clean Compose UI backed by Room, coroutines, and a unidirectional data flow.

Permissions are handled upfront, refresh runs quietly in the background, and the detail screen reflects whatever’s on-device without manual refreshes.

## Sync Contact flow
- On first launch (or permission grant) the app runs `ContactRepository.syncContactsFromPhone`, pulling device contacts through a `ContentResolver` and storing them in Room via `ContactDao.replaceContacts`.
- UI reads from Room only: `GetContactsUseCase` exposes a Flow consumed by `ContactListViewModel`, which updates the Compose list whenever the database changes.
- A `ContactObserver` listens for device contact changes; when triggered, the repository resyncs so Room stays in sync with the phone book without manual refreshes.

## Testing app manually:
open app -> get contacts permission -> wait for sync contact ->
go to your phone contacts and change, add or remove a contact ->
back to EasyContact, contacts will sync automatically

## Unit tests
- Run `./gradlew test` to execute JVM unit tests.
- Place new tests under `app/src/test/java`, e.g. `ir/co/contact/domain/...`.
- `GetContactByIdUseCaseTest` validates fetching a single contact by id.
- `ContactEntityMappingTest` ensures Room entity to domain model conversions keep all fields intact.