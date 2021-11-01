package com.jaewon.bookindex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jaewon.bookindex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val list = listOf(FragmentA(),FragmentB(),FragmentC(),FragmentD())
        val pagerAdapter = FragmentPagerAdapter(list,this)
        binding.viewPager.adapter = pagerAdapter
        val titles = listOf("책 대출","배달 현황","반납","장바구니")
        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab, position ->
            tab.text = titles.get(position)
        }.attach()
    }
}

class FragmentPagerAdapter(val fragmentList:List<Fragment>,fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = fragmentList.size
    override fun createFragment(position: Int) = fragmentList.get(position)
}