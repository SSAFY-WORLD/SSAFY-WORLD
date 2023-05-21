package com.ssafy.world.src.main

import androidx.lifecycle.ViewModel
import com.ssafy.world.data.model.Photo

class MainActivityViewModel : ViewModel() {
    var communityTitle = ""
    var entryCommunityCollection = ""
    var title = ""
    var content = ""

    private fun saveWriteInform(_title: String, _content: String) {
        title = _title
        content = _content
    }
}