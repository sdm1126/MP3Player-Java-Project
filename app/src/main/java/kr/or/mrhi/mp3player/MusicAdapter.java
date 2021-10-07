package kr.or.mrhi.mp3player;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    public static final int ALBUM_IMAGE_SIZE = 80;
    public static final BitmapFactory.Options options = new BitmapFactory.Options();

    private Context context;
    private ArrayList<MusicData> arrayList; // listed musics can be different by delivered arraylist from ListViewActivity
    private OnMusicSelectedListener onMusicSelectedListener; // set member variable to invoke interface across this class

    // interface to make item-click-event occur from ListViewActivity list and music data exist
    public interface OnMusicSelectedListener {
        void selectMusic(int position);
    }

    public MusicAdapter(Context context, ArrayList<MusicData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public void setOnMusicSelectedListener(OnMusicSelectedListener onMusicSelectedListener) {
        this.onMusicSelectedListener = onMusicSelectedListener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_view, parent, false);
        MusicViewHolder musicViewHolder = new MusicViewHolder(view);
        return musicViewHolder;
    }

    /* bind down below (view)holder and arraylist position &
       set data on widget by arraylist position */
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        if(holder instanceof MusicViewHolder) {
            holder.onMusicSelectedListener = onMusicSelectedListener;
        }

        // album image
        Bitmap bitmap = null;
        if(arrayList.get(position).getAlbumId() != null) {
            bitmap = getAlbumImage(context, Long.parseLong(arrayList.get(position).getAlbumId()), ALBUM_IMAGE_SIZE);
        }
        if(bitmap != null) {
            holder.iv_photo_item.setImageBitmap(bitmap);
        } else {
            holder.iv_photo_item.setImageResource(R.drawable.ic_main);
        }
        // title
        holder.tv_title_item.setText(arrayList.get(position).getTitle());
        // artist
        holder.tv_artist_item.setText(arrayList.get(position).getArtist());
        // duration
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        holder.tv_duration_item.setText(simpleDateFormat.format(Integer.parseInt(arrayList.get(position).getDuration())));
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Bitmap getAlbumImage(Context context, long albumId, int albumImageSize) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumId);
        if(uri != null) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r"); // 'r' means read-only
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
                if (bitmap != null) {
                    // resize bitmap image
                    if (options.outHeight != albumImageSize || options.outWidth != albumImageSize) {
                        Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true);
                        bitmap.recycle();
                        bitmap = tempBitmap;
                    }
                }
                return bitmap;

            } catch(Exception e) {
                Log.e("확인", e.toString());

            } finally {
                try {
                    if (parcelFileDescriptor != null) {
                        parcelFileDescriptor.close();
                    }
                } catch (IOException e) {
                    Log.e("확인", e.toString());
                }
            }
        }
        return null;
    }

    // item findViewIds() & handler & event
    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_photo_item;
        private TextView tv_title_item;
        private TextView tv_artist_item;
        private TextView tv_duration_item;

        private OnMusicSelectedListener onMusicSelectedListener;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_photo_item = itemView.findViewById(R.id.iv_photo_item);
            tv_title_item = itemView.findViewById(R.id.tv_title_item);
            tv_artist_item = itemView.findViewById(R.id.tv_artist_item);
            tv_duration_item = itemView.findViewById(R.id.tv_duration_item);

            itemView.setTag(getAdapterPosition());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMusicSelectedListener.selectMusic(getAdapterPosition());
                }
            });
        }
    }
}
