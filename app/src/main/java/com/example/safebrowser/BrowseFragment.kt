package com.example.safebrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
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
import com.example.safebrowser.databinding.FragmentBrowseBinding


class BrowseFragment (private var urlNew:String) : Fragment() {


    lateinit var binding: FragmentBrowseBinding
    private var darkPatternFound = false

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
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainRef.binding.progressBar.visibility=View.GONE
                    extractAndPassText()
                }


            }
            webChromeClient=object: WebChromeClient(){
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    try {
                        mainRef.binding.webIcon.setImageBitmap(icon)

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
        binding.webView.evaluateJavascript("(function () {\n" +
                "  const walker = document.createTreeWalker(\n" +
                "    document.body,\n" +
                "    NodeFilter.SHOW_TEXT,\n" +
                "    null,\n" +
                "    false\n" +
                "  );\n" +
                "\n" +
                "  let node;\n" +
                "  const visibleText = [];\n" +
                "\n" +
                "  while ((node = walker.nextNode())) {\n" +
                "    const parentElement = node.parentElement;\n" +
                "\n" +
                "    // Check if the parent element is visible\n" +
                "    if (\n" +
                "      parentElement &&\n" +
                "      (parentElement.offsetWidth > 0 ||\n" +
                "        parentElement.offsetHeight > 0 ||\n" +
                "        (parentElement.getClientRects().length > 0 &&\n" +
                "          parentElement.getClientRects()[0].width > 0 &&\n" +
                "          parentElement.getClientRects()[0].height > 0))\n" +
                "    ) {\n" +
                "      visibleText.push(node.textContent.trim());\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  return visibleText.join(' ');\n" +
                "}\n)();")
        { html ->
            println("Printing Here Start");
            val chunks = html.chunkString(45)
            println("Printing Here Processed");
            println("Printing Here end");
            processText(chunks)
            //println(html)
            // code here
        }

    }

    private fun processText(chunks: List<String>) {
        val mainActivity = requireActivity() as MainActivity
        val maxChunkSize = 50
        var darkcnt=1;
        var nondark=1;

        for (item in chunks) {
            println("chunk is "+item)
            val result = mainActivity.performPrediction(item)
            if (result == "Dark Pattern") {
                darkPatternFound = true
               highlightDarkPattern(item)
                darkcnt++; // Toast.makeText(requireContext(), "$result for "+ item, Toast.LENGTH_LONG).show()
            }
            else
                nondark++;
        }
        //checking ratio but not perfect
        println(darkcnt*100/(nondark+darkcnt))
        println(nondark*100/(nondark+darkcnt))

        if(darkcnt*100/(nondark+darkcnt)>=5)
            Toast.makeText(requireContext(), "Dark Detected", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(requireContext(), "Clean Page", Toast.LENGTH_LONG).show()

        // Dark pattern already found, no need to search further



//        for (sentence in sentences) {
//            if (!darkPatternFound) { // Check if a dark pattern is already found
//                val potentialChunk = "$sentence".trim()
//
//                val result = mainActivity.performPrediction(potentialChunk)
//                if (result == "Dark Pattern") {
//                    darkPatternFound = true
//                    Toast.makeText(requireContext(), "Predicted = $result", Toast.LENGTH_LONG).show()
//                    break
//                }
//            } else {
//                break // Dark pattern already found, no need to search further
//            }
//        }


    }
    //chunking those scrapped text , bad Time complexity but okay
    fun String.chunkString(maxLength: Int): List<String> {
        val chunks = mutableListOf<String>()
        var startIndex = 0
        while (startIndex < this.length) {
            var endIndex = minOf(startIndex + maxLength, this.length)
            while (endIndex < this.length && this[endIndex - 1] !in listOf(' ', '.')) {
                endIndex--
            }
            chunks.add(this.substring(startIndex, endIndex).trim())
            startIndex = endIndex
            while (startIndex < this.length && this[startIndex] == ' ') {
                startIndex++
            }
        }

        return chunks
    }

    private fun highlightDarkPattern(chunk: String) {
        println("Navneet here: $chunk")
//        val javascript = """
//        var darkPatternElements = document.getElementsByTagName("body")[0].innerHTML;
//        var highlightedText = darkPatternElements.replace(new RegExp('(${"\\b" + chunk + "\\b"})', 'g'), '<span style="background-color: yellow;">$1</span>');
//
//        if (darkPatternElements !== highlightedText) {
//            document.getElementsByTagName("body")[0].innerHTML = highlightedText;
//        }
//    """.trimIndent()
        println("changing "+ chunk)
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

        binding.webView.evaluateJavascript(javascript, null)
    }








}
