package com.example.frewa814.livekrubb.asynctask;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A general getTask that will get something from the database and return a inputStream as a string.
 */
public class GetTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        InputStream inputStream;
        String result = null;

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(params[0]);
        HttpResponse response;

        try {
            response = httpClient.execute(httpget);
            inputStream = response.getEntity().getContent();
            result = convertInputStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
