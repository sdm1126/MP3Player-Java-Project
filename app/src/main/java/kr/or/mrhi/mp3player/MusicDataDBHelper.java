package kr.or.mrhi.mp3player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MusicDataDBHelper extends SQLiteOpenHelper {

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public MusicDataDBHelper(@Nullable Context context) {
        super(context, "musicDB", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE IF NOT EXISTS musicTBL(id TEXT NOT NULL PRIMARY KEY, albumId TEXT NOT NULL, title TEXT NOT NULL, artist TEXT NOT NULL, duration TEXT NOT NULL, likeCount INTEGER DEFAULT 0)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS musicTBL";
        sqLiteDatabase.execSQL(query);
        onCreate(sqLiteDatabase);
    }

    public void insertMusic(MusicData musicData) {
        try {
            sqLiteDatabase = this.getWritableDatabase();
            String query = String.format("INSERT INTO musicTBL (id, albumId, title, artist, duration) VALUES ('%s', '%s', '%s', '%s', '%s');",
                    musicData.getId(), musicData.getAlbumId(), musicData.getTitle(), musicData.getArtist(), musicData.getDuration());
            sqLiteDatabase.execSQL(query);
            Log.d("확인", "데이터 추가 완료");

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            sqLiteDatabase.close();
        }
    }

    public boolean likeMusic(MusicData musicData) {
        boolean flag = false;

        try {
            sqLiteDatabase = this.getWritableDatabase();
            String query = String.format("UPDATE musicTBL SET likeCount =" + 1 + " WHERE id = '%s';", musicData.getId());
            sqLiteDatabase.execSQL(query);
            flag = true;
            Log.d("확인", "좋아요 추가 완료");

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            sqLiteDatabase.close();
        }

        return flag;
    }

    public boolean unLikeMusic(MusicData musicData) {
        boolean flag = false;

        try {
            sqLiteDatabase = this.getWritableDatabase();
            String query = String.format("UPDATE musicTBL SET likeCount =" + 0 + " WHERE id = '%s';", musicData.getId());
            sqLiteDatabase.execSQL(query);
            flag = true;
            Log.d("확인", "좋아요 취소 완료");

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            sqLiteDatabase.close();
        }

        return flag;
    }

    public ArrayList<MusicData> selectLikeMusic() {
        ArrayList<MusicData> arrayList = null;
        Cursor cursor = null;

        try {
            arrayList = new ArrayList<>();
            sqLiteDatabase = this.getReadableDatabase();
            cursor = sqLiteDatabase.rawQuery("SELECT id, albumId, title, artist, duration FROM musicTBL WHERE likeCount = 1;", null);
            while(cursor.moveToNext()) {
                arrayList.add(new MusicData(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            }

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            cursor.close();
            sqLiteDatabase.close();
        }

        return arrayList;
    }

    public ArrayList<MusicData> selectFilteredMusic(String text) {
        ArrayList<MusicData> arrayList = null;
        Cursor cursor = null;

        try {
            arrayList = new ArrayList<>();
            sqLiteDatabase = this.getReadableDatabase();
            cursor = sqLiteDatabase.rawQuery("SELECT id, albumId, title, artist, duration FROM musicTBL WHERE title LIKE" + " '%" + text + "%'" + " OR artist LIKE" + " '%" + text + "%';", null);
            while(cursor.moveToNext()) {
                arrayList.add(new MusicData(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            }

        } catch (Exception e) {
            Log.e("확인", e.toString());

        } finally {
            cursor.close();
            sqLiteDatabase.close();
        }

        return arrayList;
    }
}
