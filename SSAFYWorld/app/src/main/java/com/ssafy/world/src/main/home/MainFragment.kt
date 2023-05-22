package com.ssafy.world.src.main.home


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Calendar
import com.ssafy.world.databinding.FragmentMainBinding

private const val TAG = "MainFragment"

class MainFragment :
    BaseFragment<FragmentMainBinding>(FragmentMainBinding::bind, R.layout.fragment_main) {

    private val myAdapter: MainCalendarAdapter by lazy {
        MainCalendarAdapter()
    }
    private var calendarList: ArrayList<Calendar> = arrayListOf()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 삼성 캘린더 API를 사용하여 일정 가져오기
        fetchCalendarEvents(getCalendarId("juyong4190@gmail.com"))
        initRecycler()
    }

    private fun initRecycler() = with(binding) {
        myAdapter.submitList(calendarList.toMutableList())
        mainCalendarRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter
        }
    }

    @SuppressLint("Range")
    private fun fetchCalendarEvents(calendarId: Long?) {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val projection: Array<String> = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        val selection: String = "${CalendarContract.Events.CALENDAR_ID} = ?"
        val selectionArgs: Array<String> = arrayOf(calendarId.toString())
        val sortOrder: String = "${CalendarContract.Events.DTSTART} ASC"

        val cursor: Cursor? =
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val eventId: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID))
                val title: String =
                    cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE))
                val startTime: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))
                val endTime: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND))

                // 일정 정보를 사용하여 원하는 작업을 수행할 수 있습니다.
                calendarList.add(Calendar(eventId, title, startTime, endTime))
            }
            myAdapter.submitList(calendarList.toMutableList())
        }
    }


    @SuppressLint("Range")
    private fun getCalendarId(calendarDisplayName: String): Long? {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val projection: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        val selection: String = "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} = ?"
        val selectionArgs: Array<String> = arrayOf(calendarDisplayName)

        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, null)

        var calendarId: Long? = null
        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                calendarId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID))
            }
        }
        Log.d(TAG, "getCalendarId: $calendarId")
        return calendarId
    }


}


