package com.assignment.android.clientapp

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.assignment.android.clientapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), AppEventListener {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private var messenger: Messenger? = null //used to make an RPC invocation
    private var isServiceBounded = false
    lateinit var serviceConnection: ServiceConnection  //receives callbacks from bind and unbind invocations
    private var replyTo: Messenger? = null //invocation replies are processed by this Messenger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        binding.run {
            listener = this@MainActivity
        }

        this.serviceConnection = RemoteServiceConnection()
        this.replyTo = Messenger(RemoteResponseHandler())
    }

    override fun onStart() {
        super.onStart()

        //Bind to the remote service
        val intent = Intent()
        intent.setClassName(
            "com.assignment.android.remoteapp",
            "com.assignment.android.remoteapp.RemoteService"
        )
        this.bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        //Unbind if it is bound to the service
        if (this.isServiceBounded) {
            this.unbindService(serviceConnection)
            this.isServiceBounded = false
        }
    }


    override fun onSubmit() {
        val input = binding.etInput.text.trim().toString()

        if (input.isEmpty()) {
            showError(getString(R.string.error_invalid_input))
        } else {
            hideKeyboard()
            Toast.makeText(this, R.string.msg_wait, Toast.LENGTH_SHORT).show()

            if (this.isServiceBounded) {

                //Prepare the data object
                val data = Bundle()
                data.putString(getString(R.string.key_input), input)

                try {
                    val message = Message()

                    //Set the data
                    message.data = data

                    //Set the ReplyTo Messenger for processing the invocation response
                    message.replyTo = this.replyTo

                    //Make the invocation
                    this.messenger?.send(message)

                } catch (exc: RemoteException) {
                    exc.printStackTrace()
                    showError(getString(R.string.error_invocation_failed))
                }
            } else {
                showError(getString(R.string.error_service_not_bound))
            }
        }
    }

    override fun showResult(result: String) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage("Reply:  $result")
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            getString(R.string.btn_dialog_dismiss)
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    override fun showError(error: String) {
        binding.etInput.error = error
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


    private inner class RemoteServiceConnection : ServiceConnection {
        override fun onServiceConnected(component: ComponentName, binder: IBinder) {
            this@MainActivity.messenger = Messenger(binder)
            this@MainActivity.isServiceBounded = true
        }

        override fun onServiceDisconnected(component: ComponentName) {
            this@MainActivity.messenger = null
            this@MainActivity.isServiceBounded = false
        }
    }

    private inner class RemoteResponseHandler : Handler() {

        override fun handleMessage(message: Message) {
            val result = message.data
            showResult(result.getString(getString(R.string.key_result)).orEmpty())
        }
    }
}

interface AppEventListener {
    fun onSubmit()
    fun showResult(result: String)
    fun showError(error: String)
}