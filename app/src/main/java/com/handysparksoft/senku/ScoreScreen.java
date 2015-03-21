package com.handysparksoft.senku;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;


public class ScoreScreen extends ActionBarActivity {

    TreeSet<String> scores;

    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_screen);

        //Fill score lists
        fillListViewTopTen();
        fillListViewRanking((ArrayList)this.getIntent().getExtras().getSerializable("array_list"));


        ((Button)findViewById(R.id.btnBack)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Publicidad BANNER
        adView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Publicidad Intersticial
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id3_intersticial));
        // Create an ad request.
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        // Optionally populate the ad request builder.
        //adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        // Set an AdListener.
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //Load interstitial 60% times
                if (new Random().nextInt(10) > 6) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mInterstitialAd.show();
                        }
                    }, 700);
                }
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                //goToNextLevel();
            }
        });

        // Start loading the ad now so that it is ready by the time the user is ready to go to
        // the next level.
        mInterstitialAd.loadAd(adRequestBuilder.build());

    }

    private void fillListViewTopTen() {
        ListView listView = (ListView)findViewById(R.id.listView);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scores = new TreeSet<String>(prefs.getStringSet("scores", new HashSet<String>()));


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_layout,new ArrayList<>(scores.descendingSet()));
        listView.setAdapter(adapter);
    };

    public void fillListViewRanking(ArrayList<String> scores) {
        ListView listView = (ListView)findViewById(R.id.listView2);

        //new FireBaseManager().getUsersScoreFromFireBase();



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_layout,scores);
        listView.setAdapter(adapter);


        listView.setItemsCanFocus(true);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view.setBackgroundColor(Color.RED);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView.setItemChecked(2, true);

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_score_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
