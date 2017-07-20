package com.f1ref0x.listofcities;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class FetchingInfo implements Runnable {


    public static String description;


    String _city;

    FetchingInfo(String city) {
        this._city = city.replaceAll(" ", "%20");
    }

    
    public String getInfoByReverse() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        try {
            URL url = new URL("http://api.geonames.org/findNearbyWikipediaJSON?username=f1ref0x&placename="+ _city);

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
    
    
    
    public String getInfoByFullTextSearch() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

            try {
                URL url = new URL("http://api.geonames.org/wikipediaSearchJSON?username=f1ref0x&q=" + _city);

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

        public void parseInfo() {
            Log.i("NEWTAG", getInfoByFullTextSearch());
            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(getInfoByFullTextSearch());
                JSONArray jsonArray = dataJsonObj.getJSONArray("geonames");
                if(jsonArray.length() == 0 || !getInfoByFullTextSearch().contains(MainActivity.choosedConutry)) {
                    dataJsonObj = new JSONObject(getInfoByReverse());
                    JSONArray jsonArray2 = dataJsonObj.getJSONArray("geonames");
                    //descripton = "Can't find information about this city";
                    out: for (int i = 0; i < jsonArray2.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray2.getString(i));
                        description = jsonObject.getString("summary");
                        if (description.contains(MainActivity.choosedConutry)) {
                            Log.i("click", "one");
                            break out;
                        } else {
                            description = "Not Information about city";
                            Log.i("click", "two");
                        }
                    }
                } else {
                    out: for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        description = jsonObject.getString("summary");
                        Log.i("click", description);
                        Log.i("click","Country: " + MainActivity.choosedConutry);
                        if (description.contains(MainActivity.choosedConutry)) {
                            Log.i("click","three");
                            break out;
                        } else {
                            description = (new JSONObject(jsonArray.getString(0)).getString("summary"));
                            Log.i("click", "for");
                        }
                            //description = "Can't find information about this city";
                    }
                }
            } catch (JSONException e) {
                Log.i("ex", e.getMessage());
            }
        }

    @Override
    public void run() {
        parseInfo();
    }
}
