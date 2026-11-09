package com.iamasaw.privacy.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val client: SupabaseClient,
) {
    val sessionStatus: StateFlow<SessionStatus>
        get() = client.auth.sessionStatus

    suspend fun requestCode(phone: String) {
        client.auth.signInWith(OTP) {
            this.phone = phone
            createUser = true
        }
    }

    suspend fun verifyCode(phone: String, code: String) {
        client.auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phone,
            token = code,
        )
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
