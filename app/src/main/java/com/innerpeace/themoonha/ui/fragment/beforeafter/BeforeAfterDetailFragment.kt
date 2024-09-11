package com.innerpeace.themoonha.ui.fragment.beforeafter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.innerpeace.themoonha.adapter.bite.BeforeAfterDetailAdapter
import com.innerpeace.themoonha.data.repository.BeforeAfterRepository
import com.innerpeace.themoonha.databinding.FragmentBeforeAfterDetailBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.BeforeAfterViewModel
import com.innerpeace.themoonha.viewModel.factory.BeforeAfterViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Before&After Detail 프래그먼트
 * @author 김진규
 * @since 2024.08.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.26  	김진규       최초 생성
 * </pre>
 */
class BeforeAfterDetailFragment : Fragment() {
    private var _binding: FragmentBeforeAfterDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BeforeAfterDetailAdapter
    private val viewModel: BeforeAfterViewModel by activityViewModels {
        BeforeAfterViewModelFactory(BeforeAfterRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBeforeAfterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.apply {
            hideToolbar()
            hideBottomNavigation()
        }

        val viewPager = binding.viewPager2
        val selectedPosition = arguments?.getInt("selectedPosition") ?: 0

        binding.backButton.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.getBeforeAfterDetails(selectedPosition)

        lifecycleScope.launch {
            viewModel.beforeAfterDetailResponse.collect { details ->
                adapter = BeforeAfterDetailAdapter(details)
                viewPager.adapter = adapter
                viewPager.setCurrentItem(selectedPosition, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

