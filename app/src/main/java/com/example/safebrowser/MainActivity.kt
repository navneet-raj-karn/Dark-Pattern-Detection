package com.example.safebrowser

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.safebrowser.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private val MODEL_ASSETS_PATH = "model.tflite"
    private val INPUT_MAXLEN = 50

    private var tfLiteInterpreter : Interpreter? = null

    lateinit var binding:ActivityMainBinding
    val classifier = Classifier( this , "word_dict.json" , INPUT_MAXLEN )
    companion object{
        var tabsList: ArrayList<Fragment> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabsList.add(HomeFragment())
        binding.myPager.adapter=TabsAdapter(supportFragmentManager, lifecycle)
        binding.myPager.isUserInputEnabled=false


        // Init TFLiteInterpreter
        tfLiteInterpreter = Interpreter( loadModelFile() )

        // Start vocab processing, show a ProgressDialog to the user.
        val progressDialog = ProgressDialog( this )
        progressDialog.setMessage( "Parsing word_dict.json ..." )
        progressDialog.setCancelable( false )
        progressDialog.show()
        classifier.processVocab( object: Classifier.VocabCallback {
            override fun onVocabProcessed() {
                // Processing done, dismiss the progressDialog.
                progressDialog.dismiss()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")

    override fun onBackPressed() {

        var frag:BrowseFragment?=null
        try {
            frag= tabsList[binding.myPager.currentItem] as BrowseFragment
        }catch (e:Exception){}

        when{
            frag?.binding?.webView?.canGoBack()==true->frag.binding.webView.goBack()
            binding.myPager.currentItem!=0->{
                tabsList.removeAt(binding.myPager.currentItem)
                binding.myPager.adapter?.notifyDataSetChanged()
                binding.myPager.currentItem= tabsList.size-1
            }

            else ->super.onBackPressed()
        }

    }



    private inner class TabsAdapter (fa: FragmentManager, lc: Lifecycle) : FragmentStateAdapter(fa ,lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeTab(url :String, fragment:Fragment){
        tabsList.add(fragment)
        binding.myPager.adapter?.notifyDataSetChanged()
        binding.myPager.currentItem= tabsList.size-1
    }

    fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    fun performPrediction(text: String) : String{
        // Init the classifier.
        if(text.isEmpty()) return " "
        if ( !TextUtils.isEmpty( text ) ){
            // Tokenize and pad the given input text.
            val tokenizedMessage = classifier.tokenize( text )
            val paddedMessage = classifier.padSequence( tokenizedMessage )

            val results = classifySequence( paddedMessage )
            // Assuming binary classification, use a threshold (e.g., 0.5) to decide the class
            val threshold = 0.8
            val predictedClass = if (results[0] > threshold) "Dark Pattern" else "Not a Dark Pattern"


            //binding.output.text = "Prediction: $predictedClass"
            return predictedClass
        }else{
            //Toast.makeText( this@MainActivity, "Please enter a message.", Toast.LENGTH_LONG).show();

        }
        return "Prediction Unsuccessful"


    }


    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {

        val assetFileDescriptor = assets.openFd(MODEL_ASSETS_PATH)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Perform inference, given the input sequence.
    private fun classifySequence(sequence: IntArray): FloatArray {
        try {
            // Input shape -> (1, INPUT_MAXLEN)
            val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
            // Output shape -> (1, 2) (as numClasses = 2)
            val outputs: Array<FloatArray> = arrayOf(FloatArray(1))
            tfLiteInterpreter?.run(inputs, outputs)
            return outputs[0]
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Rethrow the exception for further analysis
        }
    }

    // Function to save text to a file in internal storage
    fun saveTextToFile(text: String, fileName: String) {
        try {
            val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(text.toByteArray())
            outputStream.close()
            Toast.makeText(applicationContext, "Text saved to $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error saving text", Toast.LENGTH_SHORT).show()
        }
    }


}