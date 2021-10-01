package com.codepath.apps.restclienttemplate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Headers;


//TWITTER,
//TWEET, RETWEET and the
//Twitter
//Bird logo are trademarks of
//Twitter
//Inc. or its affiliates.
public class TimelineActivity extends AppCompatActivity {
    public static final String TAG = "TimelineActivity";
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        swipeLayout = findViewById(R.id.swipeContainer);
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        swipeLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "onRefresh: fetching new data");
            populateHomeTimeline();

            Toast.makeText(getApplicationContext(), "Works!", Toast.LENGTH_LONG).show();

            new Handler().postDelayed(() -> swipeLayout.setRefreshing(false), 4000);
        });

        //find the recyclerview
        rvTweets = findViewById(R.id.rvTweets);

        //initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        //configure the recycler view: the layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        populateHomeTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //we want to inflate menu to add action bar if present
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.twitter_birdie_round);
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    //launch with contract: we provide input of intent, we expect an output of Tweet.
    ActivityResultContract<Intent, Tweet> contract = new ActivityResultContract<Intent, Tweet>() {
        //create the intent for startActivityForResult
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Intent input) {
            input = new Intent(context, ComposeActivity.class);
            return input;
        }

        //result from onActivityResult - tweet or JSON?
        @Override
        public Tweet parseResult(int resultCode, @Nullable Intent intent) {
            Tweet tweet = Objects.requireNonNull(intent).getParcelableExtra("tweet");
            Toast.makeText(TimelineActivity.this, tweet.body, Toast.LENGTH_SHORT).show();
            return tweet;
        }
    };

    //what to do when we have the response
    ActivityResultLauncher<Intent> launchComposeForResult = registerForActivityResult(contract, result -> {
        //modify data source, then update adapter
        tweets.add(0, result);
        adapter.notifyItemInserted(0);
        rvTweets.smoothScrollToPosition(0);
    });

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //compose icon selected (replace with android snackbar)
        Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
        Intent input = new Intent();
        launchComposeForResult.launch(contract.createIntent(this, input));
        //we also want data back from compose activity instead of just showing it
        return true;
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess! grabbed timeline items");
                JSONArray jsonArray = json.jsonArray;
                try{
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeLayout.setRefreshing(false);
                    Log.i(TAG, "onSuccess: updated timeline with data");
                }catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }
}