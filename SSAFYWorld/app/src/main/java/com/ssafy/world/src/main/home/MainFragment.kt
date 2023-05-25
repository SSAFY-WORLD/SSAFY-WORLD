package com.ssafy.world.src.main.home


import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.local.entity.NotificationEntity
import com.ssafy.world.data.model.Calendar
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentMainBinding
import com.ssafy.world.src.main.MainActivityViewModel
import com.ssafy.world.src.main.community.CommunityViewModel
import com.ssafy.world.src.main.notification.NotificationListAdapter
import com.ssafy.world.src.main.notification.NotificationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "MainFragment"

class MainFragment :
    BaseFragment<FragmentMainBinding>(FragmentMainBinding::bind, R.layout.fragment_main) {

    companion object {
        val signUrl = "https://dydwkd486.github.io/ssafy-sign/"
        val signUploadUrl =
            "https://drive.google.com/drive/folders/1JrMoL6xU4Jv1wcFmwxVQyZfA9FLgy9uA"
        val eduUrl = "https://edu.ssafy.com/edu/main/index.do"
        val gitUrl = "https://github.com/Kim-JuYong"
    }

    private val viewModel: CommunityViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val myAdapter: MainCalendarAdapter by lazy {
        MainCalendarAdapter()
    }
    private val hotAdapter: MainHotAdapter by lazy {
        MainHotAdapter(myContext)
    }
    private val notificationAdapter: NotificationListAdapter by lazy {
        NotificationListAdapter()
    }
    private var calendarList: ArrayList<Calendar> = arrayListOf()
    private var hotList: ArrayList<Community> = arrayListOf()
    private var notificationList: MutableList<NotificationEntity> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 삼성 캘린더 API를 사용하여 일정 가져오기

        if (hasPermissionCalendar()) {
            fetchCalendarEvents(getCalendarId("juyong4190@gmail.com"))
        }

        initView()
        initRecycler()
        initListener()
        viewModel.getHotCommunities()
    }

    private fun initView() = with(binding) {
        val nickname = ApplicationClass.sharedPreferences.getUser()!!.nickname
        binding.mainHomeHello.text = "안녕하세요\n${nickname}님!"

        showCalendarBtn.setOnClickListener {
            if (!hasPermissionCalendar()) {
                showCustomToast("[마이페이지] > [권한 설정] 에서 권한을 설정해주세요.")
                return@setOnClickListener
            }
            openCalendarApp()
        }
        addCalendarBtn.setOnClickListener {
            if (!hasPermissionCalendar()) {
                showCustomToast("[마이페이지] > [권한 설정] 에서 권한을 설정해주세요.")
                return@setOnClickListener
            }
            openCalendarAppAdd()
        }
        showHotBtn.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_mainHotFragment)
        }
        // 알림 전체 보기
        showAlarmBtn.setOnClickListener {
            // 권한이 있을때
            if (hasPermissionNotification()) {
                navController.navigate(R.id.action_mainFragment_to_notificationFragment)
            } else {
                showCustomToast("[마이페이지] > [권한 설정] 에서 알림을 설정해주세요.")
            }
        }

        goSign.setOnClickListener {
            openUrlInExternalBrowser(signUrl)
        }
        goGoogle.setOnClickListener {
            openUrlInExternalBrowser(signUploadUrl)
        }
        goEduSsafy.setOnClickListener {
            openUrlInExternalBrowser(eduUrl)
        }
        goGithub.setOnClickListener {
            openUrlInExternalBrowser(gitUrl)
        }
        // 알림 클릭시 해당 Community화면으로 이동
        notificationAdapter.itemClickListener = object : NotificationListAdapter.ItemClickListener {
            override fun onClick(view: View, data: NotificationEntity) {
                val destination = data.destination.split("-")
                activityViewModel.entryCommunityCollection = destination[1]
                val bundle = Bundle().apply {
                    putString("communityId", destination[2])
                }
                navController.navigate(R.id.action_mainFragment_to_communityDetailFragment, bundle)
                notificationViewModel.deleteNotification(data)
            }
        }
    }

    fun openUrlInExternalBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun initRecycler() = with(binding) {
        myAdapter.submitList(calendarList.toMutableList())
        mainCalendarRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter
        }

        myAdapter.itemClickListener = object : MainCalendarAdapter.ItemClickListener {
            override fun onClick(data: Calendar) {
                openCalendar(data.id.toString())
            }
        }

        hotAdapter.submitList(hotList.toMutableList())
        mainHotRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = hotAdapter
        }

        hotAdapter.itemClickListener = object : MainHotAdapter.ItemClickListener {
            override fun onClick(data: Community) {
                activityViewModel.entryCommunityCollection = data.collection
                val action =
                    MainFragmentDirections.actionMainFragmentToCommunityDetailFragment(data.id)
                navController.navigate(action)
            }
        }

        // 알람 init
        notificationAdapter.submitList(notificationList.toMutableList())
        mainAlarmRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = notificationAdapter
        }
    }

    private fun initListener() = with(viewModel) {
        communityList.observe(viewLifecycleOwner) {
            hotList = it
            hotAdapter.submitList(hotList.toMutableList())
        }
        // 알람 실시간 탐지 -> 시간순으로
        notificationViewModel.notifications.observe(viewLifecycleOwner) {
            notificationList = it.toMutableList()
            notificationAdapter.submitList(notificationList.take(3))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        var itemCount = 0

        cursor?.use { cursor ->
            while (cursor.moveToNext() && itemCount < 3) {
                val eventId: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID))
                val title: String =
                    cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE))
                val startTime: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART))
                val endTime: Long =
                    cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND))

                val startTimeInstant: Instant = Instant.ofEpochMilli(startTime)
                val endTimeInstant: Instant = Instant.ofEpochMilli(endTime)

                val zoneId: ZoneId = ZoneId.systemDefault()
                val startDate: LocalDate =
                    LocalDate.ofEpochDay(startTimeInstant.epochSecond / (24 * 60 * 60))
                val endDate: LocalDate =
                    LocalDate.ofEpochDay(endTimeInstant.epochSecond / (24 * 60 * 60))

                // 필터링 조건을 설정하여 원하는 작업을 수행할 수 있습니다.
                val today: LocalDate = LocalDate.now()
                if (startDate.isEqual(today) || endDate.isAfter(today) || startDate.isAfter(today)) {
                    calendarList.add(
                        Calendar(
                            eventId,
                            title,
                            startDate.toString().substring(5),
                            endDate.toString().substring(5)
                        )
                    )
                    itemCount++
                }
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

    private fun openCalendarAppAdd() {
        val calendarIntent = Intent(Intent.ACTION_VIEW)
        calendarIntent.data = CalendarContract.CONTENT_URI
        startActivity(calendarIntent)
    }

    private fun openCalendarApp() {
        val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(calendarIntent)
    }

    private fun openCalendar(eventId: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("content://com.android.calendar/events/$eventId")
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        calendarList.clear()
    }

    private fun hasPermissionNotification(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasPermissionCalendar(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}


