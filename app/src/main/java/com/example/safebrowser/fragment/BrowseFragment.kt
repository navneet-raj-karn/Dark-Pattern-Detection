package com.example.safebrowser.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.safebrowser.activity.MainActivity
import com.example.safebrowser.R
import com.example.safebrowser.databinding.FragmentBrowseBinding
import java.io.ByteArrayOutputStream


class BrowseFragment (private var urlNew:String) : Fragment() {


    lateinit var binding: FragmentBrowseBinding
    private var darkPatternFound = false
    var webIcon: Bitmap? = null

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

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        val mainRef=requireActivity() as MainActivity
        binding.webView.apply {
            settings.javaScriptEnabled=true
            settings.setSupportZoom(true)
            settings.builtInZoomControls=true
            settings.displayZoomControls=false
            webViewClient= object: WebViewClient(){

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if(MainActivity.isDesktopSite)
                        view?.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                                " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null)


                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainRef.binding.topSearchBar.text=SpannableStringBuilder(url)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let {
                        // Load the URL in the WebView
                        view?.loadUrl(url)
                        return true // Indicate that the URL has been handled
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainRef.binding.progressBar.progress=0
                    mainRef.binding.progressBar.visibility=View.VISIBLE
                    if(url!!.contains("you", ignoreCase = false)) mainRef.binding.root.transitionToEnd()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainRef.binding.progressBar.visibility=View.GONE
                    binding.webView.zoomOut()
                    extractAndPassText()
                }


            }
            webChromeClient=object: WebChromeClient(){
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    try {
                        mainRef.binding.webIcon.setImageBitmap(icon)
                        webIcon = icon
                        MainActivity.bookmarkIndex = mainRef.isBookmarked(view?.url!!)
                        if(MainActivity.bookmarkIndex != -1){
                            val array = ByteArrayOutputStream()
                            icon!!.compress(Bitmap.CompressFormat.PNG, 100, array)
                            MainActivity.bookmarkList[MainActivity.bookmarkIndex].image = array.toByteArray()
                        }

                    }catch (e: Exception){}
                }
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility=View.GONE
                    binding.customView.visibility=View.VISIBLE
                    binding.customView.addView(view)
                    mainRef.binding.root.transitionToEnd()
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webView.visibility=View.VISIBLE
                    binding.customView.visibility=View.GONE
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainRef.binding.progressBar.progress=newProgress
                }
            }
            when{
                URLUtil.isValidUrl(urlNew)->loadUrl(urlNew)
                urlNew.contains(".com",ignoreCase = true)->loadUrl(urlNew)
                else->loadUrl("https://www.google.com/search?q=$urlNew")
            }

            binding.webView.setOnTouchListener { _, event ->
                mainRef.binding.root.onTouchEvent(event)
                return@setOnTouchListener false
            }

        }
    }

    override fun onPause() {
        super.onPause()
        binding.webView.apply {
            clearMatches()
            clearHistory()
            clearFormData()
            clearSslPreferences()
            clearCache(true)

            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
        }
    }

    private fun extractAndPassText(){
        binding.webView.evaluateJavascript("(function()\n" +
                "{\n" +
                "function isHidden(el) {\n" +
                "    var style = window.getComputedStyle(el);\n" +
                "    return style.display === 'none' || style.visibility === 'hidden';\n" +
                "}\n" +
                "\n" +
                "function containsJavaScriptCode(text) {\n" +
                "    // Extended list of common JavaScript keywords\n" +
                "    const javascriptKeywords = [\n" +
                "        'document', 'window', 'setTimeout', 'setInterval', 'XMLHttpRequest', 'fetch',\n" +
                "        'addEventListener', 'querySelector', 'getElementById', 'innerHTML', 'JSON',\n" +
                "        'jquery', 'angular', 'react', 'Vue', 'axios', 'async', 'await', 'Promise',\n" +
                "        'map', 'reduce', 'splice', 'querySelectorAll'\n" +
                "        // Add more as needed\n" +
                "    ];\n" +
                "    return javascriptKeywords.some(keyword => text && text.includes(keyword));\n" +
                "}\n" +
                "\n" +
                "var body = document.querySelector('body');\n" +
                "var allTags = body.getElementsByTagName('*');\n" +
                "var processedTextSet = new Set();\n" +
                "var concatenatedText = '';\n" +
                "\n" +
                "// Iterate through each tag\n" +
                "for (var i = 0, max = allTags.length; i < max; i++) {\n" +
                "    var currentTag = allTags[i];\n" +
                "\n" +
                "    // Include inner text of <h2> tags with the specified class\n" +
                "    if (\n" +
                "        (currentTag.tagName.toLowerCase() === 'h2' &&\n" +
                "            currentTag.classList.contains('a-carousel-heading')) ||\n" +
                "        // Skip elements containing a script or specific patterns\n" +
                "        currentTag.tagName.toLowerCase() === 'script' ||\n" +
                "        !currentTag.innerText || // Check if innerText is defined\n" +
                "        currentTag.innerText.includes('window.ue_ibe') ||\n" +
                "        containsJavaScriptCode(currentTag.innerText)\n" +
                "    ) {\n" +
                "        continue;\n" +
                "    }\n" +
                "\n" +
                "    // Trim leading and trailing spaces, and replace multiple consecutive spaces with a single space\n" +
                "    var innerText = currentTag.innerText.trim().replace(/\\s+/g, ' ');\n" +
                "\n" +
                "    // Define a set of special characters you want to exclude\n" +
                "    var specialCharacters = \"!@#\$%^&*()_+~`-=[]{}|;:'\\\",.<>?/\";\n" +
                "\n" +
                "    if (!specialCharacters.includes(innerText.charAt(0)) && !processedTextSet.has(innerText)) {\n" +
                "        concatenatedText += '*' + innerText;\n" +
                "        processedTextSet.add(innerText);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "return concatenatedText;\n" +
                "})();")
        { html ->

           val resultList = extractEnclosedSentences(html)
            val finallist = splitStrings(resultList);
            for(it in finallist)
                Log.d("sam",it);
           processText(finallist)

        }

    }

    private fun processText(chunks: List<String>) {
        val mainActivity = requireActivity() as MainActivity
        val maxChunkSize = 50
        var darkcnt=1;
        var nondark=1;
        val darklist = mutableListOf<String>()


        for (item in chunks) {
            val result = mainActivity.performPrediction(item)
            if (result == "Dark Pattern") {
                darkPatternFound = true
                darklist.add(item)
                darkcnt++; // Toast.makeText(requireContext(), "$result for "+ item, Toast.LENGTH_LONG).show()
            }
            else
                nondark++;


        }
        for (it in darklist) {
            Log.d("Darklist",it)
           highlightDarkPatterns(it)
        }



        //checking ratio but not perfect


        if(darkcnt*100/(nondark+darkcnt)>=1)
            Toast.makeText(requireContext(), "Dark Detected", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(requireContext(), "Clean Page", Toast.LENGTH_LONG).show()


    }
    //chunking those scrapped text , bad Time complexity but okay
    fun extractEnclosedSentences(inputString: String): List<String> {
        val pattern = Regex("\\*(.*?)\\*") // Using a non-greedy match to capture content between '*'
        val matchResults = pattern.findAll(inputString)
        return matchResults.map { it.groupValues[1] }.toList()
    }

    private fun highlightDarkPatterns(chunk: String) {

        val javascript = "javascript:(function() {" +
                "var searchText = '" + chunk + "';" +
                "var regex = new RegExp(searchText, 'gi');" +
                "var body = document.body;" +
                "var textNodes = getTextNodes(body);" +
                "textNodes.forEach(function(node) {" +
                "   var matches = node.nodeValue.match(regex);" +
                "   if (matches) {" +
                "       var span = document.createElement('span');" +
                "       span.innerHTML = node.nodeValue.replace(regex, '<span style=\"background-color: yellow;\">$&</span>'); " +
                "       node.parentNode.replaceChild(span, node);" +
                "   }" +
                "});" +
                "function getTextNodes(node) {" +
                "   var textNodes = [];" +
                "   if (node.nodeType == 3) {" +
                "       textNodes.push(node);" +
                "   } else {" +
                "       var children = node.childNodes;" +
                "       for (var i = 0; i < children.length; i++) {" +
                "           textNodes = textNodes.concat(getTextNodes(children[i]));" +
                "       }" +
                "   }" +
                "   return textNodes;" +
                "}" +
                "})()"

        binding.webView.evaluateJavascript(javascript.toString()
        ) {
            null
        }
    }















}
fun splitStrings(inputList: List<String>, maxLength: Int = 30): List<String> {
    val resultList = mutableListOf<String>()

    for (originalString in inputList) {
        val words = originalString.split(" ")
        var currentChunk = words[0]

        for (word in words.drop(1)) {
            if (currentChunk.length + word.length + 1 <= maxLength) {
                currentChunk += " $word"
            } else {
                resultList.add(currentChunk)
                currentChunk = word
            }
        }

        if (currentChunk.isNotEmpty()) {
            resultList.add(currentChunk)
        }
    }

    return resultList
}






