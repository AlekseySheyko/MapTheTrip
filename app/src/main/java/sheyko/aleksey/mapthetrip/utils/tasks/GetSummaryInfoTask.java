package sheyko.aleksey.mapthetrip.utils.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GetSummaryInfoTask extends AsyncTask<String, Void, HashMap<String, String>> {
    public static final String TAG = GetSummaryInfoTask.class.getSimpleName();

    protected OnStatesDataRetrieved mCallback;

    private String mStateCodes = "";
    private String mDistances = "";
    private String mStatesCount = "";

    // Interface to return states data
    public interface OnStatesDataRetrieved {
        public void onStatesDataRetrieved(String stateCodes, String stateDistances, String totalDistance, String statesCount);
    }

    public GetSummaryInfoTask(OnStatesDataRetrieved callback) {
        mCallback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        HashMap<String, String> statesData = new HashMap<>();

        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("64.251.25.139")
                    .appendPath("trucks_app")
                    .appendPath("ws")
                    .appendPath("get-distance.php")
                    .appendQueryParameter("truck_id", params[0]);
            String mUrlString = builder.build().toString();

            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
                    "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

                        URL mUrl = new URL(mUrlString);

                        // Create the request and open the connection
                        urlConnection = (HttpURLConnection) mUrl.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                        try {
                            JSONObject mResponseObject = new JSONObject(buffer.toString());
                            String mQueryStatus = mResponseObject.getJSONObject("status").getString("code");
                            if (mQueryStatus.equals("OK")) {
                                JSONObject mDataObject = mResponseObject.getJSONObject("data");
                                JSONObject mStateDistances = mDataObject.getJSONObject("distance");

                                Iterator<?> keys = mStateDistances.keys();

                                List<String> keyList = new ArrayList<>();

                                int statesCounter = 0;
                                while (keys.hasNext()) {
                                        String state = (String) keys.next();
                                        keyList.add(state);
                                        statesCounter++;

                                        if (mStateCodes.equals("")) {
                                            mStateCodes = mStateCodes + state;
                                        } else {
                                            mStateCodes = mStateCodes + ", " + state;
                                        }
                                }

                                for (int i = 0; i <= statesCounter; i++) {
                                    if (mStatesCount.equals("")) {
                                        mStatesCount = mStatesCount + "0";
                                    } else {
                                        mStatesCount = mStatesCount + ", " + "0";
                                    }
                                }

                                for (String key : keyList) {
                                    if (!key.equals("total")) {

                                        String distance = mStateDistances.getDouble(key) + "";

                                        if (mDistances.equals("")) {
                                            mDistances = mDistances + distance;
                                        } else {
                                            mDistances = mDistances + ", " + distance;
                                        }
                                    }
                                }
                                mStateCodes = mStateCodes.replace("total, ", "");
                                String totalDistance = mStateDistances.getDouble("total") + "";

                                statesData.put("stateCodes", mStateCodes);
                                statesData.put("stateDistances", mDistances);
                                statesData.put("totalDistance", totalDistance);
                                statesData.put("statesCount", mStatesCount);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }

                        Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
                                "Result: " + java.net.URLDecoder.decode(buffer.toString(), "UTF-8"));

        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
        } finally {
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
        return statesData;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> mStatesInfo) {
        super.onPostExecute(mStatesInfo);

        mCallback.onStatesDataRetrieved(
                mStatesInfo.get("stateCodes"),
                mStatesInfo.get("stateDistances"),
                mStatesInfo.get("totalDistance"),
                mStatesInfo.get("statesCount"));
    }
}