package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.database.entity.ContactEntity
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId

fun User.toContactEntity(): ContactEntity =
    ContactEntity(
        userId = id.value,
        username = username,
        displayName = displayName,
        avatarPath = avatarPath,
    )

fun ContactEntity.toUser(): User =
    User(
        id = UserId(userId),
        username = username,
        displayName = displayName,
        avatarPath = avatarPath,
    )
