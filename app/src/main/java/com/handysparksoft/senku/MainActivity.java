package com.handysparksoft.senku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

/**
 * Mejoras:
 *      - Pantalla preferencias: vibrate on/off, color balls,
 *      - Barra de botones y pantalla completa
 *      - Icono con efecto 3d
 *      - Demo
 *      - Mas layouts: cruz, circulo, rombo...
 *      - Pulbish in Git
 *
 */
public class MainActivity extends ActionBarActivity implements View.OnFocusChangeListener {

    private AdView adView;

    static final int SIZE = 7;
    com.handysparksoft.senku.Game game;
    Timer timer;
    TextView txtTime;
    Vibrator vibrator;
    private TreeSet<String> scores;

    private final int ids[][] = {
            {0,0, R.id.f1,R.id.f2,R.id.f3,0,0},
            {0,0,R.id.f4,R.id.f5,R.id.f6,0,0},
            {R.id.f7,R.id.f8,R.id.f9,R.id.f10,R.id.f11,R.id.f12,R.id.f13},
            {R.id.f14,R.id.f15,R.id.f16,R.id.f17,R.id.f18,R.id.f19,R.id.f20},
            {R.id.f21,R.id.f22,R.id.f23,R.id.f24,R.id.f25,R.id.f26,R.id.f27},
            {0,0,R.id.f28,R.id.f29,R.id.f30,0,0},
            {0,0,R.id.f31,R.id.f32,R.id.f33,0,0},
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        game = new Game();
        registerListeners();
        setFigureFromGrid();

        txtTime = (TextView)findViewById(R.id.txtTime);
        startTimer();
        game.restartTimer();
        scores = getScores();

        //Publicidad
        adView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("8DBB3294399A59F966A4B5694A8563CF").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onPause() {
        timer = null;
        game.pauseTimer();
        adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.restartTimer();
        adView.resume();
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        startTimer();
        super.onRestart();
    }



    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String seconds = game.getTimeFormatted();
                        txtTime.setText(getString(R.string.time) +" "+ seconds);
                    }
                });
            }
        },1000,1000);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    //public void onClick(View v) {
        if (hasFocus) {
            RadioButton rdbSelected = ((RadioButton) v);
            int id = rdbSelected.getId();

            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (ids[i][j] == id) {
                        Boolean right = game.play(i, j);
                        setBackgroudColor(v);
                        if (right == true) {
                            vibrator.vibrate(25);
                        }
                        break;
                    }
                }
            }

            setFigureFromGrid();
            if (game.isGameFinished()) {
                Toast.makeText(this, getString(R.string.game_over), Toast.LENGTH_LONG).show();
                game.pauseTimer();
                updateScores();
            }
        }
    }

    private void updateScores() {
        Long maxScore = getMaxScore();
        if (game.getScore() > maxScore) {
            String msg = getResources().getString(R.string.record) + " ("+game.getScore()+")";
            Toast.makeText(this, msg , Toast.LENGTH_LONG).show();
        }
        addScore();
    }

    public Long getMaxScore() {
        Long result = 0L;
        scores = getScores();
        for (String score:scores) {
            if (Long.valueOf(score) > result) {
                result = Long.valueOf(score);
            }
        }

        return result ;
    }

    public TreeSet<String> getScores() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.scores = new TreeSet<String>(prefs.getStringSet("scores", new HashSet<String>()));
        return this.scores ;
    }

    public void addScore() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.scores = new TreeSet<String>(prefs.getStringSet("scores", new HashSet<String>()));
        if (!this.scores.contains(String.valueOf(game.getScore()))) {
            this.scores.add(String.format("%03d", game.getScore()));
            if (this.scores.size()> 10) {

                ArrayList<String> treeList = new ArrayList<String>(this.scores.descendingSet());
                this.scores = new TreeSet<String>(treeList.subList(0,9));
            }
            prefs.edit().putStringSet("scores", this.scores).commit();
        }

    }

    private void setBackgroudColor(View v) {
        RadioButton rdbSelected = ((RadioButton) v);

        for (int i=0;i<SIZE;i++) {
            for (int j = 0; j < SIZE; j++) {
                if (ids[i][j] != 0) {
                    RadioButton rdb = ((RadioButton) findViewById(ids[i][j]));
                    //rdb.setBackgroundColor(Color.TRANSPARENT);

                    AlphaAnimation anim = new AlphaAnimation(0.2f, 1.0f);
                    anim.setDuration(300);
                    anim.setFillAfter(true);
                    rdbSelected.startAnimation(anim);
                }
            }
        }
        //rdbSelected.setBackgroundColor(Color.parseColor("#6499cc33"));
    }

    private void registerListeners() {
        for (int i=0;i<SIZE;i++) {
            for (int j=0;j<SIZE;j++) {
                if (ids[i][j] != 0) {
                    RadioButton rdb = (RadioButton) findViewById(ids[i][j]);
                    //rdb.setOnClickListener(this);
                    rdb.setOnFocusChangeListener(this);
                }
            }
        }
    }

    private void setFigureFromGrid() {
        RadioButton rdb;
        for(int i=0;i<SIZE;i++) {
            for (int j=0;j<SIZE;j++) {
                if (ids[i][j] != 0) {
                    int value = game.getGrid(i, j);
                    rdb = (RadioButton) findViewById(ids[i][j]);

                    if (value == 1) {
                        rdb.setChecked(true);
                    }else {
                        rdb.setChecked(false);
                    }
                }
            }
        }

        ((TextView) findViewById(R.id.txtScore)).setText(getString(R.string.score) + " " +game.getScore());
    }

    private void restart() {
        game = new Game();
        setFigureFromGrid();
        ((RadioButton)findViewById(R.id.f17)).requestFocus();
    }

    private void random() {
        game = new Game();
        game.random();
        setFigureFromGrid();
        ((RadioButton)findViewById(R.id.f17)).clearFocus();
    }

    private void undo() {
        game.undo();
        setFigureFromGrid();
    }

    private void showScores() {
       Intent intent = new Intent(this, ScoreScreen.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            restart();
            return true;
        }
        if (id == R.id.action_scores) {
            showScores();
            return true;
        }

        if (id == R.id.action_random) {
            random();
            return true;
        }

        if (id == R.id.action_undo) {
            undo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
