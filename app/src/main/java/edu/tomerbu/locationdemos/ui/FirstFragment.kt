package edu.tomerbu.locationdemos.ui

import android.Manifest
import android.R.attr.data
import android.R.attr.min
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import edu.tomerbu.locationdemos.*
import edu.tomerbu.locationdemos.alarms.TaskNotificationScheduler
import edu.tomerbu.locationdemos.alarms.getAlarmManager
import edu.tomerbu.locationdemos.databinding.FragmentFirstBinding
import edu.tomerbu.locationdemos.work.DemoWorker
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
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


        val workManager = WorkManager.getInstance(requireContext())

        //constraints:
        val constraint: Constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        //PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS// 15 minutes.
        //request:
        val request = PeriodicWorkRequestBuilder<DemoWorker>(
            16, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).setInitialDelay /*+- 5*/(6, TimeUnit.SECONDS)
            .setInputData(Data.Builder().putInt(DemoWorker.TIMES, 8888).build())
            .setConstraints(constraint).build()

        //start:
        workManager.enqueueUniquePeriodicWork(
            DemoWorker.UNIQUE_ID,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        //Observe the status::
        workManager.getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner) {
            binding.textviewFirst.text = when (it.state) {
                WorkInfo.State.SUCCEEDED -> {
                    it.outputData.getString(DemoWorker.RESULT_DEMO_WORKER)?.let { data ->
                        "Success $data"
                    } ?: "Success With No Output"
                }
                WorkInfo.State.ENQUEUED -> "Enqueued"
                WorkInfo.State.BLOCKED -> "Blocked"
                WorkInfo.State.CANCELLED -> "Canceled"
                WorkInfo.State.FAILED -> "Failed"
                WorkInfo.State.RUNNING -> "Running"
            }
        }
    }

    @Inject
    lateinit var notificationScheduler: TaskNotificationScheduler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFirst.setOnClickListener {

            val alarmManager: AlarmManager? = requireContext().getAlarmManager()
            val hasPermission: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager?.canScheduleExactAlarms() ?: false
            } else {
                true
            }

            if(!hasPermission){
                Toast.makeText(requireContext(), "No Permission, request it", Toast.LENGTH_SHORT).show()
                @SuppressLint("InlinedApi") //if no permission it must be api 31+
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    Log.d(TAG, "$hourOfDay, $minute ")
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    notificationScheduler.scheduleTaskAlarm(3, calendar.timeInMillis)
                    Toast.makeText(requireContext(), "Alarm set", Toast.LENGTH_SHORT).show()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
