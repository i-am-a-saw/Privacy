package com.iamasaw.privacy.feature.contacts

import androidx.lifecycle.SavedStateHandle
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ContactRepository
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddContactViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val bob =
        User(
            id = UserId("bob-id"),
            username = "bob",
            displayName = "Bob",
            avatarPath = null,
        )

    @Test
    fun exactLookupDisplaysProfileAndSaveUsesContactRepository() =
        runTest {
            val profiles = FakeProfiles(bob)
            val contacts = FakeContacts()
            val viewModel =
                AddContactViewModel(profiles, contacts, SavedStateHandle())

            viewModel.initializeUsername("")
            viewModel.onUsernameChanged("bob")
            viewModel.search()
            runCurrent()

            assertEquals(listOf("bob"), profiles.lookups)
            assertEquals(bob, viewModel.uiState.value.profile)
            assertFalse(viewModel.uiState.value.saved)
            assertTrue(contacts.saveCalls.isEmpty())

            viewModel.save()
            runCurrent()

            assertEquals(listOf(bob), contacts.saveCalls)
            assertTrue(viewModel.uiState.value.saved)
        }

    @Test
    fun missingExactUsernameCannotBeSaved() =
        runTest {
            val profiles = FakeProfiles(bob)
            val contacts = FakeContacts()
            val viewModel =
                AddContactViewModel(profiles, contacts, SavedStateHandle())

            viewModel.initializeUsername("")
            viewModel.onUsernameChanged("bobby")
            viewModel.search()
            runCurrent()
            viewModel.save()
            runCurrent()

            assertEquals(listOf("bobby"), profiles.lookups)
            assertEquals(ContactError.NOT_FOUND, viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.profile)
            assertTrue(contacts.saveCalls.isEmpty())
            assertFalse(viewModel.uiState.value.saved)
        }

    @Test
    fun failedLocalSaveAllowsRetryWithoutReportingSuccess() =
        runTest {
            val contacts = FakeContacts()
            contacts.saveResult = AppResult.Failure(AppError.Unknown())

            val viewModel =
                AddContactViewModel(
                    FakeProfiles(bob),
                    contacts,
                    SavedStateHandle(),
                )

            viewModel.initializeUsername("bob")
            runCurrent()
            viewModel.save()
            runCurrent()

            assertFalse(viewModel.uiState.value.saved)
            assertFalse(viewModel.uiState.value.busy)
            assertEquals(ContactError.UNKNOWN, viewModel.uiState.value.error)

            contacts.saveResult = AppResult.Success(Unit)
            viewModel.save()
            runCurrent()

            assertEquals(2, contacts.saveCalls.size)
            assertTrue(viewModel.uiState.value.saved)
        }
}

private class FakeProfiles(
    private val profile: User,
) : ProfileRepository {
    val lookups = mutableListOf<String>()

    override suspend fun findByUsername(username: String): AppResult<User?> {
        lookups += username
        return AppResult.Success(profile.takeIf { it.username == username })
    }

    override suspend fun fetchOwnProfile(): AppResult<User?> = AppResult.Success(profile)

    override suspend fun createProfile(
        username: String,
        displayName: String?,
    ): AppResult<User> = error("Not used by these tests")
}

private class FakeContacts : ContactRepository {
    val saveCalls = mutableListOf<User>()
    var saveResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observeAll(): Flow<List<User>> = flowOf(emptyList())

    override suspend fun saveContact(user: User): AppResult<Unit> {
        saveCalls += user
        return saveResult
    }
}
