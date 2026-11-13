package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.dto.ProfileDto

fun ProfileDto.toUser(): User =
    User(
        id = UserId(id),
        username = username,
        displayName = displayName ?: username,
        avatarPath = avatarPath,
    )
