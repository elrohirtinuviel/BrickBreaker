package com.emreakca.emreakcabricks;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.emreakca.emreakcabricks.databinding.ActivityMainBinding;

import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;

    //VISUAL COMPONENTS///////////////////////////////
    public TextView scoreText;
    public TextView livesText;
    public TextView gameOverText;
    public View paddle;
    public View ball;
    public LinearLayout brickContainer;
    public Button newGameButton;
    public Button quitButton;

    public TextView healthPowerUp;
    public TextView paddlePowerUp;
    public TextView powerUp;
    ValueAnimator animator;


    ////////////////////////////////////////////////////////////


    //BRICK CONTAINER VALUES////////////////////////////////////
    public int brickRows = 9;
    public int brickColumns = 10;
    public int brickWidth = 100;
    public int brickHeight = 60;
    public int brickMargin = 4;

    //////////////////////////////////////////////////////////


    //GAMEPLAY VALUES/////////////////////////////////////////
    public float ballSpeedX = 0f;
    public float ballSpeedY = 0f;
    public float speedIncrement = 1.015f;


    public float ballX = 0f;
    public float ballY = 0f;
    public float paddleX = 0f;

    public int paddleWidth = 0;

    public float powerUpX = 0f;
    public float powerUpY = 0f;

    public int lives = 3;
    public int score = 0;
    public int bricksLeft = 0;
    public int lv2BricksLeft = 0;
    public float powerUpChance = 20.0f;

    public boolean isBallLaunched = false;
    public boolean gameRunning = false;
    public boolean isPowerUpActive = false;
    public boolean initialStart = true;


    //////////////////////////////////////////////////////////

    //OTHER COMPONENTS///////////////////////////////////////

    public MediaPlayer mpPaddleHit,mpSoftBrickHit,mpHardBrickHit,mpLevelCompleted,mpGameOver,mpHealthPowerUpTaken,mpPaddleEnlargePowerUpTaken;
    Random rnd = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        FindViews(); //This function constructs the visual elements by scanning the activity_main.xml file
        ConstructMediaPlayers();
    //BUTTON CONSTRUCTORS////////////////////////////////////////

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!initialStart){
                    initialStart = true;
                }
                DefaultValues();
                ArrangeBricks();
                ResetValues();
                StartGame();
                lives = 3;
                newGameButton.setVisibility(View.INVISIBLE);
                quitButton.setVisibility(View.INVISIBLE);
                gameOverText.setVisibility(View.INVISIBLE);
                livesText.setText("Lives: " + lives);
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });



    ////////////////////////////////////////////////////////////////////

    }
    //This is for when the game starts again
    public void DefaultValues(){
        gameRunning = false;
        ballSpeedX = 0f;
        ballSpeedY = 0f;

        ballX = 0f;
        ballY = 0f;
        paddleX = 0f;

        paddleWidth = 0;

        powerUpX = 0f;
        powerUpY = 0f;

        lives = 3;
        score = 0;
        bricksLeft = 0;
        lv2BricksLeft = 0;
        powerUpChance = 20.0f;

        isBallLaunched = false;

        isPowerUpActive = false;

        if(paddleWidth != 0) {
            ViewGroup.LayoutParams p = paddle.getLayoutParams();
            p.width = paddleWidth;
            paddle.setLayoutParams(p);
        }
    }

    ////////////////////////////////////////////////////////////////////////


    //Function that creates the power-ups. It works on a chance based system every time a brick is broken.
    public void GeneratePowerUp(){
        if(!isPowerUpActive) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            float screenWidth = displayMetrics.widthPixels - 20;
            float screenHeight = displayMetrics.heightPixels - 450;
            isPowerUpActive = true;
            int powerUpResult = rnd.nextInt(2);
            if(powerUpResult == 0){
                powerUp = healthPowerUp;
            }
            else{
                powerUp = paddlePowerUp;
            }
            powerUpX = rnd.nextInt(Math.round(screenWidth));
            powerUpY = rnd.nextInt(Math.round(screenHeight));
            powerUp.setX(powerUpX);
            powerUp.setY(powerUpY);
            powerUp.setVisibility(View.VISIBLE);


        }
    }
    ///////////////////////////////////////////////////////////////////////////

    //Initialization of the media players that play the sound effects.
    public void ConstructMediaPlayers(){
        mpGameOver = MediaPlayer.create(this,R.raw.game_over);
        mpLevelCompleted = MediaPlayer.create(this,R.raw.level_completed);
        mpHealthPowerUpTaken = MediaPlayer.create(this,R.raw.health_powerup_taken);
        mpPaddleEnlargePowerUpTaken = MediaPlayer.create(this,R.raw.paddle_enlarge_pickup_taken);
        mpPaddleHit = MediaPlayer.create(this,R.raw.paddle_hit);
        mpSoftBrickHit = MediaPlayer.create(this,R.raw.soft_brick_hit);
        mpHardBrickHit = MediaPlayer.create(this,R.raw.hard_brick_hit);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void ResetValues(){
        ballX = 0;
        ballY = 0;

    }

    //////////////////////////////////////////////////////////////////////////////////
    public void StartGame() {
        ResetValues();
        gameRunning = true;
        MovePaddle();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenDensity = displayMetrics.density;
        float screenWidth = displayMetrics.widthPixels;
        float screenHeight = displayMetrics.heightPixels;
        paddleX = screenWidth / 2 - paddle.getWidth() / 2;
        paddle.setX(paddleX);
        ballX = screenWidth / 2 - ball.getWidth() / 2;
        ballX = rnd.nextInt(Math.round(ballX));
        ballY = screenHeight / 2 - ball.getHeight() / 2;


            if(initialStart) {
                ballSpeedX = (-3 + (int) Math.random() * 7) * screenDensity;
                ballSpeedY = (-3 + (int) Math.random() * 7) * screenDensity;
                initialStart = false;
            }



        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Long.MAX_VALUE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(gameRunning){
                    animation.setDuration(Long.MAX_VALUE);
                    MoveBall();
                    CheckCollision();
                }
                else{
                    animation.cancel();
                }

            }
        });
        animator.start();

    }


    //////////////////////////////////////////////////////////////////////////////////
    public void StopGame(){
        gameRunning = false;
        animator.cancel();
        animator = null;
    }

    //////////////////////////////////////////////////////////////////////////////////

    public void GameOver() {
        livesText.setText("Lives: 0");
        EmptyBrickContainer();
        gameOverText.setText("GAME OVER");
        newGameButton.setText("RETRY");
        newGameButton.setVisibility(View.VISIBLE);
        quitButton.setVisibility(View.VISIBLE);
        gameOverText.setVisibility(View.VISIBLE);
        mpGameOver.start();
        if(powerUp != null){
            powerUp.setVisibility(View.INVISIBLE);
            isPowerUpActive = false;
        }

    }
    //////////////////////////////////////////////////////////////////////////////////
    public void LevelCompleted(){
        gameRunning = false;
        newGameButton.setText("NEXT LEVEL");
        gameOverText.setText("LEVEL COMPLETED");
        newGameButton.setVisibility(View.VISIBLE);
        quitButton.setVisibility(View.VISIBLE);
        gameOverText.setVisibility(View.VISIBLE);
        mpLevelCompleted.start();
        powerUp.setVisibility(View.INVISIBLE);
        if(paddleWidth != 0){
            ViewGroup.LayoutParams p = paddle.getLayoutParams();
            p.width = paddleWidth;
            paddle.setLayoutParams(p);
        }
        DefaultValues();
        EmptyBrickContainer();

    }
    //////////////////////////////////////////////////////////////////////////////////
    public void ResetBallPosition() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenDensity = displayMetrics.density;
        float screenWidth = displayMetrics.widthPixels;
        float screenHeight = displayMetrics.heightPixels;
        ballX = screenWidth / 2 - ball.getWidth() / 2;
        ballY = screenHeight / 2 - ball.getHeight() / 2 - 425;
        ball.setX(rnd.nextInt(Math.round(ballX)));
        ball.setY(ballY);

        paddleX = screenWidth / 2 - paddle.getWidth() / 2;
        paddle.setX(paddleX);

    }

    //////////////////////////////////////////////////////////////////////////////////

    //Clears the linear layout that the bricks are in to avoid stacking on the existing bricks
    public void EmptyBrickContainer(){

        StopGame();
        brickContainer.removeAllViewsInLayout();

    }

    //////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    public void MovePaddle() {
        paddle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        MovePaddleF(event.getRawX());
                        break;
                }
                return true;
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void MoveBall() {
        float randomOffset = rnd.nextFloat();
        randomOffset /= 10;
        ballX += ballSpeedX + randomOffset;
        ballY += ballSpeedY + randomOffset;
        ball.setX(ballX);
        ball.setY(ballY);
    }
    public void MovePaddleF(float x) {
        paddleX = x - paddle.getWidth() / 2;
        paddle.setX(paddleX);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void CheckCollision() {
        // Check collision with walls
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (ballX <= 0 || ballX + ball.getWidth() >= screenWidth) {
            ballSpeedX *= -1;
        }
        if (ballY <= 0) {
            ballSpeedY *= -1;
        }


        if (powerUp != null && powerUp.getVisibility() == View.VISIBLE && ballY + ball.getHeight() >= powerUp.getY() && ballY + ball.getHeight() <= powerUp.getY() + powerUp.getHeight()
                && ballX + ball.getWidth() >= powerUp.getX() && ballX <= powerUp.getX() + powerUp.getWidth()) {

            if(powerUp == healthPowerUp){
                lives++;
                livesText.setText("Lives: " + lives);
                mpHealthPowerUpTaken.start();
            }
            else{

                ViewGroup.LayoutParams p = paddle.getLayoutParams();
                if(paddleWidth == 0) {
                    paddleWidth = p.width;
                }
                p.width *= 1.5;
                paddle.setLayoutParams(p);
                mpPaddleEnlargePowerUpTaken.start();

            }
            powerUp.setVisibility(View.INVISIBLE);
            isPowerUpActive = false;
        }


        // Check collision with paddle
        if (ballY + ball.getHeight() >= paddle.getY() && ballY + ball.getHeight() <= paddle.getY() + paddle.getHeight()
                && ballX + ball.getWidth() >= paddle.getX() && ballX <= paddle.getX() + paddle.getWidth()) {
            ballSpeedY *= -1;
            score++;
            scoreText.setText("Score: " + score);
            mpPaddleHit.start();


        }


        // Check collision with bricks
        for (int row = 0; row < brickRows; row++) {
            LinearLayout rowLayout = (LinearLayout) brickContainer.getChildAt(row);
            float rowTop = rowLayout.getY() + brickContainer.getY();
            float rowBottom = rowTop + rowLayout.getHeight();
            for (int col = 0; col < brickColumns; col++) {
                View brick = rowLayout.getChildAt(col);
                if (brick != null && brick.getVisibility() == View.VISIBLE) {
                    float brickLeft = brick.getX() + rowLayout.getX();
                    float brickRight = brickLeft + brick.getWidth();
                    float brickTop = brick.getY() + rowTop;
                    float brickBottom = brickTop + brick.getHeight();
                    if (ballX + ball.getWidth() >= brickLeft && ballX <= brickRight
                            && ballY + ball.getHeight() >= brickTop && ballY <= brickBottom) {

                        if(brick.getTag() == "LV1"){
                            brick.setVisibility(View.INVISIBLE);
                            bricksLeft--;
                            score+=2;
                            ballSpeedX = ballSpeedX * speedIncrement;
                            ballSpeedY = ballSpeedY * speedIncrement;
                            mpSoftBrickHit.start();
                            if(rnd.nextInt(100)<=powerUpChance){
                                GeneratePowerUp();
                            }

                        }
                        if(brick.getTag() == "LV2"){
                            if(bricksLeft == 0){
                                brick.setVisibility(View.INVISIBLE);
                                score+=5;
                                lv2BricksLeft--;
                                ballSpeedX = ballSpeedX * speedIncrement;
                                ballSpeedY = ballSpeedY * speedIncrement;

                                mpHardBrickHit.start();
                                if(rnd.nextInt(100)<=powerUpChance){
                                    GeneratePowerUp();
                                }
                            }
                            else{
                                ballSpeedY *= -1;
                            }

                        }

                        scoreText.setText("Score: " + score);
                        if(lv2BricksLeft == 0){
                            gameRunning = false;
                            LevelCompleted();
                        }
                    }
                }
            }
        }

        // Check collision with bottom wall
        if (ballY + ball.getHeight() >= screenHeight - 100) {
            lives--;
            if(paddleWidth != 0){
                ViewGroup.LayoutParams p = paddle.getLayoutParams();
                p.width = paddleWidth;
                paddle.setLayoutParams(p);
            }


            livesText.setText("Lives: " + lives);
            paddle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            MovePaddleF(event.getRawX());
                            break;
                    }
                    return true;
                }
            });
            if (lives <= 0) {
                lives = 3;
                GameOver();
            } else {
                // Reset the ball to its initial position
                ResetBallPosition();
                score -= 20;
                StartGame();
                ballSpeedX *= 0.66;
                ballSpeedY *= 0.66;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    //View initializations
    public void FindViews(){
        scoreText = findViewById(R.id.scoreText);
        livesText = findViewById(R.id.livesText);
        paddle = findViewById(R.id.paddle);
        ball = findViewById(R.id.ball);
        brickContainer = findViewById(R.id.brickContainer);
        gameOverText = findViewById(R.id.gameOverText);
        healthPowerUp = findViewById(R.id.healthPowerUp);
        paddlePowerUp = findViewById(R.id.paddlePowerUp);

        newGameButton = findViewById(R.id.newgame);
        quitButton = findViewById(R.id.quit);
    }
    //////////////////////////////////////////////////////////////////////////////////
    public void ArrangeBricks() {

        //Nested loops for creating the rows and columns of the bricks.

        //Outer loop is for the rows.
        for (int row = 0; row < brickRows; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            //Inner loops is for the columns.
            for (int col = 0; col < brickColumns; col++) {

                //Statement for the construction of the PINK bricks.
                if ((row != brickRows-1 && row != brickRows-2) || ((row == brickRows-1 || row == brickRows-2 )&& (col == 3 || col == 4 || col == 5 || col == 6))){
                    View brick = new View(this);
                    brick.setTag("LV1");
                    LinearLayout.LayoutParams brickParams = new LinearLayout.LayoutParams(brickWidth, brickHeight);
                    brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin);
                    brick.setLayoutParams(brickParams);
                    brick.setBackgroundResource(R.drawable.ic_launcher_background3);
                    rowLayout.addView(brick);
                    bricksLeft++;
                }
                //BLUE bricks construction.
                else if(row == brickRows-1 || row == brickRows -2){
                    View brick = new View(this);
                    brick.setTag("LV2");
                    LinearLayout.LayoutParams brickParams = new LinearLayout.LayoutParams(brickWidth, brickHeight);
                    brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin);
                    brick.setLayoutParams(brickParams);
                    brick.setBackgroundResource(R.drawable.ic_launcher_background2);
                    rowLayout.addView(brick);
                    lv2BricksLeft++;

                }
            }
            brickContainer.addView(rowLayout);
        }
    }


}  //End of Main Activity


//////////////////////////////////////////////////////////////////////////////////
