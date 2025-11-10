## EasyContact
EasyContact is a lean contacts client that mirrors the phone book, persists it locally, and keeps itself current every time the app wakes up.
It focuses on surfacing the essentials—name, phone, email, and addresses—through a clean Compose UI backed by Room, coroutines, and a unidirectional data flow.
Permissions are handled upfront, refresh runs quietly in the background, and the detail screen reflects whatever's on-device without manual refreshes.

## Sync Contact flow
- **First launch**: The app runs `ContactRepository.syncContactsFromPhone`, pulling all device contacts through a `ContentResolver` and storing them in Room via `ContactDao.replaceContacts`.
- **Incremental sync**: On subsequent launches, only contacts modified after the last sync are fetched using `CONTACT_LAST_UPDATED_TIMESTAMP`, dramatically improving performance for large contact lists.
- **Deletion detection**: The sync process compares contact IDs to detect and remove deleted contacts from the local database.
- **UI reactivity**: `GetContactsUseCase` exposes a Flow consumed by `ContactListViewModel`, which updates the Compose list whenever the database changes.
- **Live updates**: A `ContactObserver` listens for device contact changes; when triggered, the repository performs an incremental resync so Room stays in sync with the phone book without manual refreshes.

## Testing app manually:
open app -> get contacts permission -> wait for sync contact ->
go to your phone contacts and change, add or remove a contact ->
back to EasyContact, contacts will sync automatically

## Unit tests
- Run `./gradlew test` to execute JVM unit tests.
- Place new tests under `app/src/test/java`, e.g. `ir/co/contact/domain/...`.
- `GetContactByIdUseCaseTest` validates fetching a single contact by id.
- `ContactEntityMappingTest` ensures Room entity to domain model conversions keep all fields intact.