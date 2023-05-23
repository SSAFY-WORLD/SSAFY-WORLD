package com.ssafy.world.src.main.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentWebviewBinding
import com.ssafy.world.src.main.community.CommunityDetailFragmentArgs

class WebViewFragment : BaseFragment<FragmentWebviewBinding>(FragmentWebviewBinding::bind, R.layout.fragment_webview) {
    private val safeArgs: WebViewFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() = with(binding) {
        val url = safeArgs.url
        webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)

            // 웹뷰의 초기 크기 설정
            setInitialScale(100) // 1이 기본 크기입니다. 조정이 필요하면 원하는 값으로 변경하세요.

            // 확대/축소 제어 설정
            settings.setSupportZoom(true) // 확대/축소를 지원하도록 설정
            settings.builtInZoomControls = true // 내장된 확대/축소 컨트롤을 사용할 수 있도록 설정
            settings.displayZoomControls = false
        }
    }

}