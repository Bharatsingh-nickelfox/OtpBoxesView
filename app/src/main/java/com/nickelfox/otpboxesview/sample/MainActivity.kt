package com.nickelfox.otpboxesview.sample

import `in`.bharat.otptextview.OTPListener
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bharat.otpboxesview.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            otpView.otpListener = object : OTPListener {
                override fun onInteractionListener(isComplete: Boolean) {
                    verifyOtpBtn.isEnabled = isComplete
                    if (isComplete)
                        hideKeyBoard()
                    else wrongOtpTv.visibility = View.GONE
                }
            }

            verifyOtpBtn.setOnClickListener {
                if (otpView.otp?.length == 6 && otpView.otp == "111111"){
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Otp verified")
                        .setPositiveButton(android.R.string.ok
                        ) { d, _ -> d.dismiss() }
                        .show()
                }else {
                    otpView.showError()
                    wrongOtpTv.visibility = View.VISIBLE
                    viewFlipper.displayedChild = 0
                }
            }
        }
    }

    fun hideKeyBoard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.verifyOtpBtn.windowToken, 0)
    }
}