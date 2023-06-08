package com.ssafy.world.ui.main.community.map

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentCommunityMapSearchBinding
import com.ssafy.world.ui.main.MainActivityViewModel

private const val TAG = "CommunityMapSearchFragm"
class CommunityMapSearchFragment : BaseFragment<FragmentCommunityMapSearchBinding>(
    FragmentCommunityMapSearchBinding::bind,
    R.layout.fragment_community_map_search
) {

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    private lateinit var placesClient: PlacesClient

    private val myAdapter: CommunityMapSearchAdapter by lazy {
        CommunityMapSearchAdapter()
    }
    private var myList = arrayListOf<Place>()
    private var totalCount = 0
    private var loadedCount = 0

    private lateinit var place: Place


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activityViewModel.place = null
        // Places API 초기화
        Places.initialize(myContext, getString(R.string.google_map_api_key))
        placesClient = Places.createClient(myContext)

        initView()
    }

    private fun initView() = with(binding) {
        idEditTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = idEditTextView.text.toString().trim()
                if (searchText.isEmpty()) {
                    idEditTextView.error = "장소를 입력해주세요." // 에러 메시지 설정
                    return@setOnEditorActionListener true
                }
                fetchPlaces(idEditTextView.text.toString())

                return@setOnEditorActionListener true
            }
            false
        }

        myAdapter.submitList(myList.toMutableList())
        myAdapter.itemClickListener = object : CommunityMapSearchAdapter.ItemClickListener {
            override fun onClick(data: Place) {
                activityViewModel.place = data
                navController.navigate(R.id.action_communityMapSearchFragment_to_communityMapWriteFragment)
            }
        }
        communityRv.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun fetchPlaces(keyword: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(keyword)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                totalCount = response.autocompletePredictions.size

                for (prediction in response.autocompletePredictions) {
                    val placeId = prediction.placeId
                    fetchPlaceDetails(placeId)
                }
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    val message = exception.message
                    Log.e(TAG, "Place prediction fetch failed: $statusCode $message")
                }
            }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.LAT_LNG
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place

                myList.add(place)
                loadedCount++
                if (loadedCount == totalCount) {
                    myAdapter.submitList(myList.toMutableList())
                    loadedCount = 0
                    myList.clear()
                }
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    val message = exception.message
                    Log.e(TAG, "Place details fetch failed: $statusCode $message")
                }
            }
    }
}