package com.example.safebrowser

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.safebrowser.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

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
}