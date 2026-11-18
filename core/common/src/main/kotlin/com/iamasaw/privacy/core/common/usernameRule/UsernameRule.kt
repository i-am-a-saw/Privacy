package com.iamasaw.privacy.core.common.usernameRule

object UsernameRule {
    private val pattern = Regex("^[a-z0-9_]{3,20}$")

    fun isValid(username: String): Boolean = pattern.matches(username)
}
