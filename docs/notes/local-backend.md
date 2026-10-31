# Local backend — milestone 0, step 13

## Setup

This project uses Supabase CLI 2.122.0 through `npx.cmd` with Node.js 24.13.1 on Windows. Docker Desktop 4.97.0 runs the Linux containers with Docker Engine 29.9.2. Start Docker Desktop before running these commands from the repository root:

```powershell
npx.cmd --yes supabase@2.122.0 start
npx.cmd --yes supabase@2.122.0 status
npx.cmd --yes supabase@2.122.0 stop
```

`supabase/config.toml` defines the local services and ports. The default API URL is `http://127.0.0.1:54321`, PostgreSQL uses port `54322`, and Studio is at `http://127.0.0.1:54323`. Check `status` for the actual URLs and local publishable key. A normal `stop` preserves local database data.

## Phone authentication check

`[auth.sms.test_otp]` maps the synthetic number `+16505550101` to the fixed code `123456`; the TOML key omits the `+`. Phone signup and confirmation are enabled. The CLI disables phone login unless an SMS provider is configured, so `[auth.sms.twilio]` contains placeholder credentials for this local test. These credentials cannot deliver real SMS.

The local Auth service reports phone login enabled. A direct query of `auth.users` found one record for the synthetic number with `phone_confirmed_at` set. This verifies local test-number authentication without an SMS provider account. It does not establish how a hosted project behaves.

The local Studio service is healthy, but its Authentication → Users view did not show that record during this check. The direct database query is the evidence for the confirmed user; the Studio display discrepancy remains open.

## What this setup taught me

The CLI reads `config.toml` to assemble a local backend from Docker containers, so changing Auth settings requires restarting the stack. The database keeps its data across a normal stop and start, while the generated configuration belongs in Git for repeatable setup. A fixed test OTP avoids sending an SMS, but the CLI still checks that an SMS provider is enabled before allowing phone login. Studio is a view of the backend, so a missing row there should be checked against the database before drawing a conclusion about authentication.
