package kr.or.mrhi.mp3player;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PlayViewActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final int RESULT_CODE = 900624;

    // widgets
    private ImageView iv_photo_play;
    private TextView tv_title_play;
    private TextView tv_artist_play;
    private TextView tv_currentPosition_play;
    private TextView tv_duration_play;
    private SeekBar sb_play;
    private ImageButton ib_mode_play;
    private ImageButton ib_previous_play;
    private ImageButton ib_play_pause_play;
    private ImageButton ib_next_play;
    private ImageButton ib_like_play;

    // data
    private MusicDataDBHelper musicDataDBHelper;
    private Cursor cursor;
    private ArrayList<MusicData> arrayList; // list delivered from ListViewActivity
    private ArrayList<MusicData> likeArrayList;
    private ArrayList<MusicData> filteredArrayList;
    private int position;
    private int mode;
    private boolean isLiked;
    private boolean isPaused;
    private SimpleDateFormat simpleDateFormat;
    private String presentId;
    private String listName;

    // media player
    private MediaPlayer mediaPlayer;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_view);

        // get information(music list and its position) from ListViewActivity
        Intent intent = getIntent();
        arrayList = (ArrayList<MusicData>) intent.getSerializableExtra("arrayList");
        likeArrayList = (ArrayList<MusicData>) intent.getSerializableExtra("likeArrayList");
        filteredArrayList = (ArrayList<MusicData>) intent.getSerializableExtra("filteredArrayList");
        listName = intent.getStringExtra("listName");
        position = intent.getIntExtra("position", Integer.MAX_VALUE);
        presentId = intent.getStringExtra("presentId"); // already being played music's id

        findViewByIds();
        setEvents();
        initialize();

        // set list by list name delivered from ListViewActivity
        setArrayList(listName);

        // check whether present and previous music's ids are same
        checkSameMusic();

        // play next music automatically when music ends
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                switch(mode) {
                    case 0:
                    case 1:
                        playNextMusic();
                        break;
                    case 2:
                        int count = arrayList.size() - 1;
                        position = (int) (Math.random() * (count - 0 + 1) + 0);
                        playDifferentMusic(arrayList.get(position));
                        break;
                }
            }
        });
    }

    private void findViewByIds() {
        iv_photo_play = findViewById(R.id.iv_photo_play);
        tv_title_play = findViewById(R.id.tv_title_play);
        tv_artist_play = findViewById(R.id.tv_artist_play);
        tv_currentPosition_play = findViewById(R.id.tv_currentPosition_play);
        tv_duration_play = findViewById(R.id.tv_duration_play);
        sb_play = findViewById(R.id.sb_play);
        ib_mode_play = findViewById(R.id.ib_mode_play);
        ib_previous_play = findViewById(R.id.ib_previous_play);
        ib_play_pause_play = findViewById(R.id.ib_play_pause_play);
        ib_next_play = findViewById(R.id.ib_next_play);
        ib_like_play = findViewById(R.id.ib_like_play);
    }

    // event
    private void setEvents() {
        ib_mode_play.setOnClickListener(this);
        ib_previous_play.setOnClickListener(this);
        ib_play_pause_play.setOnClickListener(this);
        ib_next_play.setOnClickListener(this);
        ib_like_play.setOnClickListener(this);
        sb_play.setOnSeekBarChangeListener(this);
    }

    // handler
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_mode_play:
                int count = mode;
                ++count;
                count = (count == 3) ? count % 3 : count;
                setPlayMode(count);
                break;

            case R.id.ib_previous_play:
                playPreviousMusic();
                break;

            case R.id.ib_play_pause_play:
                if(!isPaused) {
                    pauseMusic();
                } else {
                    unPauseMusic();
                }
                break;

            case R.id.ib_next_play:
                playNextMusic();
                break;

            case R.id.ib_like_play:
                onClickLike();
                break;
        }
    }

    private void initialize() {
        // data
        simpleDateFormat = new SimpleDateFormat("mm:ss");
        musicDataDBHelper = new MusicDataDBHelper(this);

        // mediaplayer
        mediaPlayer = MainActivity.mediaPlayer;

        // status check
        isPaused = false;
    }

    private void setArrayList(String listName) {
        switch(listName) {
            case "all":                                          break;
            case "like":     this.arrayList = likeArrayList;     break;
            case "filtered": this.arrayList = filteredArrayList; break;
        }
    }

    private void checkSameMusic() {
        // if present and previous musics' ids are same, play music continuously
        if (arrayList.get(position).getId().equals(presentId)) {
            Log.d("확인", "현재 아이디: " + arrayList.get(position).getId() + " 이전 아이디: " + presentId);
            playSameMusic(arrayList.get(position));
            // if they are different, newly play music
        } else {
            Log.d("확인", "현재 아이디: " + arrayList.get(position).getId() + " 이전 아이디: " + presentId);
            playDifferentMusic(arrayList.get(position));
        }
    }

    // play
    private void playMusic(MusicData musicData) {
        // change play icon into pause icon
        if (mediaPlayer.isPlaying()) {
            ib_play_pause_play.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            ib_play_pause_play.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }

        setMusicData(musicData);
        setSeekBar();
        setPlayMode(mode);

        isPaused = false;
    }

    private void playSameMusic(MusicData musicData) {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.start();

        playMusic(musicData);
    }

    private void playDifferentMusic(MusicData musicData) {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();

            Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicData.getId()); // get music file path
            mediaPlayer.setDataSource(this, uri); // set music using music file path
            mediaPlayer.prepare();
            mediaPlayer.start();

            playMusic(musicData);

        } catch (Exception e) {
            Log.e("확인", e.toString());
        }
    }

    private void playPreviousMusic() {
        switch(mode) {
            case 0:
            case 1:
                position--;
                if(position < 0) {
                    position = arrayList.size() - 1;
                }
                playDifferentMusic(arrayList.get(position));
                break;
            case 2:
                int count = arrayList.size() - 1;
                position = (int) (Math.random() * (count - 0 + 1) + 0);
                playDifferentMusic(arrayList.get(position));
                break;
        }
    }

    private void playNextMusic() {
        switch(mode) {
            case 0:
            case 1:
                position++;
                if(position == arrayList.size()) {
                    position = 0;
                }
                playDifferentMusic(arrayList.get(position));
                break;
            case 2:
                int count = arrayList.size() - 1;
                position = (int) (Math.random() * (count - 0 + 1) + 0);
                playDifferentMusic(arrayList.get(position));
                break;
        }
    }

    // pause
    private void pauseMusic() {
        mediaPlayer.pause();
        ib_play_pause_play.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        isPaused = true;
    }

    private void unPauseMusic() {
        mediaPlayer.getCurrentPosition();
        mediaPlayer.start();

        ib_play_pause_play.setImageResource(R.drawable.ic_baseline_pause_24);
        isPaused = false;
        setSeekBar();
    }

    // set music data on widgets
    private void setMusicData(MusicData musicData) {
        // album image
        String albumImagePath = getAlbumImagePath(getApplication(), Long.parseLong(musicData.getAlbumId()));
        if (albumImagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(albumImagePath);
            iv_photo_play.setImageBitmap(bitmap);
        } else {
            iv_photo_play.setImageResource(R.drawable.ic_main);
        }
        // title
        tv_title_play.setText(musicData.getTitle());
        // artist
        tv_artist_play.setText(musicData.getArtist());
        // like button
        checkLikeCount();
    }

    // set play mode(3 types)
    private void setPlayMode(int mode) {
        switch(mode) {
            case 0:
                mediaPlayer.setLooping(false);
                ib_mode_play.setImageResource(R.drawable.ic_baseline_repeat_24);
                break;
            case 1:
                mediaPlayer.setLooping(true);
                ib_mode_play.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                break;
            case 2:
                mediaPlayer.setLooping(false);
                ib_mode_play.setImageResource(R.drawable.ic_baseline_shuffle_24);
                break;
        }
        this.mode = mode;
    }

    // get album image path
    private String getAlbumImagePath(Context context, long albumId) {
        String albumImagePath = null;

        try {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + " = ?",
                    new String[]{Long.toString(albumId)},
                    null
            );

            boolean result = cursor.moveToFirst();
            if (result) {
                albumImagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            }

            return albumImagePath;

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    // link SeekBar with music by async method
    private void setSeekBar() {
        sb_play.setProgress(0);
        sb_play.setMax(mediaPlayer.getDuration());

        String totalDuration = simpleDateFormat.format(mediaPlayer.getDuration());
        tv_duration_play.setText(totalDuration);
        thread = new Thread() {
            @Override
            public void run() {
                while (mediaPlayer.isPlaying()) {
                    // adapt thread to UI by using 'runOnUiThread()'(async method)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb_play.setProgress(mediaPlayer.getCurrentPosition());
                            String currentPosition = simpleDateFormat.format(mediaPlayer.getCurrentPosition());
                            tv_currentPosition_play.setText(currentPosition);
                        }
                    });
                    SystemClock.sleep(500);
                }
            }
        };
        thread.start();
    }

    // control SeekBar movement
    @Override
    public void onProgressChanged(SeekBar seekBar, int position, boolean isChangedByUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mediaPlayer.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.seekTo(sb_play.getProgress());
        if (seekBar.getProgress() >= 0 && seekBar.getProgress() <= mediaPlayer.getDuration()) {
            mediaPlayer.start();
        }
    }

    // set like button pushed if likeCount is 1
    private void checkLikeCount() {
        // load list of like-pushed music
        likeArrayList = musicDataDBHelper.selectLikeMusic();

        // basically set like button pushed
        for(MusicData musicData : likeArrayList) {
            if(arrayList.get(position).getId().equals(musicData.getId())) {
                ib_like_play.setImageResource(R.drawable.ic_baseline_star_24);
                isLiked = true;
            } else {
                ib_like_play.setImageResource(R.drawable.ic_baseline_star_border_24);
                isLiked = false;
            }
        }
    }

    // like-pushing event
    private void onClickLike() {
        if(isLiked == false) {
            Toast.makeText(getApplicationContext(), "좋아요 선택", Toast.LENGTH_SHORT).show();
            isLiked = true;
            ib_like_play.setImageResource(R.drawable.ic_baseline_star_24);
            // inform database that like button has been pushed
            musicDataDBHelper.likeMusic(arrayList.get(position));
        } else {
            Toast.makeText(getApplicationContext(), "좋아요 해제", Toast.LENGTH_SHORT).show();
            isLiked = false;
            ib_like_play.setImageResource(R.drawable.ic_baseline_star_border_24);
            // inform database that unlike button has been pushed
            musicDataDBHelper.unLikeMusic(arrayList.get(position));
        }
    }

    // press back button to move to ListViewActivity
    @Override
    public void onBackPressed() {
        presentId = arrayList.get(position).getId();

        Intent intent = new Intent(PlayViewActivity.this, ListViewActivity.class);
        intent.putExtra("presentId", presentId);
        setResult(RESULT_CODE, intent);

        Log.d("확인", "Play에서 List에게 보내는 현재 재생 중인 아이디: " + presentId);

        super.onBackPressed();
        finish();
    }
}