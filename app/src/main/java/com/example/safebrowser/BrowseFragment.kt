package com.example.safebrowser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.example.safebrowser.databinding.FragmentBrowseBinding
import com.example.safebrowser.databinding.FragmentHomeBinding

class BrowseFragment (private var urlNew:String) : Fragment() {

    lateinit var binding: FragmentBrowseBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_browse,container,false)
        binding= FragmentBrowseBinding.bind(view)

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        binding.webView.apply {
            settings.javaScriptEnabled=true
            settings.setSupportZoom(true)
            settings.builtInZoomControls=true
            settings.displayZoomControls=false
            webViewClient= WebViewClient()
            webChromeClient= WebChromeClient()

            loadUrl(urlNew)
        }
    }
}