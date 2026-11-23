package com.iamasaw.privacy

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.LocalDataRepository

class FakeLocalDataRepository : LocalDataRepository {
    var pending = false
    var clearResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun isSignOutPending(): AppResult<Boolean> = AppResult.Success(pending)

    override suspend fun beginSignOut(): AppResult<Unit> {
        pending = true
        return AppResult.Success(Unit)
    }

    override suspend fun clearUserData(): AppResult<Unit> = clearResult

    override suspend fun completeSignOut(): AppResult<Unit> {
        pending = false
        return AppResult.Success(Unit)
    }
}
