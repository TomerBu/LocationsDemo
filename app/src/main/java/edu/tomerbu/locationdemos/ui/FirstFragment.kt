package edu.tomerbu.locationdemos.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import edu.tomerbu.locationdemos.R
import edu.tomerbu.locationdemos.databinding.FragmentFirstBinding
import edu.tomerbu.locationdemos.work.DemoWorker
import edu.tomerbu.locationdemos.work.RefreshDataWorker


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
//            val request = OneTimeWorkRequestBuilder<DemoWorker>().build()
//            WorkManager.getInstance(requireContext()).enqueue(request)

            val request = OneTimeWorkRequestBuilder<RefreshDataWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(request)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}