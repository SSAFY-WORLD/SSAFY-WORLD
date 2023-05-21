package com.ssafy.world.src.main.photo

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Photo
import com.ssafy.world.databinding.FragmentPhotoBinding

private const val TAG = "PhotoFragment_싸피"

class PhotoFragment :
    BaseFragment<FragmentPhotoBinding>(FragmentPhotoBinding::bind, R.layout.fragment_photo) {
    private val images: MutableList<Photo> = mutableListOf()
    private val myAdapter: PhotoGridAdapter by lazy {
        PhotoGridAdapter(myContext)
    }

    private var imageCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPhotos()
        initView()
    }

    private fun initView() = with(binding) {
        myAdapter.submitList(images)
        photoRv.apply {
            layoutManager = GridLayoutManager(myContext, 3)
            adapter = myAdapter
        }


        myAdapter.checkBoxClickListener = object : PhotoGridAdapter.CheckBoxClickListener {
            override fun onClick(data: Photo) {
                imageCount += if (data.isSelected) 1 else -1
                photoTvCount.visibility = if (imageCount > 0) View.VISIBLE else View.GONE
                photoTvCount.text = imageCount.toString()
            }
        }

        myAdapter.itemClickListener = object : PhotoGridAdapter.ItemClickListener {
            override fun onClick(data: Photo) {
                PhotoFullDialog(data.url).show(requireActivity().supportFragmentManager, "")
            }
        }

        photoBtnComplete.setOnClickListener {
            val selectedPhotos = arrayListOf<Photo>()
            myAdapter.currentList.forEach {
                if (it.isSelected) {
                    selectedPhotos.add(it)
                }
            }
            val gson = Gson()
            val photoListJson = gson.toJson(selectedPhotos)
            val bundle = Bundle()
            bundle.putString("photoListJson", photoListJson)
            navController.navigate(R.id.action_photoFragment_to_communityWriteFragment, bundle)
        }
    }

    private fun loadPhotos() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = myContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(idColumnIndex)
                val imageName = cursor.getString(nameColumnIndex)
                val imageData = cursor.getString(dataColumnIndex)
                Log.d(TAG, "loadPhotos: $imageName")
                // 이미지 데이터를 사용하여 필요한 작업 수행
                images.add(Photo().apply {
                    url = imageData
                    isSelected = false
                })
            }
        }
    }
}