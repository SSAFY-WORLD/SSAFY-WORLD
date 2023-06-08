package com.ssafy.world.ui.main

import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place

class MainActivityViewModel : ViewModel() {
    var entryCommunityCollection = ""
    var title = ""
    var content = ""
    var place: Place? = null

    private fun saveWriteInform(_title: String, _content: String) {
        title = _title
        content = _content
    }

    fun getCommunityTitle() : String {
        return when(entryCommunityCollection) {
            "free" -> "자유게시판"
            "question" -> "질문게시판"
            "company" -> "취업게시판"
            "market" -> "장터게시판"
            "store" -> "맛집게시판"
            "room" -> "부동산게시판"
            else -> ""
        }
    }
}