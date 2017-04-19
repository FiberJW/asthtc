package io.github.datwheat.asthtc

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.widget.Toast
import com.ibm.icu.text.Transliterator
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val t = Transliterator.getInstance("Halfwidth-Fullwidth")

        RxTextView.textChangeEvents(boringEditText)
                .subscribe(object : Observer<TextViewTextChangeEvent> {
                    override fun onSubscribe(d: Disposable) {
                        Log.i(TAG, "onSubscribe: Yee weouchea")
                    }

                    override fun onNext(value: TextViewTextChangeEvent) {
                        Log.i(TAG, "onNext: " + value.text().toString())
                        val betterText = t.transliterate(value.text().toString())
                        betterTextView.text = betterText
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: " + e.toString())
                    }

                    override fun onComplete() {
                        Log.i(TAG, "onComplete: Yee wedone")
                    }
                })

        RxView.clicks(copyToClipboardButton).subscribe(object : Observer<Any> {
            override fun onSubscribe(d: Disposable) {
                Log.i(TAG, "onSubscribe: Yee weouchea")
            }

            override fun onNext(obj: Any) {
                val betterText = betterTextView.text.toString()

                if (betterText.isNotEmpty()) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    clipboard.primaryClip = ClipData.newPlainText("asthtc txt", betterText)

                    Toast.makeText(this@MainActivity, "Text Copied", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.toString())
            }

            override fun onComplete() {
                Log.i(TAG, "onComplete: Yee wedone")
            }
        })

        moreButton.setOnClickListener { view ->
            val popup = PopupMenu(this@MainActivity, view)

            val inflater = popup.menuInflater
            inflater.inflate(R.menu.main_activity_actions, popup.menu)

            popup.show()

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.new_notification -> setNotification()
                    R.id.remove_notification -> removeNotification()
                    else -> false
                }
            }
        }
    }

    private fun setNotification(): Boolean {
        removeNotification()
        val settings = getPreferences(Context.MODE_PRIVATE)
        val newNotiId = Random().nextInt()

        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_panorama_fish_eye)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setColor(ContextCompat.getColor(this, R.color.majorelle_blue))
                .setContentText("Tap to generate ａｅｓｔｈｅｔｉｃ text!")

        val resultIntent = Intent(this, MainActivity::class.java)

        val resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder.setContentIntent(resultPendingIntent)

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(newNotiId, builder.build())

        settings.edit().putInt(NOTI_ID, newNotiId).apply()
        return true
    }

    private fun removeNotification(): Boolean {
        val notiId = noticationId
        if (notiId != 0) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notiId)

            val settings = getPreferences(Context.MODE_PRIVATE)
            settings.edit().putInt(NOTI_ID, 0).apply()
        }
        return true
    }

    private val noticationId: Int
        get() {
            val settings = getPreferences(Context.MODE_PRIVATE)
            return settings.getInt(NOTI_ID, 0)
        }

    companion object {
        val NOTI_ID = "NOTI_ID"
    }

}
