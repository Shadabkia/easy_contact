## EasyContact

EasyContact is a lean contacts client that mirrors the phone book, persists it locally, and keeps itself current every time the app wakes up.

It focuses on surfacing the essentials—name, phone, email, and addresses—through a clean Compose UI backed by Room, coroutines, and a unidirectional data flow.

Permissions are handled upfront, refresh runs quietly in the background, and the detail screen reflects whatever’s on-device without manual refreshes.

## Testing app: 
open app -> get contacts permission -> wait for sync contact ->
go to your phone contacts and change, add or remove a contact ->
back to EasyContact, contacts will sync automatically