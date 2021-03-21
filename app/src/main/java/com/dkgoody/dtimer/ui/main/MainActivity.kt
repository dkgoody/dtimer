package com.dkgoody.dtimer.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dkgoody.dtimer.DTimerViewModel
import com.dkgoody.dtimer.R


class MainActivity : AppCompatActivity()
{

    lateinit var viewModel : DTimerViewModel
    lateinit var keep_menu : Menu

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel = viewModels<DTimerViewModel>().value
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        if (viewModel.autostart())
            menu.getItem(0).setChecked(true)

        if (viewModel.voicealert())
            menu.getItem(1).setChecked(true)

        if(viewModel.alarm())
            menu.getItem(2).setChecked(true)

        keep_menu = menu

        return true
    }




    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_autostart -> {
            item.setChecked(!item.isChecked())
            viewModel.set_autostart(item.isChecked())
            keep_menu.getItem(2).setEnabled(!item.isChecked())
            if (item.isChecked()) {
                keep_menu.getItem(2).setChecked(false)
                viewModel.set_alarm(false)
            }
            true
        }
        R.id.action_voicealert -> {
            item.setChecked(!item.isChecked())
            viewModel.set_voicealert(item.isChecked())
            true
        }
        R.id.action_alarm -> {
            item.setChecked(!item.isChecked())
            viewModel.set_alarm(item.isChecked())
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun msgShow(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        if (!isRecreating()) {
            viewModel.putToBackground(this)
            Log.i("DTimer", "BACKGROUND")
        }
    }

    override fun onResume() {
        if (!isRecreating()) {
            Log.i("DTimer", "FOREGROUND")
            viewModel.putToForeground(this)
        }
        super.onResume()
    }

    fun isRecreating(): Boolean {
        //consider pre honeycomb not recreating
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB &&
                isChangingConfigurations()
    }
}

