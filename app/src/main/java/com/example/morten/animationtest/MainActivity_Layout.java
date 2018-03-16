package com.example.morten.animationtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.os.Vibrator;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

public class MainActivity_Layout extends SurfaceView implements Runnable {

    Thread thread               = null;
    boolean canDraw             = false;
    Context ctx = null;


    List<Circle> circleList = new ArrayList<>();


    private Sensor nSensor = null;
    SensorManager nSensorManager = null;

    float ax, ay = 0;


    Bitmap backGround           = null;
    Canvas canvas               = null;
    SurfaceHolder surfaceHolder = null;


    public MainActivity_Layout(Context context) {
        super(context);
        surfaceHolder = getHolder();
        backGround = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ctx = context;

        setup(context);

    }

    private void setup(Context context) {

        // Change how many balls are spawned
        // for (int i = 0; i < 2; i++) {
        //    for (int j = 0; j < 2; j++) {
        //        circleList.add(new Circle(i*300, j*800));
        //    }
        // }

        // Random value for positions
        Random rand = new Random();
        int v = rand.nextInt(1200) + 200;

        // Spawn exactly 2 circles
        circleList.add(new Circle(v, 800));
        circleList.add(new Circle(1500, v));


        nSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (nSensorManager != null) {
            nSensor = nSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        } else {
            Log.e("SensorManagerError: ", "nSensorManager cant get system service");
        }

        SensorEventListener gyroscopeSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                ax = sensorEvent.values[0] / 15;
                ay = sensorEvent.values[1] / 15;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        nSensorManager.registerListener(gyroscopeSensorListener, nSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void run() {

        while(canDraw) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(backGround, 0, 0, null);

            draw();

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void checkBallCollisions() {
        for (int i = 0; i < circleList.size() - 1; i++) {
            for (int j = i; j < circleList.size(); j++) {
                Circle c1 = circleList.get(i);
                Circle c2 = circleList.get(j);
                if (checkBallCollide(circleList.get(i), circleList.get(j))) {
                    float tmp2x = c2.vx, tmp2y = c2.vy;
                    c2.vx = c1.vx;
                    c2.vy = c1.vy;

                    c1.vx = tmp2x;
                    c1.vy = tmp2y;
                }
            }
        }
    }

    private boolean checkBallCollide(Circle c1, Circle c2) {
        float deltaX = Math.abs((c1.x - c1.vx) - (c2.x + c2.vx));
        float deltaY = Math.abs((c1.y + c1.vy) - (c2.y + c2.vy));
        return (deltaX <= c1.radius + c2.radius && deltaY <= c1.radius + c2.radius);
    }

    public void resume() {
        canDraw = true;

        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        canDraw = false;

        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                Log.e("Error: ", e.getMessage());
            }
        }
        thread = null;

    }

    private void draw() {
        for (int i = 0; i < circleList.size(); ++i) {
            circleList.get(i).draw();
            checkBallCollisions();
        }
    }



    public class Circle {
        private float x, y, vx, vy = 0;
        private int radius = 20;
        private MediaPlayer mp = MediaPlayer.create(ctx, R.raw.ding);
        private Vibrator v = (Vibrator) ctx.getSystemService(VIBRATOR_SERVICE);

        Paint circlePaint = new Paint();

        private Circle(float xpos, float ypos) {
            this.circlePaint.setColor(Color.WHITE);
            this.circlePaint.setStyle(Paint.Style.FILL);
            x = xpos; y = ypos;
        }

        private void wallCollision() {
            if (this.x - this.radius < 0) {
                this.x = this.radius;
                this.vx *= -0.6f;
                notifyClient();
            }
            if (this.x + this.radius > canvas.getWidth()) {
                this.x = canvas.getWidth() - this.radius;
                this.vx *= -0.6f;
                notifyClient();
            }
            if (this.y - this.radius < 0) {
                this.y = this.radius;
                this.vy *= -0.6f;
                notifyClient();
            }
            if (this.y + this.radius > canvas.getHeight()) {
                this.y = canvas.getHeight() - this.radius;
                this.vy *= -0.6f;
                notifyClient();
            }
        }

        private void notifyClient() {
            this.mp.seekTo(100);
            this.mp.start();
            v.vibrate(400);
        }

        private void draw() {
            this.vx += ax; this.vy += ay;

            wallCollision();

            this.x -= this.vx; this.y += this.vy;

            canvas.drawCircle(this.x, this.y, this.radius, circlePaint);
        }
    }
}
