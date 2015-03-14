package com.handysparksoft.senku;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by davasens on 3/5/2015.
 */
public class Game {

    private enum State {READY_TO_PICK, READY_TO_DROP, FINISHED}
    private State gameState;
    private final int SIZE = 7;
    private final int[][] CROSS = {
        {0,0,1,1,1,0,0},
        {0,0,1,1,1,0,0},
        {1,1,1,1,1,1,1},
        {1,1,1,0,1,1,1},
        {1,1,1,1,1,1,1},
        {0,0,1,1,1,0,0},
        {0,0,1,1,1,0,0}
    };

    private final int[][] BOARD = {
            {0,0,1,1,1,0,0},
            {0,0,1,1,1,0,0},
            {1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1},
            {0,0,1,1,1,0,0},
            {0,0,1,1,1,0,0}
    };
    private int[][] grid;
    private int pickedI, pickedJ;
    private int jumpedI, jumpedJ;
    private long score;
    private long secondCounter = 0;
    private Date initTime;
    private Timer timer;
    private Boolean paused = false;
    private final int BASE_POINTS = 32;
    private LinkedList<String> stack;


    public Game() {
        this.grid = new int[SIZE][SIZE];

        for (int i=0;i<SIZE;i++) {
            for (int j=0;j<SIZE;j++) {
                grid[i][j] = CROSS[i][j];
            }
        }

        gameState = State.READY_TO_PICK;
        initTime = new Date();
        paused = false;
        secondCounter = 0;
        stack = new LinkedList<String>();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!paused) {
                    secondCounter++;
                }
            }
        },1000,1000);
    }

    public void pauseTimer() {
        this.paused = true;
    }
    public void restartTimer() {
        if (!isGameFinished()) {
            this.paused = false;
        }
    }

    public Long getTime() {
        //long diffInMs = new Date().getTime() - initTime.getTime();
        //long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        return secondCounter;
    }

    public String getTimeFormatted() {
        Long seconds = getTime();
        Long min = (seconds / 60);
        Long remainder = min == 0 ? seconds : seconds - (min * 60);
        Long sec = remainder;

        return String.format("%02d:%02d", min, sec);
    }

    public Boolean isAvailable(int i1, int j1, int i2, int j2) {
		//Ficha fuera del tablero de juego o ya consumida
		if (grid[i1][j1] == 0 || grid[i2][j2] == 1) {
			return false;
		}
		if (Math.abs(i2-i1) == 2 && j1 == j2) {
			jumpedI = i2>i1 ? i1 + 1: i2 + 1;
			jumpedJ = j1;
			if (grid[jumpedI][jumpedJ] == 1) {
				return true;
			}
		}
		if (Math.abs(j2-j1) == 2 && i1 == i2) {
			jumpedJ = j2 > j1 ? j1 + 1: j2 + 1;
			jumpedI = i1;
			if (grid[jumpedI][jumpedJ] == 1) {
				return true;
			}
		}
        return false;
    }

    public Boolean play(int i, int j) {
        Boolean result = false;
        if (gameState == State.READY_TO_PICK) {
            pickedI = i;
            pickedJ = j;
            gameState = State.READY_TO_DROP;
        }else if (gameState == State.READY_TO_DROP) {
            if (isAvailable(pickedI, pickedJ, i, j)) {
                gameState = State.READY_TO_PICK;
                grid[pickedI][pickedJ] = 0;
                grid[jumpedI][jumpedJ] = 0;
                grid[i][j] = 1;
                score = addScore();

                stack.add(String.format("%d;%d;%d",pickedI, pickedJ, 1));
                stack.add(String.format("%d;%d;%d",jumpedI, jumpedJ, 1));
                stack.add(String.format("%d;%d;%d",i, j, 0));

                result = true;
                if (isGameFinished()) {
                    gameState = State.FINISHED;
                }
            } else {
                pickedI = i;
                pickedJ = j;
            }
        }
        return result;
    }

    private long addScore() {

        int penalty = (int)(getTime() / 5);
        score += BASE_POINTS - (penalty < BASE_POINTS ? penalty : BASE_POINTS - 2);
        return score;
    }
    private long susScore() {
        score -= BASE_POINTS;
        score = score < 0 ? 0 : score;
        return score;
    }

    public long getScore() {
        return score;
    }

    public int getGrid(int i, int j) {
        return this.grid[i][j];
    }

    public Boolean isGameFinished() {
	
		for (int i=0;i<SIZE;i++) {
			for (int j=0;j<SIZE;j++) {
				for (int p=0;p<SIZE;p++) {
					for (int q=0;q<SIZE;q++) {
						if (grid[i][j] == 1 && grid[p][q] == 0 && BOARD[p][q] == 1) {
							if (isAvailable(i, j, p, q)) {
								return false;
							}
						}
					}	
				}
			}
		}
        return true;
    }

    public void random() {
        for (int i=0;i<SIZE;i++) {
            for (int j=0;j<SIZE;j++) {
                if (grid[i][j] == 1) {
                    int rndNumber = new Random().nextInt(10);
                    if (rndNumber > 5) {
                        grid[i][j] = 0;
                    }
                }

            }
        }
    }

    public void undo() {
        if (stack.size() >= 3) {
            String lastMove1 = stack.removeLast();
            String lastMove2 = stack.removeLast();
            String lastMove3 = stack.removeLast();
            if (lastMove1 != null && lastMove2 != null && lastMove3 != null) {
                if (lastMove1.length() > 0 && lastMove2.length() > 0 && lastMove3.length() > 0) {
                    String[] lm1 = lastMove1.split(";");
                    String[] lm2 = lastMove2.split(";");
                    String[] lm3 = lastMove3.split(";");

                    grid[Integer.valueOf(lm1[0])][Integer.valueOf(lm1[1])] = Integer.valueOf(lm1[2]);
                    grid[Integer.valueOf(lm2[0])][Integer.valueOf(lm2[1])] = Integer.valueOf(lm2[2]);
                    grid[Integer.valueOf(lm3[0])][Integer.valueOf(lm3[1])] = Integer.valueOf(lm3[2]);

                    susScore();
                }
            }
        }
    }
}
