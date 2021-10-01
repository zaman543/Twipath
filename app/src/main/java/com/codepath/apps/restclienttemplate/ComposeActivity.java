package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    public static final int MAX_TWEET_LENGTH = 280;

    EditText etCompose;
    Button btnTweet;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        client = TwitterApp.getRestClient(ComposeActivity.this);

        //set click listener on button
        btnTweet.setOnClickListener(view -> {
            String tweetContent = etCompose.getText().toString();

            if(tweetContent.isEmpty()){
                Toast.makeText(ComposeActivity.this, "Sorry, tweet can't be empty!", Toast.LENGTH_SHORT).show();
                return;
            } if (tweetContent.length() > MAX_TWEET_LENGTH){
                Toast.makeText(ComposeActivity.this, "Sorry, tweet too long!", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
            //make api call to twitter to publish tweet
            //json response contains the tweet object
            client.postTweet(tweetContent, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Toast.makeText(ComposeActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                    Log.i("ComposeActivity", "onSuccess: published tweet");
                    try {
                        Tweet tweet = Tweet.fromJson(json.jsonObject);
                        Log.i("ComposeActivity", "onSuccess: published tweet says:" + tweet);
                        Intent intent = new Intent();
                        intent.putExtra("tweet", Parcels.wrap(tweet));
                        setResult(20, intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ComposeActivity", "Posted tweet but failed to receive a JSON object response", e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Toast.makeText(ComposeActivity.this, "Failed, try again.", Toast.LENGTH_SHORT).show();
                    Log.e("ComposeActivity", "onFailure: Failed to send tweet", throwable);
                }
            });
        });


    }
}

