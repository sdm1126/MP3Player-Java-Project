package kr.or.mrhi.mp3player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    // widgets
    private ImageView iv_main;

    // animation
    private Animation animation;

    // media player
    static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_main = findViewById(R.id.iv_main);

        // initialize
        mediaPlayer = new MediaPlayer();
        setAnimation();

        // move to next activity automatically
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToListViewActivity();
            }
        }, 1000);

    }

    private void setAnimation() {
        // load and set animation effect
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ic_main_anim);
        iv_main.setAnimation(animation);
    }

    private void moveToListViewActivity() {
        Intent intent = new Intent(MainActivity.this, ListViewActivity.class);
        startActivity(intent);
        finish();
    }
}