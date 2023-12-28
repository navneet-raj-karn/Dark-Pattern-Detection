package com.example.safebrowser

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import com.example.safebrowser.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_home,container,false)
        binding=FragmentHomeBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()

        val mainActivityRef=requireActivity() as MainActivity
        mainActivityRef.binding.topSearchBar.text=SpannableStringBuilder("")
        binding.searchView.setQuery("",false)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(result: String?): Boolean {
                if(mainActivityRef.checkForInternet(requireContext())){
                    mainActivityRef.changeTab(result!!, BrowseFragment(result))
                }else{
                    Snackbar.make(binding.root,"Internet not Connected",3000).show()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        mainActivityRef.binding.goBtn.setOnClickListener {
            if(mainActivityRef.checkForInternet(requireContext())){
                mainActivityRef.changeTab(mainActivityRef.binding.topSearchBar.text.toString(),
                    BrowseFragment(mainActivityRef.binding.topSearchBar.text.toString()))
            }else{
                Snackbar.make(binding.root,"Internet not Connected",3000).show()
            }
        }



    }
}

