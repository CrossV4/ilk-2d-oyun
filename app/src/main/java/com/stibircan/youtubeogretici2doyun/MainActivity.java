package com.stibircan.youtubeogretici2doyun;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    private GameSheet gameSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout gameLayout = findViewById(R.id.am_FrameSheet);
        TextView scoreText = findViewById(R.id.am_ScoreText);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        MediaPlayer deathMusic = MediaPlayer.create(this, R.raw.kill);
        MediaPlayer passMusic = MediaPlayer.create(this, R.raw.pipe_pass);
        gameSheet = new GameSheet(this, displayMetrics.heightPixels, displayMetrics.widthPixels, scoreText, deathMusic, passMusic);
        gameLayout.addView(gameSheet);
    }

    private static class GameSheet extends View
    {
        private final Paint paint = new Paint();

        private final ball player;
        private final ArrayList<brick> bricks;
        private final TextView textView;
        private final int deviceWidth, deviceHeight;

        private final MediaPlayer death, pass;

        private int score = 0;

        @SuppressLint("SetTextI18n")
        GameSheet(Context context, int deviceHeight, int deviceWidth, TextView textView, MediaPlayer death, MediaPlayer pass)
        {
            super(context);
            this.textView = textView;
            this.deviceWidth = deviceWidth;
            this.deviceHeight = deviceHeight;
            this.death = death;
            this.pass = pass;

            textView.setText("Skor : " + score);

            player = new ball(context, deviceWidth, deviceHeight);
            bricks = new ArrayList<>();
            paint.setColor(Color.WHITE);

            Handler handler = new Handler();
            bricks.add(new brick(deviceWidth, deviceHeight));
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    bricks.add(new brick(deviceWidth, deviceHeight));
                    handler.postDelayed(this, 600);
                }
            }, 600);
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawColor(Color.DKGRAY);

            if (player.isDeath())
            {
                death.start();
                newGame();
                bricks.clear();
                bricks.add(new brick(deviceWidth, deviceHeight));
            }
            else
            {
                if (bricks.get(0).isDeath())
                {
                    pass.start();
                    score++;
                    textView.setText("Skor : " + score);
                    bricks.remove(0);
                }

                for (int b = 0; b < bricks.size(); b++)
                {
                    if (player.isTouch((int) bricks.get(b).X, bricks.get(b).Y, bricks.get(b).length))
                    {
                        death.start();
                        newGame();
                        bricks.clear();
                        bricks.add(new brick(deviceWidth, deviceHeight));
                        break;
                    }
                }

                for (int a = 0; a < bricks.size(); a++)
                {
                    bricks.get(a).draw(canvas, paint);
                    bricks.get(a).update();
                }

                player.draw(canvas, paint);
                player.update();
            }

            invalidate();

            super.onDraw(canvas);
        }

        private static class ball
        {
            final int yD, xD;

            final float X = 200;
            float Y;
            float radius = 25;

            double velocity = 0;

            final ArrayList<Bitmap> frames;
            boolean dummy;
            int locatedFrame;

            ball(Context context, int dH, int dY)
            {
                xD = dH;
                yD = dY;

                frames = new ArrayList<>();
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_0));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_1));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_2));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_3));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_4));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_5));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_6));
                frames.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bird_frame_7));
                dummy = true;
                locatedFrame = 0;
                locateFrames();

                Y = dY / 2;
            }

            void draw(Canvas canvas, Paint paint)
            {
                canvas.drawBitmap(frames.get(locatedFrame), X - frames.get(locatedFrame).getWidth() / 2, Y - frames.get(locatedFrame).getHeight() / 2, paint);
            }

            void tap()
            {
                int upForce = -15;
                velocity += upForce;
                locatedFrame = 0;
                dummy = false;
            }

            void update()
            {
                double gravity = 0.7;
                velocity += gravity;
                velocity *= 0.9;
                Y += velocity;
            }

            void newGame()
            {

                Y = yD / 2;
            }

            boolean isDeath()
            {
                return Y < 0 || Y > yD;
            }

            boolean isTouch(int pX, float pY, float length)
            {
                float lowX = pX, highX = pX + length;
                float lowY = pY, highY = pY + length;

                if (X > lowX && X < highX)
                {
                    return Y > lowY && Y < highY;
                }
                else
                    return false;
            }

            void locateFrames()
            {
                new Timer().scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        if (!dummy)
                        {
                            locatedFrame++;
                            if (locatedFrame == 7)
                            {
                                locatedFrame = 0;
                                dummy = true;
                            }
                        }
                    }
                }, 60, 60);
            }
        }

        private static class brick
        {
            float X;
            float Y;
            float length = 100;

            brick(int dW, int dH)
            {
                Y = dH /2;
                X = dW + length;
            }

            void draw(Canvas canvas, Paint paint)
            {
                canvas.drawRect(X, Y, X + length, Y + length, paint);
            }

            void update()
            {
                X -= 9;
            }

            boolean isDeath()
            {
                return X < 0;
            }
        }

        void newGame()
        {
            score = 0;
            textView.setText("Skor : " + score);
            player.newGame();
        }

        void touch()
        {
            player.tap();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        gameSheet.touch();
        return true;
    }

    @Override
    protected void onResume()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                handler.postDelayed(this, 3000);
            }
        }, 3000);
        super.onResume();
    }
}