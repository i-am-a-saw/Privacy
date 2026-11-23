package com.iamasaw.privacy.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val SIGN_OUT_TIMEOUT_MILLIS = 5_000L

class SupabaseAuthRemoteDataSource
    @Inject
    constructor(
        private val client: SupabaseClient,
    ) : AuthRemoteDataSource {
        override val sessionStatus: StateFlow<SessionStatus>
            get() = client.auth.sessionStatus

        override suspend fun requestCode(phone: String) {
            client.auth.signInWith(OTP) {
                this.phone = phone
                createUser = true
            }
        }

        override suspend fun verifyCode(
            phone: String,
            code: String,
        ) {
            client.auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = phone,
                token = code,
            )
        }

        override suspend fun signOut() {
            client.auth.awaitInitialization()

            withTimeoutOrNull(SIGN_OUT_TIMEOUT_MILLIS.milliseconds) {
                try {
                    client.auth.signOut(SignOutScope.LOCAL)
                } catch(_: RestException) {
                    // Continue with local session cleanup.
                } catch (_: IOException) {
                    // Continue with local session cleanup.
                }
            }

            client.auth.clearSession()
        }
    }
