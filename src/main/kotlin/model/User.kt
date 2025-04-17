package com.youchunmaru.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val password: String){

    fun getName() = name
    fun getPassword() = password
}
