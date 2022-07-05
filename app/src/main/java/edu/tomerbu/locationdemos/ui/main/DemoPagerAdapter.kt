package edu.tomerbu.locationdemos.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import javax.inject.Inject
private const val TAG = "edu.tomerbu"
class DemoPagerAdapter @Inject constructor(fa: FragmentActivity) :

    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = PlaceholderFragment.newInstance(position + 1)
        Log.d(TAG, "fragment returned: $position")
        return fragment
    }
}

/**
 *     override fun getItem(position: Int): Fragment {
// getItem is called to instantiate the fragment for the given page.
// Return a PlaceholderFragment (defined as a static inner class below).
return PlaceholderFragment.newInstance(position + 1)
}

override fun getPageTitle(position: Int): CharSequence? {
return context.resources.getString(TAB_TITLES[position])
}
 */