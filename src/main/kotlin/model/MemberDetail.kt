package com.youchunmaru.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberDetail(var email: String?, var address: String?)
