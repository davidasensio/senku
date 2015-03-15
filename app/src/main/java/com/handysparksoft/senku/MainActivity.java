package com.handysparksoft.senku;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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
    private boolean isSolution = false;

    private final int ids[][] = {
            {0,0, R.id.f1,R.id.f2,R.id.f3,0,0},
            {0,0,R.id.f4,R.id.f5,R.id.f6,0,0},
            {R.id.f7,R.id.f8,R.id.f9,R.id.f10,R.id.f11,R.id.f12,R.id.f13},
            {R.id.f14,R.id.f15,R.id.f16,R.id.f17,R.id.f18,R.id.f19,R.id.f20},
            {R.id.f21,R.id.f22,R.id.f23,R.id.f24,R.id.f25,R.id.f26,R.id.f27},
            {0,0,R.id.f28,R.id.f29,R.id.f30,0,0},
            {0,0,R.id.f31,R.id.f32,R.id.f33,0,0},
    };

    private String solution[]={"f29-f17","f26-f24","f33-f25","f31-f33","f18-f30","f33-f25","f6-f18","f13-f11","f27-f13"
            ,"f10-f12","f13-f11","f8-f10","f1-f9","f3-f1","f16-f4","f1-f9","f28-f16","f21-f23","f7-f21","f24-f22"
            ,"f21-f23","f10-f8","f8-f22","f22-f24","f24-f26","f26-f12","f12-f10","f17-f15","f5-f17","f18-f16","f15-f17"};

    private FireBaseManager fireBaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isSolution = false;
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        game = new Game();
        registerListeners();
        setFigureFromGrid();

        txtTime = (TextView)findViewById(R.id.txtTime);
        startTimer();
        game.restartTimer();
        scores = getScores();

        fireBaseManager = new FireBaseManager();
        fireBaseManager.initFireBase(this);

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
        isSolution = false;
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
        if (!isSolution) {
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
        if (!isSolution) {
            Long maxScore = getMaxScore();
            if (game.getScore() > maxScore) {
                String msg = getResources().getString(R.string.record) + " (" + game.getScore() + ")";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
            addScore();
        }
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
            this.scores.add(String.format("%04d", game.getScore()));
            if (this.scores.size() > 10) {

                ArrayList<String> treeList = new ArrayList<String>(this.scores.descendingSet());
                this.scores = new TreeSet<String>(treeList.subList(0,9));
            }
            prefs.edit().putStringSet("scores", this.scores).commit();

            String maxScore = this.scores.last();
            fireBaseManager.storeUserScoreInFireBase(getFormattedUserAccount(), maxScore);
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

                    rdb.setOnFocusChangeListener(this);
                    rdb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setFigureFromGrid();
                        }
                    });
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
        isSolution = false;
        game = new Game();
        setFigureFromGrid();
        ((RadioButton)findViewById(R.id.f17)).requestFocus();
    }

    private void random() {
        isSolution = false;
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
       //Intent intent = new Intent(this, ScoreScreen.class);
       // startActivity(intent);
        fireBaseManager.getUsersScoreFromFireBase(this);
    }

    private String getUserAccount() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String gmail = null;

        for(Account account: list){
            if(account.type.equalsIgnoreCase("com.google")) {
                gmail = account.name;
                break;
            }
        }
        //showSortMsg(gmail);
        return gmail;
    }

    private String getFormattedUserAccount() {
        String result = getUserAccount();
        result = result.split("@")[0];
        result = result.replaceAll("\\.", "_").replaceAll("\\#","_").replaceAll("\\$", "_").replaceAll("\\[", "(").replaceAll("\\]", ")");
        return result;
    }



    private void playMove(String move) {
        int i1=-1,j1=-1,i2=-1,j2=-1;
        String view1 = move.split("-")[0];
        String view2 = move.split("-")[1];

        for(int i=0;i<SIZE;i++) {
            for (int j = 0; j < SIZE; j++) {
                if (ids[i][j] != 0) {
                    if (ids[i][j] == getResources().getIdentifier(view1,"id",this.getPackageName())) {
                        i1 = i;
                        j1 = j;
                        //((RadioButton)findViewById(getResources().getIdentifier(view1,"id",this.getPackageName()))).setSelected(true);

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        for(int i=0;i<SIZE;i++) {
            for (int j = 0; j < SIZE; j++) {
                if (ids[i][j] != 0) {
                    if (ids[i][j] == getResources().getIdentifier(view2,"id",this.getPackageName())) {
                        i2 = i;
                        j2 = j;
                    }
                }
            }
        }

        game.play(i1,j1);
        game.play(i2,j2);
    }

    private void resolve() {
        restart();
        final LinkedList<String> llSolution= new LinkedList<>(Arrays.asList(solution));

        Timer timerResolve = new Timer();
        timerResolve.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String nextMove = llSolution.pollFirst();
                            if (nextMove != null) {
                                playMove(nextMove);
                                setFigureFromGrid();
                            }else {
                                timer = null;
                            }
                        }
                    });
                }
            },750,750);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showSortMsg(String msg) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
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

        if (id == R.id.action_resolve) {
            isSolution = true;
            resolve();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
