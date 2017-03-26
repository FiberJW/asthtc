package io.github.datwheat.asthtc;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.icu.text.Transliterator;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.Random;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {
    public static final String NOTI_ID = "NOTI_ID";
    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Transliterator t = Transliterator.getInstance("Halfwidth-Fullwidth");

        EditText editText = ((EditText) findViewById(R.id.boringEditText));
        final TextView betterTextView = (TextView) findViewById(R.id.betterTextView);
        ImageButton moreButton = (ImageButton) findViewById(R.id.imageButton);
        Button copyToClipboardButton = (Button) findViewById(R.id.button);


        RxTextView.textChangeEvents(editText)
                .subscribe(new Observer<TextViewTextChangeEvent>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "onSubscribe: Yee weouchea");
                    }

                    @Override
                    public void onNext(TextViewTextChangeEvent value) {
                        Log.i(TAG, "onNext: " + value.text().toString());
                        String betterText = t.transliterate(value.text().toString());
                        betterTextView.setText(betterText);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete: Yee wedone");
                    }
                });

        RxView.clicks(copyToClipboardButton).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "onSubscribe: Yee weouchea");
            }

            @Override
            public void onNext(Object obj) {
                String betterText = betterTextView.getText().toString();

                if (betterText.length() > 0) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);

                    ClipData clip = ClipData.newPlainText("asthtc txt", betterText);

                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(MainActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete: Yee wedone");
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);

                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.main_activity_actions, popup.getMenu());

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.new_notification:
                                return setNotification();
                            case R.id.remove_notification:
                                return removeNotification();
                            default:
                                return false;
                        }
                    }
                });
            }
        });

    }

    private boolean setNotification() {
        removeNotification();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        int newNotiId = (new Random()).nextInt();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_panorama_fish_eye)
                        .setContentTitle(getString(R.string.app_name))
                        .setOngoing(true)
                        .setContentText("Tap to generate ａｅｓｔｈｅｔｉｃ text!");

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(newNotiId, builder.build());

        settings.edit().putInt(NOTI_ID, newNotiId).apply();
        return true;
    }

    private boolean removeNotification() {
        int notiId = getNoticationId();
        if (notiId != 0) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notiId);

            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            settings.edit().putInt(NOTI_ID, 0).apply();
        }
        return true;
    }

    private int getNoticationId() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        return settings.getInt(NOTI_ID, 0);
    }

}
