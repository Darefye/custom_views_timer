package com.example.timer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.timer.clock.TimeState
import com.example.timer.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {


            btStart.setOnClickListener {
                btReset.isEnabled = false
                btStart.isVisible = false
                btStop.isVisible = true
                starView.start(0L)
            }


            btStop.setOnClickListener {
                btReset.isEnabled = true
                btStart.isVisible = true
                btStop.isVisible = false
                starView.stop()
            }


            btReset.setOnClickListener {
                starView.removeUpdateListener { }
                starView.reset()
            }


            starView.addUpdateListener { timeState ->
                setTime(timeState)
            }

            setTime(starView.currentTime())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    private fun setTime(timeState: TimeState) {
        val time = timeState.time
        binding.tvTime.text = String.format(
            "%02d:%02d:%02d",
            time / 1000 / 3600,
            time / 1000 / 60 % 60,
            time / 1000 % 60
        )
    }

    private fun setTime(timeLong: Long) {
        binding.tvTime.text = String.format(
            "%02d:%02d:%02d",
            timeLong / 3600,
            timeLong / 60 % 60,
            timeLong % 60
        )
    }

    override fun onDestroyView() {
        binding.starView.removeUpdateListener { }
        super.onDestroyView()
    }

}