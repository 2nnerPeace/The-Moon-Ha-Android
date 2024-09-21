package com.innerpeace.themoonha.adapter.schedule

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.schedule.ScheduleWeeklyResponse
import com.innerpeace.themoonha.databinding.DialogLessonInfoBinding
import com.innerpeace.themoonha.ui.util.Colors
import com.innerpeace.themoonha.viewModel.LoungeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/**
 * 스케줄 viewHolder
 * @author 조희정
 * @since 2024.09.07
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.07  	조희정       최초 생성
 * 2024.09.07  	조희정       주간 스케줄 불러오기 구현
 * </pre>
 */
class ScheduleWeeklyViewHolder(view: View, private val fragment: Fragment, private val viewModel: LoungeViewModel) : RecyclerView.ViewHolder(view) {
    private val gridLayout: GridLayout = view.findViewById(R.id.gl_time_table)

    // 각 TextView에 해당하는 날짜를 할당
    private val tvDays: List<TextView> = listOf(
        view.findViewById(R.id.tv_1),
        view.findViewById(R.id.tv_2),
        view.findViewById(R.id.tv_3),
        view.findViewById(R.id.tv_4),
        view.findViewById(R.id.tv_5),
        view.findViewById(R.id.tv_6),
        view.findViewById(R.id.tv_7)
    )

    fun bind(scheduleWeeklyResponseList: List<ScheduleWeeklyResponse>, standardSunday: Calendar) {

        // 1. 수업이 없으면 17:00부터 아래까지 삭제
        if (scheduleWeeklyResponseList.isEmpty()) {
            removeCellsAfterLastClass(15, 0)
            return
        }

        // 날짜 포맷 설정 (일만 표시)
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        // standardSunday를 기준으로 일주일 간의 날짜 설정
        for (i in tvDays.indices) {
            // standardSunday에서 i일 더한 날짜를 계산
            val dayCalendar = standardSunday.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_YEAR, i)

            // TextView에 날짜 설정
            tvDays[i].text = dateFormat.format(dayCalendar.time)
        }

        Log.d("WeeklyHolder", "Fetched Schedule Data: $scheduleWeeklyResponseList")
        Log.d("WeeklyHolder", "Fetched Schedule Data: $standardSunday")

        var latestHour = 0
        var latestMinute = 0
        var earliestHour = 23
        var earliestMinute = 59

        // 스케줄 표 그리기
        for (schedule in scheduleWeeklyResponseList) {
            val day = schedule.day
            val beforeHour = schedule.startHour
            val beforeMinute = schedule.startMinute
            val afterHour = schedule.endHour
            val afterMinute = schedule.endMinute

            // 가장 빨리 시작하는 수업 시간 갱신
            if (beforeHour < earliestHour || (beforeHour == earliestHour && beforeMinute < earliestMinute)) {
                earliestHour = beforeHour
                earliestMinute = beforeMinute
            }


            // 가장 늦은 시간 갱신
            if (afterHour > latestHour || (afterHour == latestHour && afterMinute > latestMinute)) {
                latestHour = afterHour
                latestMinute = afterMinute
            }

            // 병합할 row 수 (강좌 지속 시간)
            val hourSubtract = (afterHour - beforeHour) * 6
            val minuteSubtract = (afterMinute - beforeMinute) / 10
            val result = hourSubtract + minuteSubtract

            // 병합 시작할 row
            val row = 1 + (beforeHour - 10) * 6 + (beforeMinute / 10)

            // 병합할 셀 제거
            removeMergedCells(day, beforeHour, beforeMinute, result, itemView)

            // 셀 병합, 수업 이름 적용
            mergeCells(schedule, day, beforeHour, beforeMinute, afterHour, afterMinute, result, row, schedule.title, itemView)

        }

        // 2. 가장 늦게 끝나는 수업이 17:00 이전이면 17:00부터 삭제
        if (latestHour < 15 || (latestHour == 15 && latestMinute == 0)) {
            removeCellsAfterLastClass(15, 0)
        }
        // 3. 가장 늦게 끝나는 수업이 17:00 이후에 끝난다면, 해당 수업 종료시간 + 1의 00분부터 삭제
        else {
            removeCellsAfterLastClass(latestHour + 1, 0)
        }

    }

    private fun removeCellsAfterLastClass(latestHour: Int, latestMinute: Int) {
        Log.d("확인", "latestHour: $latestHour, latestMinute: $latestMinute")

        // 요일 배열: 일요일부터 토요일까지
        val days =
            listOf("", "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")

        // 삭제할 시간 계산
        var hour = latestHour
        var minute = latestMinute

        while (hour < 20 || (hour == 19 && minute <= 50)) {
            for (day in days) {
                val deleteCellName =
                    day + String.format("%02d", hour) + String.format("%02d", minute)
                val deleteCell = gridLayout.findViewWithTag<TextView>(deleteCellName)

                if (deleteCell != null) {
                    gridLayout.removeView(deleteCell)
                }
            }

            // 10분 단위로 시간 증가
            if (minute == 50) {
                minute = 0
                hour += 1
            } else {
                minute += 10
            }
        }
    }


    // 병합된 셀 제거
    private fun removeMergedCells(
        day: String, beforeHour: Int, beforeMinute: Int, result: Int, view: View
    ) {
        var hour = beforeHour
        var minute = beforeMinute

        for (j in 0 until result - 1) {
            val deleteCellHour = if (minute == 50) {
                if (hour + 1 < 10) "0${hour + 1}" else "${hour + 1}"
            } else {
                if (hour < 10) "0$hour" else "$hour"
            }

            val deleteCellMinute = if (minute == 50) "00" else String.format("%02d", minute + 10)
            minute = if (minute == 50) 0 else minute + 10
            hour = if (minute == 0) hour + 1 else hour

            val deleteCellName = dayToEnglish(day) + deleteCellHour + deleteCellMinute
            val deleteCell = view.findViewWithTag<TextView>(deleteCellName)

            // 셀 삭제
            if (deleteCell != null) {
                gridLayout.removeView(deleteCell)
            }
        }
    }

    // 셀 병합
    private fun mergeCells(
        schedule: ScheduleWeeklyResponse, day: String, beforeHour: Int, beforeMinute: Int, afterHour: Int, afterMinute: Int, result: Int, row: Int, title: String, view: View
    ) {
        // 병합할 셀 정보
        val IDofSpanCell = dayToEnglish(day) + String.format("%02d", beforeHour) + String.format("%02d", beforeMinute)
        val spanCell = view.findViewWithTag<TextView>(IDofSpanCell)
        val layoutParams = spanCell.layoutParams as GridLayout.LayoutParams
        layoutParams.columnSpec = GridLayout.spec(dayToNum(day))
        layoutParams.rowSpec = GridLayout.spec(row, result)
        spanCell.layoutParams = layoutParams

        // 셀 병합
        layoutParams.setGravity(Gravity.FILL)
        spanCell.layoutParams = layoutParams

        // 셀 꾸미기
        // 들어갈 텍스트
        val timeString = String.format("%02d:%02d~%02d:%02d", beforeHour, beforeMinute, afterHour, afterMinute)
        val shortenedTitle = if (title.length > 20) title.substring(0, 17) + "..." else title
//        val fullText = "$shortenedTitle\n\n$timeString"
        val fullText = shortenedTitle

        // 텍스트 속성
        // 제목
        spanCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        spanCell.typeface = ResourcesCompat.getFont(itemView.context, R.font.happiness_sans_bold)


        // 시간
        val spannable = SpannableString(fullText)
        val timeStart = fullText.indexOf("\n") + 1
        spannable.setSpan(RelativeSizeSpan(0.8f), timeStart, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spanCell.text = spannable

        // 셀 속성
        val colorIndex = schedule.lessonId.toInt() % Colors.fixedColors.size
        val color = Colors.fixedColors[colorIndex]
        spanCell.setBackgroundColor(color)
        spanCell.setPadding(12, 12, 12, 12)

        // 강좌 정보 다이얼로그
        spanCell.setOnClickListener {
            showLessonDialog(listOf(schedule), fragment, viewModel)
        }

    }

    private fun dayToNum(day: String): Int {
        return when (day) {
            "일" -> 1
            "월" -> 2
            "화" -> 3
            "수" -> 4
            "목" -> 5
            "금" -> 6
            "토" -> 7
            else -> 0
        }
    }

    private fun dayToEnglish(day: String): String {
        return when (day) {
            "월" -> "monday"
            "화" -> "tuesday"
            "수" -> "wednesday"
            "목" -> "thursday"
            "금" -> "friday"
            "토" -> "saturday"
            "일" -> "sunday"
            else -> ""
        }
    }

    private fun showLessonDialog(scheduleList: List<ScheduleWeeklyResponse>, fragment: Fragment, viewModel: LoungeViewModel) {

        val bottomSheetDialog = BottomSheetDialog(itemView.context)
        val dialogBinding = DialogLessonInfoBinding.inflate(LayoutInflater.from(itemView.context))

        // ViewPager에 어댑터 설정
        val dialogAdapter = ScheduleWeeklyDialogAdapter(scheduleList, fragment, viewModel)
        dialogBinding.vpLessonOfDay.adapter = dialogAdapter

        // 날짜 포맷 설정
        val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val todayDate = Calendar.getInstance().time
        dialogBinding.tvSelectedDate.text = "${formatter.format(todayDate)}의 강좌"

        // BottomSheetDialog에 View 설정
        bottomSheetDialog.setContentView(dialogBinding.root)

        // 네비게이션 변경 시 다이얼로그 닫기
        val navController = fragment.findNavController()
        navController.addOnDestinationChangedListener { _, _, _ ->
            if (bottomSheetDialog.isShowing) {
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
    }

}