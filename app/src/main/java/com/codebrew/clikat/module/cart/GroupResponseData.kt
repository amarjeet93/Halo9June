package com.codebrew.clikat.module.cart

class GroupResponseData {
    val data: GroupData? = null
    val message: String? = null
    val status: Int? = null
}

class GroupData {
    val result: ArrayList<GroupResultData>? = null
    val type: String? = null
}

class GroupResultData {
    val id: Int? = null
    val group_name: String? = null
    val created_by_user_id: String? = null
    val user_ids: String? = null
    val supplier_id: Int? = null
    val created_at: String? = null
    val updated_at: String? = null
    val is_active: Int? = null
}
