package com.innerpeace.themoonha.ui.fragment.lounge

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.adapter.lounge.LoungeHomeNoticeViewAdapter
import com.innerpeace.themoonha.viewModel.SharedViewModel
import com.innerpeace.themoonha.data.model.lounge.LoungeHomeResponse
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.LoungeService
import com.innerpeace.themoonha.data.repository.LoungeRepository
import com.innerpeace.themoonha.databinding.FragmentLoungeHomeBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.LoungeViewModel
import com.innerpeace.themoonha.viewModel.factory.LoungeViewModelFactory

/**
 * 라운지 개별 홈 프래그먼트
 * @author 조희정
 * @since 2024.08.23
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.24  	조희정       최초 생성
 * 2024.08.30   조희정       대문 이미지, 공지사항 Recycler View 구현
 * </pre>
 */
class LoungeHomeFragment : Fragment() {

    private var _binding: FragmentLoungeHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var adapter: LoungeHomeNoticeViewAdapter
    private val viewModel: LoungeViewModel by activityViewModels {
        LoungeViewModelFactory(LoungeRepository(ApiClient.getClient().create(LoungeService::class.java)))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoungeHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // 탭 설정
        binding.tab.tabRippleColor = null
        setHasOptionsMenu(true)
        setTabLayout()

        // 네비게이션바
        (activity as? MainActivity)?.hideNavigationBar()
        (activity as? MainActivity)?.showToolbar()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 스크롤 설정
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        binding.mainScrollView.run {
            header = binding.tab
            stickListener = {
                sharedViewModel.setScrollEnabled(true) // 스크롤 활성화
            }
            freeListener = {
                sharedViewModel.setScrollEnabled(false) // 스크롤 비활성화
            }
        }

        // FCM으로 전달된 id가 있을 경우 지정
        val fcmLoungeId = arguments?.getLong("loungeId", -1)

        if (fcmLoungeId != null && fcmLoungeId != -1L) {
            viewModel.setSelectedLoungeId(fcmLoungeId)
        }

        viewModel.selectedLoungeId.observe(viewLifecycleOwner, Observer { loungeId ->
            if (loungeId != null) {
                viewModel.fetchLoungeHome(loungeId)
            }
        })

        viewModel.loungeHome.observe(viewLifecycleOwner, Observer { home ->
            if (home != null) {
                setupMainImages(home.loungeInfo)
                setupNoticeRecyclerView(home.loungeNoticeList)
            }
        })
    }

    // 툴바 메뉴 변경
    override fun onPrepareOptionsMenu(menu: Menu) {
        val item1 = menu.findItem(R.id.item1)
        val icon1 = ContextCompat.getDrawable(requireContext(), R.drawable.ic_write)
        item1.icon = icon1

        val item2 = menu.findItem(R.id.item2)
        val icon2 = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search2)
        item2.icon = icon2
    }
    // 툴바 메뉴 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item1 -> {
                findNavController().navigate(R.id.action_loungeHomeFragment_to_loungePostWriteFragment)
                true
            }
            R.id.item2 -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 툴바, 대문 이미지 설정
    private fun setupMainImages(item: LoungeHomeResponse.LoungeInfo) {
        // 툴바 제목 변경
        (activity as? MainActivity)?.setToolbarTitle(item.title)

        // 이미지 설정
        Glide.with(binding.ivLoungeImage.context)
            .load(item.loungeImgUrl)
            .into(binding.ivLoungeImage)
    }

    // 공지사항 설정
    private fun setupNoticeRecyclerView(item: List<LoungeHomeResponse.LoungeNoticePost>) {
        adapter = LoungeHomeNoticeViewAdapter{ loungeItem ->
            navigateToDetailFragment(loungeItem)
        }
        binding.vpLoungeNotice.adapter = adapter

        // 데이터 설정
        adapter.setItems(item)

        // 인디케이터와 연결
        binding.vpHomeDotsIndicator.setViewPager2(binding.vpLoungeNotice)
    }

    // 탭 설정
    private fun setTabLayout() {
        replaceFragment(LoungeHomeLoungeTabFragment())

        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(LoungeHomeLoungeTabFragment())
                    1 -> replaceFragment(LoungeHomeInfoTabFragment())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.tab.getTabAt(0)?.select()
    }

    // 탭 페이지 이동
    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.tab_layout_fragment, fragment)
            .commit()
    }

    // 게시물 상세 페이지로 이동
    private fun navigateToDetailFragment(item: LoungeHomeResponse.LoungeNoticePost) {
        viewModel.setSelectedLoungePostId(item.loungePostId)
        findNavController().navigate(R.id.action_loungeHomeFragment_to_loungePostFragment)
    }

    override fun onResume() {
        super.onResume()

        replaceFragment(LoungeHomeLoungeTabFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}