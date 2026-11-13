package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.dto.ProfileDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class DefaultProfileRepositoryTest {
    @Test
    fun `create profile maps dto to user`() =
        runTest {
            val fake =
                FakeProfileRemoteDataSource().apply {
                    createResult =
                        Result.success(
                            ProfileDto(
                                id = "user-alice",
                                username = "alice",
                                displayName = "Alice",
                                avatarPath = null,
                            ),
                        )
                }
            val repository = DefaultProfileRepository(fake)

            assertEquals(
                AppResult.Success(User(UserId("user-alice"), "alice", "Alice", null)),
                repository.createProfile(username = "alice", displayName = "Alice"),
            )
        }

    @Test
    fun `find by username without profile is success with null`() =
        runTest {
            val fake =
                FakeProfileRemoteDataSource().apply {
                    findByUserNameResult = Result.success(null)
                }
            val repository = DefaultProfileRepository(fake)

            assertEquals(
                AppResult.Success(null),
                repository.findByUsername(username = "nobody"),
            )
        }

    @Test
    fun `network failure is returned as no network`() =
        runTest {
            val fake =
                FakeProfileRemoteDataSource().apply {
                    findByUserNameResult = Result.failure(IOException("offline"))
                }
            val repository = DefaultProfileRepository(fake)

            assertEquals(
                AppResult.Failure(AppError.NoNetwork),
                repository.findByUsername(username = "alice"),
            )
        }
}
