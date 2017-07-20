package com.f1ref0x.listofcities;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import static com.f1ref0x.listofcities.MainActivity.LOG_TAG;




public class WriteJSONDataInDB implements Runnable  {

    SQLiteDatabase db;
    public static DBHelper dbHelper = new DBHelper(SplashActivity.context);
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";

    public String getJsonData() {

        try {
            URL url = new URL("https://raw.githubusercontent.com/David-Haim/CountriesToCitiesJSON/master/countriesToCities.json");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            resultJson = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    public void writeDataInDataBase() {
        Log.i(LOG_TAG, getJsonData());
        JSONObject dataJsonObj = null;
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            dataJsonObj = new JSONObject(getJsonData());
            Iterator<?> i = dataJsonObj.keys();

            while (i.hasNext()) {
                String objName = i.next().toString();
                try{
                    byte[] bytes = objName.getBytes("cp1252");
                    String str = new String(bytes, "UTF-8");
                    values.put("Country", str);
                }
                catch(UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                JSONArray jsonArray = dataJsonObj.getJSONArray(objName);
                int k = 0;
                do {
                    try{
                        byte[] bytes = jsonArray.getString(k).getBytes("cp1252");
                        String str = new String(bytes, "UTF-8");
                        values.put("Cities", str);
                    }
                    catch(UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                    db.insert("countries", null, values);
                    k++;
                }  while (k < jsonArray.length());
                //Log.i(LOG_TAG, objName + " = " + jsonArray.length());
                Log.i(LOG_TAG, "Iteration");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        getJsonData();
        writeDataInDataBase();
    }


}
