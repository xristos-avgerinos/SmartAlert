package com.unipi.chrisavg.smartalert;

import static android.content.ContentValues.TAG;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMsend {
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    static OkHttpClient mClient = new OkHttpClient();
    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private  static final String SERVER_KEY = "AAAA-AbA9RA:APA91bFAvuvVrtKlgkSZ0YuclLMeBrdzH0jW2egC51SHbFPNKHpHi1_v0DU7HO5sZdqvy5UR6xGLkM_VH1mcyrXJ_HxYM6q4WrDd1QI_Vr6qm_BdWfmBGSGRrsKR8SwTgwODJiAqETyJ";
    static void sendMessage(final JSONArray recipients, final String title, final String body, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                try {
                    JSONObject root = new JSONObject();
                    /*JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", R.drawable.appicon);
                    notification.put("sound", defaultSoundUri);
                    notification.put("default_vibrate_timings", false);
                    notification.put("vibrate_timings", new long[] { 1000, 1000, 1000, 1000, 1000 });*/

                    JSONObject data = new JSONObject();
                    /*data.put("message", message);
                    root.put("notification", notification);*/

                    data.put("body", body);
                    data.put("title", title);
                    data.put("key_1", "XIONI GAMATA");

                    root.put("registration_ids", recipients);
                    root.put("data", data);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    //Toast.makeText(MainActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    static String postToFCM(String bodyString) throws IOException {
        RequestBody body = RequestBody.create(JSON,bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }
}

