package com.example.safebrowser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import com.example.safebrowser.databinding.FragmentHomeBinding


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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(result: String?): Boolean {
                (requireActivity() as MainActivity).changeTab(result!!, BrowseFragment(result))
                return true
            }


            override fun onQueryTextChange(newText: String?): Boolean = false

        })

    }
}

