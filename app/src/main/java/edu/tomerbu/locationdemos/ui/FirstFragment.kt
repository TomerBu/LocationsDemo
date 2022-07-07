package edu.tomerbu.locationdemos.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import edu.tomerbu.locationdemos.*
import edu.tomerbu.locationdemos.databinding.FragmentFirstBinding
import edu.tomerbu.locationdemos.work.RefreshDataWorker
import kotlin.system.exitProcess


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

    private val launcher: ActivityResultLauncher<String> = requestPermissionLauncher {
        if (it) {
            Log.d(TAG, "onCreate: We have fine or coarse")
            doWorkThatRequiresPermission()
        } else {
            Log.d(TAG, "onCreate: No Permission")
            AlertDialog.Builder(requireContext()).setCancelable(false)
                .setNegativeButton("Bye") { d, w ->
                    Toast.makeText(requireContext(), "Bye", Toast.LENGTH_SHORT).show()
                    exitProcess(0)
                }
                .setPositiveButton("Settings") { d, w ->
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
                }.setTitle("Settings or quit").show()
        }
    }

    private fun doWorkThatRequiresPermission() {
        if (!hasLocationPermission()) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        //background must be requested separately
        if (!hasBackgroundPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                AlertDialog.Builder(requireContext()).setCancelable(false)
                    .setNegativeButton("Cancel") { _, _ -> }
                    .setPositiveButton("Background") { _, _ ->
                        launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }.setTitle("Please give us background permissions")
                    .setMessage("Settings -> allow all the time").show()

            } else {
                launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            return
        }
        Toast.makeText(requireContext(), "We have background", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFirst.setOnClickListener {
            doWorkThatRequiresPermission()
            //val request = OneTimeWorkRequestBuilder<DemoWorker>().build()
            //WorkManager.getInstance(requireContext()).enqueue(request)

            val request = OneTimeWorkRequestBuilder<RefreshDataWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(request)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}