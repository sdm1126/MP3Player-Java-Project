package kr.or.mrhi.mp3player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class ListViewActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 111256;

    // widgets
    private Toolbar tb_list;
    private EditText et_list;
    private RecyclerView rv_list;

    // data
    private MusicDataDBHelper musicDataDBHelper;
    private Cursor cursor;
    private MusicData musicData;
    private ArrayList<MusicData> arrayList;
    private ArrayList<MusicData> likeArrayList;
    private ArrayList<MusicData> filteredArrayList;
    private String presentId;
    private String listName;

    // adapter
    private MusicAdapter musicAdapter;
    private LinearLayoutManager linearLayoutManager;
    private MusicAdapter.OnMusicSelectedListener onMusicSelectedListener;

    // media player
    private MediaPlayer mediaPlayer;

    // etc.
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        findViewByIds();
        initialize();

        // check whether permissions had been granted before
        int checkReadPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int checkWritePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(checkReadPermission != PackageManager.PERMISSION_GRANTED && checkWritePermission != PackageManager.PERMISSION_GRANTED) {
            // request permissions to reach external storage
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    MODE_PRIVATE);
        } else {
            // basically start with loading all musics on ListView
            loadListAll();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MODE_PRIVATE) {
            loadListAll();
        }
    }

    private void findViewByIds() {
        tb_list = findViewById(R.id.tb_list);
        et_list = findViewById(R.id.et_list);
        rv_list = findViewById(R.id.rv_list);
    }

    private void initialize() {
        // array list
        arrayList = new ArrayList<>();
        likeArrayList = new ArrayList<>();
        filteredArrayList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);

        // adapter
        onMusicSelectedListener = new MusicAdapter.OnMusicSelectedListener() {
            @Override
            public void selectMusic(int position) {
                Intent intent = new Intent(ListViewActivity.this, PlayViewActivity.class);
                intent.putExtra("arrayList", arrayList);
                intent.putExtra("filteredArrayList", filteredArrayList);
                intent.putExtra("likeArrayList", likeArrayList);
                intent.putExtra("listName", listName);
                intent.putExtra("position", position);
                intent.putExtra("presentId", presentId);
                startActivityForResult(intent, REQUEST_CODE);
            }
        };

        // database
        musicDataDBHelper = new MusicDataDBHelper(this);

        // media player
        mediaPlayer = MainActivity.mediaPlayer;

        // widget
        setToolbar();
        setSearchbar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == REQUEST_CODE && resultCode == PlayViewActivity.RESULT_CODE) {
            if(getIntent() != null) {
                presentId = intent.getStringExtra("presentId");
                Log.d("확인", "Play로부터 전달 받은 아이디: " + presentId);
            }
        }
    }

    // tool bar
    private void setToolbar() {
        setSupportActionBar(tb_list);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        tb_list.setTitleTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            // load all musics on ListView
            case R.id.menu_all:
                loadListAll();
                break;
            // load like musics on ListView
            case R.id.menu_like:
                loadListLike();
                break;
        }
        return true;
    }

    // search bar
    private void setSearchbar() {
        et_list.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = et_list.getText().toString();
                if (text.length() == 0) {
                    loadListAll();
                } else {
                    loadListFiltered(text);
                }
            }
        });
    }

    // load musics by sort of array list
    private void loadListAll() {
        setTitle("전체 목록");
        listName = "all";
        arrayList.clear(); // clear list not to be duplicated
        arrayList = getMusicDataAll();
        musicAdapter = new MusicAdapter(this, arrayList);
        musicAdapter.setOnMusicSelectedListener(onMusicSelectedListener); // invoke callback function
        rv_list.setLayoutManager(linearLayoutManager);
        rv_list.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }

    private void loadListLike() {
        setTitle("좋아요 목록");
        listName = "like";
        likeArrayList = musicDataDBHelper.selectLikeMusic();
        musicAdapter = new MusicAdapter(this, likeArrayList);
        musicAdapter.setOnMusicSelectedListener(onMusicSelectedListener);  // invoke callback function
        rv_list.setLayoutManager(linearLayoutManager);
        rv_list.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }

    private void loadListFiltered(String text) {
        setTitle("검색 목록");
        listName = "filtered";
        filteredArrayList = musicDataDBHelper.selectFilteredMusic(text);
        musicAdapter = new MusicAdapter(this, filteredArrayList);
        musicAdapter.setOnMusicSelectedListener(onMusicSelectedListener);  // invoke callback function
        rv_list.setLayoutManager(linearLayoutManager);
        rv_list.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }

    // load music data from external storage and insert into database
    private ArrayList<MusicData> getMusicDataAll() {
        // set columns to search data
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,      // file path
                MediaStore.Audio.Media.ALBUM_ID, // album image path
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION };

        // get data by columns using cursor
        try {
            cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,      // table(table path)
                    projection,                                       // columns
                    MediaStore.Audio.Media.DATA + " like ? ", // condition
                    new String[] {"%mymusic%"},                       // value by condition
                    MediaStore.Audio.Media.TITLE                      // sort condition
            );

            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)); // text-format conversion needed
                musicData = new MusicData(id, albumId, title, artist, duration);
                arrayList.add(musicData);

                // insert into database
                musicDataDBHelper.insertMusic(musicData);
            }

        } catch(Exception e) {
            Log.e("확인", e.toString());

        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return arrayList;
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        long gap = currentTime - backPressedTime;

        if(gap >= 0 && gap <= 2000) {
            super.onBackPressed();
            finish();

        } else {
            backPressedTime = currentTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // instead of release to avoid IllegalException from 'mediaPlayer.isplaying()' of PlayerViewActivity
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer = null;
        super.onDestroy();
    }
}