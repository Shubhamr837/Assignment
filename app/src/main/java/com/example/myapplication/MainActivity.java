package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    private String videoLink = null ;
    private JSONObject jsonObject ;
    private String apiUrl = "http://15.207.150.183/API/index.php?p=videoTestAPI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniExoplayer();
    }

    private void iniExoplayer() {
        playerView = (PlayerView) findViewById(R.id.player);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(simpleExoPlayer);

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        simpleExoPlayer.release();
    }

    public void playVideo(View view) throws JSONException {
        if(!isNetworkConnected())
        {
            Toast.makeText(this,"No internet Connection",Toast.LENGTH_SHORT);
            return;
        }
        else {
            new JsonTask().execute(apiUrl);
        }


    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }  finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {

                jsonObject = new JSONObject(result);
                System.out.println("Response Json : " + jsonObject);
                int response_status = jsonObject.getInt("code");
                if(response_status!=200)
                    return;
                JSONObject video = (JSONObject) jsonObject.getJSONArray("msg").get(0);
                videoLink = video.getString("mp4Video");
                DataSource.Factory datasourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                        Util.getUserAgent(getApplicationContext(),"myapplication"));
                MediaSource mediaSource = new ExtractorMediaSource.Factory(datasourceFactory).createMediaSource(Uri.parse(videoLink));
                simpleExoPlayer.prepare(mediaSource);
                simpleExoPlayer.setPlayWhenReady(true);

            } catch (JSONException e) {
                e.printStackTrace();
                jsonObject = null;
            }

        }
    }
}
