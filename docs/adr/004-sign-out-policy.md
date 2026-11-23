
# ADR-004: Clear local account data during sign-out

## Status

Accepted, 2026-11-23

## Context

The device stores the account session, saved contacts, and application
preferences. These must not be inherited by another account signing in
on the same installation.

Room, application preferences, and Supabase session storage cannot be
cleared in one shared database transaction.

## Decision

Route application sign-out through SignOutUseCase.

Block account screens while cleanup runs. Persist a pending-cleanup
flag before deleting local data.

Clear the local contact table and application user preferences.
Preserve the pending-cleanup flag until all cleanup finishes.

Attempt to revoke the current Supabase session with a bounded request.
Network failure must not prevent clearing the local session.

Clear the SDK-managed session before allowing another account to sign in.

Remove the pending-cleanup flag only after all required local cleanup
succeeds. Resume pending cleanup during application startup.

Keep the application blocked if required local cleanup fails, and offer
a retry.

## Consequences

- Signing out removes saved contacts from this installation.
- Signing out does not delete the Supabase account or profile.
- Another account starts with an empty local contact list.
- Local sign-out works without a backend connection.
- Server-side session revocation cannot be confirmed while offline.
- Interrupted cleanup is retried before account or login screens open.
- Future account-owned tables and preferences must join the same cleanup.
