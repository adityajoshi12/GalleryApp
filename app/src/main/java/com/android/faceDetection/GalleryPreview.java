package com.android.faceDetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import java.io.File;


public class GalleryPreview extends AppCompatActivity {

    FaceOverlayView GalleryPreviewImg;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.gallery_preview);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        GalleryPreviewImg = (FaceOverlayView) findViewById(R.id.face_overlay);

//        File sd = Environment.getExternalStorageDirectory();
        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);

        GalleryPreviewImg.setBitmap(rotatedBitmap);

//        InputStream stream = getResources().openRawResource( R.raw.face );
//        Bitmap bitmap = BitmapFactory.decodeStream(stream);
//        Glide.with(GalleryPreview.this)
//                .load(new File(path)) // Uri of the picture
//                .into(GalleryPreviewImg);
    }
}
