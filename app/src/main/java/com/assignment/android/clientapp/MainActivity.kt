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
        val input = binding.etInput.text
        Log.i(TAG, "Input: $input")

        if (input.isNullOrEmpty()) {
            binding.etInput.error = getString(R.string.error_invalid_input)
        } else {
            hideKeyboard()

            if (this.isServiceBounded) {

                //Setup the message for invocation
                val message = Message.obtain(null, 1, 0, 0)
                try {
                    //Set the ReplyTo Messenger for processing the invocation response
                    message.replyTo = this.replyTo

                    //Make the invocation
                    this.messenger?.send(message)
                } catch (exc: RemoteException) {
                    exc.printStackTrace()
                    Log.i(TAG, "Invocation Failed!!")
                }
            } else {
                Log.i(TAG, "Service is Not Bound!!")
            }
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

        override fun handleMessage(msg: Message) {
            val what = msg.what
            showResult("Remote Service replied - ($what)")
        }
    }
}

interface AppEventListener {
    fun onSubmit()
    fun showResult(result: String)
}