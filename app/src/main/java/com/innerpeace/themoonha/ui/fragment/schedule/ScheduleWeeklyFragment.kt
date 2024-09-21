package com.innerpeace.themoonha.ui.fragment.schedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.innerpeace.themoonha.adapter.schedule.ScheduleWeeklyAdapter
import com.innerpeace.themoonha.data.model.schedule.ScheduleWeeklyResponse
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.LoungeService
import com.innerpeace.themoonha.data.network.ScheduleService
import com.innerpeace.themoonha.data.repository.LoungeRepository
import com.innerpeace.themoonha.data.repository.ScheduleRepository
import com.innerpeace.themoonha.databinding.FragmentScheduleWeeklyBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.LoungeViewModel
import com.innerpeace.themoonha.viewModel.ScheduleViewModel
import com.innerpeace.themoonha.viewModel.factory.LoungeViewModelFactory
import com.innerpeace.themoonha.viewModel.factory.ScheduleViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * 주간 스케줄 프래그먼트
 * @author 조희정
 * @since 2024.09.04
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.04  	조희정       최초 생성
 * 2024.09.07  	조희정       주간 스케줄 불러오기 기능 구현
 * </pre>
 */
class ScheduleWeeklyFragment : Fragment() {

    private var _binding: FragmentScheduleWeeklyBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ScheduleWeeklyAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private var standardSundayCalendarList = mutableListOf<Calendar>()
    private var standardSundayStringList = mutableListOf<String>()


    private val viewModel: ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(
            ScheduleRepository(
                ApiClient.getClient().create(ScheduleService::class.java)
            )
        )
    }
    private val loungeViewModel: LoungeViewModel by activityViewModels {
        LoungeViewModelFactory(LoungeRepository(ApiClient.getClient().create(LoungeService::class.java)))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleWeeklyBinding.inflate(inflater, container, false)
        val view = binding.root

        // 툴바 제목 변경
        (activity as? MainActivity)?.setToolbarTitle("스케줄")

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager 설정
        viewPager = binding.vpWeeklyTable

        // 드래그 이동 막기
        viewPager.getChildAt(0).setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_MOVE -> true
                else -> false
            }
        }

        // 1. 초기 데이터 리스트 설정
        standardSundayCalendarList = getWeeklyDates().toMutableList() // 초기 주차 데이터
        standardSundayStringList = standardSundayCalendarList.map { dateFormat.format(it.time) }.toMutableList() // Calendar를 String으로 변환

        // 2. 처음 데이터를 받아오기 위한 ViewModel 관찰
        viewModel.selectedStandardDate.observe(viewLifecycleOwner, Observer { standardDates ->
            standardDates?.let {
                // 데이터를 받아오는 API 호출
                viewModel.fetchScheduleWeeklyList(it)
            }
        })

        // 3. 받아온 데이터를 관찰하고 어댑터에 반영
        viewModel.scheduleWeeklyList.observe(viewLifecycleOwner, Observer { scheduleWeeklyResponseList ->
            scheduleWeeklyResponseList?.let {
                Log.d("Fragment", "Fetched Schedule Data: $it")
                // 어댑터 재설정
                adapter = ScheduleWeeklyAdapter(it, standardSundayCalendarList, this, loungeViewModel)
                viewPager.adapter = adapter
                // 기본적으로 중간 페이지 (2024-09-15) 보여주기
                viewPager.post {
                    viewPager.currentItem = 1
                    updateYearMonthDisplay(standardSundayCalendarList[1]) // 현재 중간 페이지 날짜로 year-month 업데이트
                }
            }
        })

        // 처음 데이터 요청
        viewModel.setSelectedStandardDate(standardSundayStringList) // ViewModel을 통해 날짜 설정

        // 4. nextWeekButton 클릭 시 처리
        binding.nextWeekButton.setOnClickListener {
            // 다음 주차로 리스트 업데이트
            updateWeeklyDates(forward = true)

            // 업데이트된 날짜 리스트를 ViewModel에 전달
            viewModel.setSelectedStandardDate(standardSundayStringList)
        }

        // 5. previousWeekButton 클릭 시 처리 (옵션)
        binding.previousWeekButton.setOnClickListener {
            // 이전 주차로 리스트 업데이트
            updateWeeklyDates(forward = false)

            // 업데이트된 날짜 리스트를 ViewModel에 전달
            viewModel.setSelectedStandardDate(standardSundayStringList)
        }
    }

    private fun getWeeklyDates(): List<Calendar> {
        val today = Calendar.getInstance()

        // 현재 주, 저번 주, 다음 주 일요일 구하기
        val currentSunday = getSunday(today)
        val lastSunday = getSunday((today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) })
        val nextSunday = getSunday((today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) })

        return listOf(lastSunday, currentSunday, nextSunday)
    }

    private fun getSunday(calendar: Calendar): Calendar {
        val clonedCalendar = calendar.clone() as Calendar
        val dayOfWeek = clonedCalendar.get(Calendar.DAY_OF_WEEK)
        val diff = Calendar.SUNDAY - dayOfWeek
        clonedCalendar.add(Calendar.DAY_OF_YEAR, diff)
        return clonedCalendar
    }

    private fun updateWeeklyDates(forward: Boolean) {
        // 주차 이동: forward가 true면 1주차 이동, false면 1주차 감소
        val offset = if (forward) 7 else -7

        // 각 Sunday 날짜를 업데이트 (Calendar 리스트 업데이트)
        for (i in standardSundayCalendarList.indices) {
            standardSundayCalendarList[i].add(Calendar.DAY_OF_YEAR, offset)
        }

        // Calendar 리스트를 기반으로 String 리스트 업데이트
        standardSundayStringList = standardSundayCalendarList.map { dateFormat.format(it.time) }.toMutableList()

        // 로그로 변경된 리스트 출력하여 확인
        Log.d("Updated Dates (Calenda", standardSundayCalendarList.toString())
        Log.d("Updated Dates (String)", standardSundayStringList.toString())
    }

    // 선택된 주의 년도와 월을 업데이트하는 함수
    private fun updateYearMonthDisplay(calendar: Calendar) {
        val yearMonth = yearMonthFormat.format(calendar.time)
        binding.tvYearMonth.text = yearMonth
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewPager = binding.vpWeeklyTable
//
//        // 드래그 이동 막기
//        viewPager.getChildAt(0).setOnTouchListener { _, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_MOVE -> true // Prevent swiping
//                else -> false
//            }
//        }
//
//        // 최초 데이터 로드 (오늘 날짜 기준으로 일요일 계산)
//        val today = Calendar.getInstance()
//
//        // 초기 일요일 날짜 리스트 설정
//        val initialSundayDates = getSundayDates(today)
//        updateSundayDates(initialSundayDates)
//
//        // 기준일 변경
//        viewModel.selectedStandardDate.observe(viewLifecycleOwner, Observer { standardDates ->
//            standardDates?.let {
//                viewModel.fetchScheduleWeeklyList(it)
//            }
//            val yearMonth = standardDates[1]
//            binding.tvYearMonth.text = "${yearMonth.substring(0, 4)}년 ${yearMonth.substring(5, 7)}월"
//        })
//
//        // 주간 스케줄 목록 변경
//        viewModel.scheduleWeeklyList.observe(viewLifecycleOwner, Observer { scheduleList ->
//            scheduleList?.let {
//                adapter = ScheduleWeeklyAdapter(it, initialSundayDates, this, loungeViewModel)
//                viewPager.adapter = adapter
//                viewPager.setCurrentItem(1, false)
//            }
//        })
//
//
//        // ViewPager 위치에 따라 데이터 재로딩
//        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//
//                viewModel.selectedStandardDate.value?.let { standardDates ->
//                    when (position) {
//                        0 -> {
//                            val previousSunday = Calendar.getInstance().apply {
//                                time = dateFormat.parse(standardDates[0])!!
//                            }
//                            val updatedSundayDates = getSundayDates(previousSunday)
//                            updateSundayDates(updatedSundayDates)
//                            viewPager.setCurrentItem(1, false)
//                        }
//
//                        2 -> {
//                            val nextSunday = Calendar.getInstance().apply {
//                                time = dateFormat.parse(standardDates[2])!!
//                            }
//                            val updatedSundayDates = getSundayDates(nextSunday)
//                            updateSundayDates(updatedSundayDates)
//                            viewPager.setCurrentItem(1, false)
//                        }
//                    }
//                }
//            }
//        })
//
//        // 전주, 다음주 이동
//        binding.previousWeekButton.setOnClickListener {
//            // 이전 페이지로 이동
//            val currentItem = viewPager.currentItem
//            if (currentItem > 0) {
//                viewPager.setCurrentItem(currentItem - 1, true)
//            }
//        }
//
//        binding.nextWeekButton.setOnClickListener {
//            // 다음 페이지로 이동
//            val currentItem = viewPager.currentItem
//            if (currentItem < adapter.itemCount - 1) {
//                viewPager.setCurrentItem(currentItem + 1, true)
//            }
//        }
//    }
//
//    // 일요일 날짜 리스트
//    private fun updateSundayDates(sundayDates: List<Calendar>) {
//        val sundayDateStrings = sundayDates.map { dateFormat.format(it.time) }
//
//        Log.d("SundayDatesStrings", sundayDateStrings.toString())
//        Log.d("SundayDatesCalendars", sundayDates.toString())
//
//        viewModel.setSelectedStandardDates(sundayDateStrings)
//    }
//
//    // 기준이 되는 일요일 구하기
//    fun getSundayDates(standardDay: Calendar): List<Calendar> {
//        // 이번 주 일요일
//        val currentSunday = standardDay.clone() as Calendar
//        val dayOfWeek = currentSunday.get(Calendar.DAY_OF_WEEK)
//        val offset = (dayOfWeek - Calendar.SUNDAY + 7) % 7
//        currentSunday.add(Calendar.DAY_OF_YEAR, -offset)
//
//        // 1주일 전 일요일
//        val oneWeekBeforeSunday = currentSunday.clone() as Calendar
//        oneWeekBeforeSunday.add(Calendar.WEEK_OF_YEAR, -1)
//
//        // 1주일 후 일요일
//        val nextSunday = currentSunday.clone() as Calendar
//        nextSunday.add(Calendar.WEEK_OF_YEAR, 1)
//
//        return listOf(oneWeekBeforeSunday, currentSunday, nextSunday)
//    }
}