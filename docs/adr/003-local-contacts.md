# ADR-003: Keep contacts on the device

## Status

Accepted, 2026-11-18

## Context

The messenger needs a saved contact list. Supabase already stores public
profile information and supports exact username lookup, but saving a
contact does not require storing the relationship between two users
on the backend.

## Decision

Store contacts in the app's Room 3 database.

Saving, updating, and removing contacts will modify only the local
database. Do not create a backend contacts table or upload the saved
contact list.

Exclude app databases from Android cloud backup and device transfer.

Use BundledSQLiteDriver initially. Database encryption and the
migration to a compatible SQLCipher integration are planned for
milestone 7.

## Consequences

- Contacts remain available without a network connection.
- Contacts do not synchronize between devices.
- Clearing app data or uninstalling the app removes the local list.
- Profile lookup still sends the requested username to Supabase.
- Cached profile names and avatar paths can become outdated.
- The database is initially unencrypted.
- Milestone 1, step 10 must clear the database during sign-out.
- Future schema changes require versioned migrations and migration tests.
