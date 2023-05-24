package com.ssafy.world.src.main

import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.ssafy.world.data.model.Photo

class MainActivityViewModel : ViewModel() {
    var communityTitle = ""
    var entryCommunityCollection = ""
    var title = ""
    var content = ""
    var place: Place? = null

    private fun saveWriteInform(_title: String, _content: String) {
        title = _title
        content = _content
    }
}