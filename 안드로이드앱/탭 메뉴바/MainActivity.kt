package com.example.viewpagerfragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.viewpagerfragment.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //1. 페이지 데이터를 로드
        val list = listOf(FragmentA(), FragmentB(), FragmentC(), FragmentD())
        //2. 아답터를 생성
        val pagerAdapter = FragmentPagerAdapter(list, this)

        //3. 아답터와 뷰페이저 연결
        binding.viewPager.adapter = pagerAdapter

        //4. 탭 메뉴의 개수만큼 제목을 목록으로 생성
        val titles = listOf("책대출","배달 현황","반납","마이페이지")

        //5. 텝 레이아웃과 뷰 페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            tab.text = titles.get(position)
        }.attach()
    }
}

class FragmentPagerAdapter(val fragmentList:List<Fragment>, fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = fragmentList.size


    override fun createFragment(position: Int) = fragmentList.get(position)

}