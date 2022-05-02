package com.der.muxicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.der.muxicplayer.adapter.CustomAdapter;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 12345;
    private static final int PERMISSIONS_COUNT = 1;


    private boolean isMusicPlayerInit;
    private List<String> musicFileList;
    Thread updateSeekBar;
    MediaPlayer mediaPlayer;
    BarVisualizer visualizer;
    TextView tv_start;
    TextView tv_end;
    ListView listView;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        visualizer = findViewById(R.id.bar);
        tv_start = findViewById(R.id.tv_start);
        tv_end = findViewById(R.id.tv_end);
        listView = findViewById(R.id.listView);
        seekBar = findViewById(R.id.seek_bar);

    }

/*    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        //SDK_INT >= Build.VERSION_CODES.M ) is TRUE - means the device running the app has Android SDK 23 or up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSION);
            return;
        }

        if (!isMusicPlayerInit){
            musicFileList = new ArrayList<>();
            CustomAdapter adapter = new CustomAdapter();


            fillMusicList();
            adapter.setData(musicFileList);
            listView.setAdapter(adapter);
            isMusicPlayerInit = true;

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        final String musicFilePath = musicFileList.get(position);
                        //TODO: first Music dosen't stop if you play other music
                        playMusicFile(musicFilePath ,seekBar, position);

                    }

            });

        }

    }


    private void playMusicFile(String path, SeekBar seekBar, int position) {
            Uri uri = Uri.parse(path);
            mediaPlayer = MediaPlayer.create(this, uri);

            System.out.println(mediaPlayer.isPlaying() + " : : :  : : : : :: : "+ mediaPlayer.getDuration());
            System.out.println(path + " : : :  : : : : :: : " + position);

            mediaPlayer.start();




        //TODO : 1hour music dose not work in seek bar.
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setVisibility(View.VISIBLE);
        tv_start.setVisibility(View.VISIBLE);
        tv_end.setVisibility(View.VISIBLE);

        //TODO : when device rotate seek bar is gone

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                super.run();
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration){
                    try {
                        Thread.sleep(500);
                        currentPosition =mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateSeekBar.start();


        String endTime =calculateMusicDurationMillsToMinutes(mediaPlayer.getDuration());
        tv_end.setText(endTime);

        Handler handler =new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = calculateMusicDurationMillsToMinutes(mediaPlayer.getCurrentPosition());
                tv_start.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

/*        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }*/


    }

    private String calculateMusicDurationMillsToMinutes(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time = min + ":";
        time += sec;
        return time;
    }

    private void fillMusicList(){
        musicFileList.clear();
        String dirPath1 = System.getenv("EXTERNAL_STORAGE");
        String dirPath2 = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        String dirPath3 = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

        addMusicFileFrom(dirPath1);
        addMusicFileFrom(dirPath2);
        addMusicFileFrom(dirPath3);
    }

    private void addMusicFileFrom(String dirPath){
        final File musicDir = new File(dirPath).getAbsoluteFile();

        if (!musicDir.exists()){
            musicDir.mkdir();
            return;
        }

        final File[] files = musicDir.listFiles();

        if (files != null){

            for (File file : files){
                final String path = file.getAbsolutePath();
                if (path.endsWith(".mp3")){//TODO : .mp3 dose't know
                    System.out.println("******** " + path + " ***********");
                    musicFileList.add(path);
                }else if (path.endsWith(".m4a")){//TODO : .m4a is seen but do not play
                    musicFileList.add(path);
                }else if (path.endsWith(".aac")){
                    musicFileList.add(path);
                }else if (path.endsWith(".wav")){//TODO : .wav dose't know
                    System.out.println("******** " + path + " ***********");
                    musicFileList.add(path);
                }

            }

        }

    }

    @Override
    public void recreate() {
        super.recreate();
        Log.d("App_MainActivity", "recreate is call");
    }

    @SuppressLint("NewApi")
    private boolean arePermissionDenied(){

        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            Log.d("App_MainActivity", PERMISSIONS[i] + " != " + PackageManager.PERMISSION_GRANTED);
                if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                    return true;
                }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("App_MainActivity", "onRequestPermissionsResult is call");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (arePermissionDenied()){
            ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            recreate();
            Log.d("App_MainActivity", "user deny the permission");
        }else {
            Log.d("App_MainActivity", "user approve the permission");
            onResume();
        }
    }



}