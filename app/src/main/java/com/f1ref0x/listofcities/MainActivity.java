package com.f1ref0x.listofcities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    DBHelper dbHelper = new DBHelper(MainActivity.this);
    public static String LOG_TAG = "my_log";
    Spinner dropDown;
    TextView textView;
    String[] countries;
    ListView listView;
    public static String choosedConutry;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        dropDown = (Spinner) findViewById(R.id.spinner);
        listView = (ListView) findViewById(R.id.listView);
        listView.setFastScrollEnabled(true);
        db = dbHelper.getWritableDatabase();
        context = getApplicationContext();


        // Получения списка всех стран из БД и запись в DropDown
        Cursor cv = db.query("countries", new String[] {"Country"},null,null,"Country",null,null);
        if (cv.moveToFirst()) {

            int countryColumn = cv.getColumnIndex("Country");
            cv.moveToNext();
            countries = new String[247];
            int i = 0;
            do {
                countries[i] = cv.getString(countryColumn);
                //Log.i("newtag", cv.getString(cityColumn));
                i++;
            } while (cv.moveToNext());

        } else
            Log.i("newtag", "0 rows");
        cv.close();

        ArrayAdapter<String> dropAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, countries);
        dropDown.setAdapter(dropAdapter);

        // Обработик выбора страны из выпадающего списка и получение городов для записи в ListView

        dropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choosedConutry = (String) adapterView.getItemAtPosition(i);
                Log.i("item", (String) adapterView.getItemAtPosition(i));
                Cursor c = db.query("countries", new String[] {"cities"},"Country = " + "\'" +
                        adapterView.getItemAtPosition(i) + "\'",null,"cities",null,null);
                ArrayList<String> cities = new ArrayList<>();
                if (c.moveToFirst()) {
                    int cityColumn = c.getColumnIndex("cities");
                    do {
                        cities.add(c.getString(cityColumn));
                        if(c.isLast()) {
                            ArrayAdapter<String> viewAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,cities);
                            listView.setAdapter(viewAdapter);
                        }
                        Log.i(LOG_TAG, c.getString(cityColumn));
                    } while (c.moveToNext());

                } else
                    Log.i(LOG_TAG, "0 rows");
                c.close();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Обработчик нажатия на город в ListView

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isNetwork()) {
                    Thread thrd = new Thread(new FetchingInfo((String) adapterView.getItemAtPosition(i)));
                    thrd.start();
                    try {
                        thrd.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String tag = "frag1";

                    SimpleFrag frag = new SimpleFrag();
                    android.app.FragmentManager fragmentManager;
                    fragmentManager = getFragmentManager();
                    frag.show(fragmentManager, tag);
                } else
                    Toast.makeText(MainActivity.this,"Not Available Network",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null;
    }
}

