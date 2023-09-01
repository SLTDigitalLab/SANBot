package com.example.san;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class videoActivity extends AppCompatActivity {
    @BindView(R.id.videoView)
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        String videoURL="https://drive.google.com/file/d/1CQjsCQIk12YXqp1yMiWGhCBhXniuBklx/view?usp=sharing";
        Uri uri=Uri.parse(videoURL);
        videoView.setVideoURI(uri);
        //Creating MediaController
        MediaController mediaController= new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.requestFocus();
        videoView.start();
        Intent intent = new Intent(videoActivity.this, MainActivity2.class);
        startActivity(intent);
        finish();
    }
}