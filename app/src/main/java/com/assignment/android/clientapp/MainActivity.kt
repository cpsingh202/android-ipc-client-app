package com.assignment.android.clientapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.assignment.android.clientapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), AppEventListener {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        binding.run {
            listener = this@MainActivity
        }
    }

    override fun onSubmit() {
        val input = binding.etInput.text
        Log.i(TAG, "Input: ${input}")

        if (input.isNullOrEmpty()) {
            binding.etInput.error = getString(R.string.error_invalid_input)
        } else {
            hideKeyboard()
        }
    }

    override fun showResult(result: String) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(result)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL,
            getString(R.string.btn_dialog_dismiss)
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE)
                as InputMethodManager

        var view = this.currentFocus
        if (view == null) {
            view = View(this)
        }

        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

interface AppEventListener {
    fun onSubmit()
    fun showResult(result: String)
}