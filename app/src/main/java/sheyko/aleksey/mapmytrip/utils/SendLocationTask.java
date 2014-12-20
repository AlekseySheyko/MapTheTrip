package sheyko.aleksey.mapmytrip.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class SendLocationTask extends AsyncTask<String, Void, Void> {

    public static final String TAG = SendLocationTask.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        String firstServerJsonResponse = null;
        String secondServerJsonResponse = null;

        // Request to first server
        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("wsapp.mapthetrip.com")
                    .appendPath("TrucFuelLog.svc")
                    .appendPath("TFLRecordTripCoordinates")
                    .appendQueryParameter("TripId", params[0])
                    .appendQueryParameter("Latitute", params[1])
                    .appendQueryParameter("Longitude", params[2])
                    .appendQueryParameter("CoordinatesRecordDateTime", URLDecoder.decode(params[3]))
                    .appendQueryParameter("CoordinatesRecordTimezone", "EST")
                    .appendQueryParameter("CoordinatesIdStatesRegions", "Test")
                    .appendQueryParameter("CoordinatesStateRegionCode", "T001")
                    .appendQueryParameter("CoordinatesCountry", "IN")
                    .appendQueryParameter("UserId", "1");
            String mUrlString = builder.build().toString();

            URL mUrl = new URL(mUrlString);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            firstServerJsonResponse = buffer.toString();
            Log.i(TAG, firstServerJsonResponse);
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        // Request to second server
        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("64.251.25.139")
                    .appendPath("trucks_app")
                    .appendPath("ws")
                    .appendPath("record-position.php")
                    .appendQueryParameter("lat", params[1])
                    .appendQueryParameter("lon", params[2])
                    .appendQueryParameter("id", params[0])
                    .appendQueryParameter("datetime", URLDecoder.decode(params[3]).replace(" ", "+"));

            String mUrlString = builder.build().toString();

            URL mUrl = new URL(mUrlString);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            secondServerJsonResponse = buffer.toString();
            Log.i(TAG, secondServerJsonResponse);
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }
}
