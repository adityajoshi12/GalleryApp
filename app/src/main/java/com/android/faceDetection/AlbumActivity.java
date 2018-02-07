package com.android.faceDetection;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class AlbumActivity extends AppCompatActivity {
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    String album_name = "";
    LoadAlbumImages loadAlbumTask;

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        album_name = intent.getStringExtra("name");
        setTitle(album_name);


        galleryGridView = (GridView) findViewById(R.id.galleryGridView);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();


    }


    class LoadAlbumImages extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            int id =0;
            String timestamp = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.Images.ImageColumns.PICASA_ID};

            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                String picasa_id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.PICASA_ID));
                if (TextUtils.isEmpty(picasa_id)) {
                    picasa_id = "-1";
                }
                imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null, String.valueOf(id), picasa_id));
                Log.d("GalleryApp", "picasa_id " + picasa_id + " for path " + path + "value of id" + id);
            }
            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            Collections.sort(imageList, new MapComparator(Function.KEY_PICASA_ID, "asc"));
            Log.d("GalleryApp", " for path " + imageList.size());
            for (int i = 0; i < imageList.size(); i++) {
                //ImageView child = (ImageView) galleryGridView.getChildAt(i);
                // do stuff with child view
                String path1 = imageList.get(i).get(Function.KEY_PATH);
                int rowId =  Integer.parseInt(imageList.get(i).get(Function.KEY_ID));
                Log.d("GalleryApp", " for path " + path1);
                File image = new File(imageList.get(i).get(Function.KEY_PATH));
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if (Integer.parseInt(imageList.get(i).get(Function.KEY_PICASA_ID)) < 0) {
                    detectFaces(rotatedBitmap, rowId);
                }
            }

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            SingleAlbumAdapter adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
            galleryGridView.setAdapter(adapter);
//            Log.d("GalleryApp" ,  " for path " + imageList.size() );
//            for(int i=0; i<imageList.size(); i++) {
//                //ImageView child = (ImageView) galleryGridView.getChildAt(i);
//                // do stuff with child view
//                String path = imageList.get(i).get(Function.KEY_PATH);
//                Log.d("GalleryApp" ,  " for path " + path );
//                File image = new File(imageList.get(i).get(Function.KEY_PATH));
//                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
//
//               Matrix matrix = new Matrix();
//                matrix.postRotate(270);
//                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
//
//                detectFaces(rotatedBitmap);
//            }

            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH));
                    startActivity(intent);
                }
            });
        }
    }

    public void detectFaces(Bitmap bitmap, int rowId) {
        mBitmap = bitmap;
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        if (!detector.isOperational()) {
            //Handle contingency
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            mFaces = detector.detect(frame);
            detector.release();
        }
        Log.d("GalleryApp", " Face count" + mFaces.size());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.PICASA_ID, mFaces.size());

        getContentResolver().update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, MediaStore.MediaColumns._ID+"=?", new String[] {String.valueOf(rowId)});
        Log.d("GalleryApp", "UPdate");
    }


    class SingleAlbumAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<HashMap<String, String>> data;

        public SingleAlbumAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
            activity = a;
            data = d;
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            SingleAlbumViewHolder holder = null;
            if (convertView == null) {
                holder = new SingleAlbumViewHolder();
                convertView = LayoutInflater.from(activity).inflate(
                        R.layout.single_album_row, parent, false);

                holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);

                convertView.setTag(holder);
            } else {
                holder = (SingleAlbumViewHolder) convertView.getTag();
            }
            holder.galleryImage.setId(position);

            HashMap<String, String> song = new HashMap<String, String>();
            song = data.get(position);
            try {

                Glide.with(activity)
                        .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.galleryImage);


            } catch (Exception e) {
            }
            return convertView;
        }
    }


    class SingleAlbumViewHolder {
        ImageView galleryImage;
    }
}
