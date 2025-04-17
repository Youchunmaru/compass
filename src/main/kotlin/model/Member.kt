package com.youchunmaru.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(val firstName: String, val lastName: String, val detail: MemberDetail?)
