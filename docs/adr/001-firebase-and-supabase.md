# ADR-001: Supabase instead of Firebase

## Status

Accepted, 2026-10-22

## Context

The app is an educational project with no commercial plans. It must cost nothing, and we will not attach a billing account to any service.

The backend could be Firebase or Supabase. Firebase's free Spark plan needs no payment method, but it does not include phone authentication, phone number verification, Cloud Storage or Cloud Functions. Those need the pay-as-you-go Blaze plan, which requires a billing account ([Firebase pricing](https://firebase.google.com/pricing)).

Supabase's free plan includes authentication (unlimited total users, 50,000 monthly active users), a 500 MB database, 1 GB of file storage, 500,000 Edge Function invocations and Realtime with 200 concurrent connections ([Supabase pricing](https://supabase.com/pricing#compare-plans)).

Supabase does not send SMS itself. Phone sign-in codes go through an SMS provider, such as Twilio or MessageBird, which bills for every message. Test phone numbers with fixed codes send no SMS and cost nothing.

Prices checked on 2026-10-22.

## Decision

We will use Supabase for authentication, the database, file storage, server-side functions and real-time message exchange. Push notifications will still go through Firebase Cloud Messaging, which is free on the Spark plan and needs no billing account.

## Consequences

- The whole backend runs on free plans, with no billing account on either platform.
- Supabase is open source and can be self-hosted later, so the server and the metadata it holds could move under our control.
- Real users cannot sign in by SMS while the no-billing-account rule holds; development uses test phone numbers only. Choosing a paid SMS provider before any public release will need a new ADR.
- Verified locally in milestone 0, step 13: a configured test phone number has a confirmed record in `auth.users` without an SMS provider account. The CLI required an enabled SMS provider configuration, so local-only placeholder credentials were used; the test OTP did not require real SMS delivery. Hosted-project behavior remains unverified. The local Studio Users view did not show the record, although a direct database query found it.
- Free projects pause after one week of inactivity, and only two can be active at once, so we use them as "dev" and "demo".
- Uploads are capped at 50 MB per file, which limits media messages in milestone 5.
- The database is limited to 500 MB, so the server must delete messages once they are delivered.
- We manage two platforms: a small Firebase project remains for push notifications.
