package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.mapper.runCatchingApp
import com.iamasaw.privacy.core.data.repository.mapper.toContactEntity
import com.iamasaw.privacy.core.data.repository.mapper.toUser
import com.iamasaw.privacy.core.database.dao.ContactDao
import com.iamasaw.privacy.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultContactRepository @Inject constructor(
    private val contactDao: ContactDao
) : ContactRepository {
    override fun observeAll(): Flow<List<User>> =
        contactDao.observeAll().map { contacts ->
            contacts.map { it.toUser() }
        }


    override suspend fun saveContact(user: User): AppResult<Unit> =
        runCatchingApp {
            contactDao.upsert(user.toContactEntity())
        }
}
