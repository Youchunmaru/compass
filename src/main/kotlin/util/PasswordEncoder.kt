package com.youchunmaru.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class PasswordEncoder {

    private val bcryptPasswordEncoder = BCryptPasswordEncoder()

    fun encode(rawPassword: String): String = bcryptPasswordEncoder.encode(rawPassword)

    fun matches(rawPassword: String, encodedPassword: String): Boolean = bcryptPasswordEncoder.matches(rawPassword, encodedPassword)
}