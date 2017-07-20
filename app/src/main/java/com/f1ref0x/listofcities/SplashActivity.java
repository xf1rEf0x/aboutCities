package com.f1ref0x.listofcities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


public class SplashActivity extends AppCompatActivity {

    public static Context context;
    TextView textView;
    TextView reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        textView = (TextView) findViewById(R.id.loading);
        reload = (TextView) findViewById(R.id.reload);
        reload.setVisibility(View.INVISIBLE);


        final Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                    // Если программа запускается впервые - происходит загрузка данных в базу

                    if (pref.getBoolean("is_start", false) != true) {

                        WriteJSONDataInDB.dbHelper.deleteAll();

                        out:
                        do {
                            if (isNetwork()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setVisibility(View.VISIBLE);
                                        reload.setVisibility(View.INVISIBLE);
                                    }
                                });
                                Thread dbThread = new Thread(new WriteJSONDataInDB());
                                dbThread.start();
                                do {
                                    Thread.sleep(300);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText("Loading.");
                                        }
                                    });
                                    Thread.sleep(300);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText("Loading..");
                                        }
                                    });
                                    Thread.sleep(300);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText("Loading...");
                                        }
                                    });
                                    Thread.sleep(300);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText("Loading");
                                        }
                                    });
                                } while (dbThread.isAlive());
                                dbThread.join();

                                break out;
                            } else {
                                textView.setVisibility(View.INVISIBLE);
                                reload.setVisibility(View.VISIBLE);
                                Thread.sleep(1000);
                                continue out;
                            }
                        } while (true);
                        pref.edit().putBoolean("is_start", true).commit();
                    }
                } catch (Exception e) {
                    Log.i("Exepchik", e.getMessage());
                } finally {
                    Intent i = new Intent(SplashActivity.this,
                            MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }

    public boolean isNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null;
    }

 // Свернуть при нажатии кнопки "назад" , что бы не обовать процесс загрузки бд

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
