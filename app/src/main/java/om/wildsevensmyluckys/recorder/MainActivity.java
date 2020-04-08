package om.wildsevensmyluckys.recorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SoundRecordingDemo";

    private Button mStartRecordingButton;
    private Button mStopRecordingButton;
    private MediaRecorder mediaRecorder;
    private File fileName;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaRecorder = new MediaRecorder();

        mStartRecordingButton = findViewById(R.id.record_button);
        mStopRecordingButton = findViewById(R.id.stop_button);

        mStartRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    mStartRecordingButton.setEnabled(false);
                    mStopRecordingButton.setEnabled(true);
                    mStopRecordingButton.requestFocus();

                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);
                    } else {
                       recordStart();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Caught io exception " + e.getMessage());
                }
            }
        });

        mStopRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mStartRecordingButton.setEnabled(true);
                mStopRecordingButton.setEnabled(false);
                mStartRecordingButton.requestFocus();
                recordStop();
            }
        });

        mStopRecordingButton.setEnabled(false);
        mStartRecordingButton.setEnabled(true);
    }


    public void recordStart() {
        if (fileName == null) {
            try {
                File sampleDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), getPackageName() + getResources().getString(R.string.app_name));
                if (!sampleDir.exists()) {
                    sampleDir.mkdirs();
                }

                // Текущее время
                Date currentDate = new Date();
// Форматирование времени как "день.месяц.год"
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String dateText = dateFormat.format(currentDate);
// Форматирование времени как "часы:минуты:секунды"
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String timeText = timeFormat.format(currentDate);


                fileName = File.createTempFile(String.format("meow %s %s ", dateText, timeText), ".3gp", sampleDir);
            } catch (IOException e) {

                Log.e(TAG, "sdcard access error");
                return;
            }
        }

        try {
            releaseRecorder();

            if (fileName.exists()) {
                fileName.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName.getPath());
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            fileName = null;
        }
    }

    public void playStart(View v) {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
    }
}
