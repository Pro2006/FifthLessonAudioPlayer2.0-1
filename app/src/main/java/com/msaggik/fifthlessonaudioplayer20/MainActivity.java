package com.msaggik.fifthlessonaudioplayer20;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Runnable {

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer(); // создание поля медиа-плеера
    private SeekBar seekBar; // создание поля SeekBar
    private boolean wasPlaying = false; // поле проигрывания аудио-файла
    private FloatingActionButton fabPlayPause, fabNext, fabBack, fabLoop, fabForward; // поле кнопки проигрывания и постановки на паузу аудиофайла
    private TextView seekBarHint, songInformation; // поле информации у SeekBar
    private String metaDataAudio;
    private boolean isRepeat = false;
    int mediaPlayerStopProgress = 0;
    private String[] f;
    private int musicList = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присваивание полям id ресурсов
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabNext = findViewById(R.id.floatingActionNext);
        fabForward = findViewById(R.id.floatingActionForward);
        fabBack = findViewById(R.id.floatingButtonback);
        fabLoop = findViewById(R.id.floatingActionLoop);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        songInformation = findViewById(R.id.songInfomation);

        // создание слушателя изменения SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // метод при перетаскивании ползунка по шкале,
            // где progress позволяет получить нове значение ползунка (позже progress назрачается длина трека в миллисекундах)
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE); // установление видимости seekBarHint
                //seekBarHint.setVisibility(View.INVISIBLE); // установление не видимости seekBarHint

                // Math.ceil() - округление до целого в большую сторону
                int timeTrack = (int) Math.ceil(progress / 1000f); // перевод времени из миллисекунд в секунды

                // вывод на экран времени отсчёта трека
                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60) {
                    seekBarHint.setText("00:" + timeTrack);
                } else {
                    seekBarHint.setText("01:" + (timeTrack - 60));
                }

                // передвижение времени отсчёта трека
                double percentTrack = progress / (double) seekBar.getMax(); // получение процента проигранного трека (проигранное время делится на длину трека)
                // seekBar.getX() - начало seekBar по оси Х
                // seekBar.getWidth() - ширина контейнера seekBar
                // 0.92 - поправочный коэффициент (так как seekBar занимает не всю ширину своего контейнера)
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth() * percentTrack * 0.92));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится
                    mediaPlayer.pause(); // остановка и очиска MediaPlayer
                    // назначение кнопке картинки play
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    // установление seekBar значения 0
                }
            }

            // метод при начале перетаскивания ползунка по шкале
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE); // установление видимости seekBarHint
            }

            // метод при завершении перетаскивания ползунка по шкале
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                    mediaPlayer.seekTo(seekBar.getProgress()); // обновление позиции трека при изменении seekBar
                }
            }
        });

        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabLoop.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
        fabNext.setOnClickListener(listener);
    }

    private final View.OnClickListener listener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabPlayPause:
                    playSong();
                    break;
                case R.id.floatingActionNext:
                    if (mediaPlayer != null) {
                        clearMediaPlayer();
                        musicList += 1;
                        playSong();
                        break;
                    }
                case R.id.floatingButtonback:
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                    }
                    break;
                case R.id.floatingActionForward:
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                    }
                    break;
                case R.id.floatingActionLoop:
                    if (mediaPlayer != null) {
                        if (isRepeat) {
                            isRepeat = false;
                            mediaPlayer.setLooping(false);
                        } else {
                            isRepeat = true;
                            mediaPlayer.setLooping(true);
                        }
                        break;
                    }
            }

        }
    };

    // метод запуска аудио-файла
    public void playSong() {
        try { // обработка исключения на случай отстутствия файла
            if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                mediaPlayerStopProgress = mediaPlayer.getCurrentPosition();
                clearMediaPlayer();// остановка mediaPlayer
                wasPlaying = true; // инициализация значения запуска аудио-файла
                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) { // если mediaPlayer пустой
                    mediaPlayer = new MediaPlayer(); // то выделяется для него память
                }
                // альтернативный способ считывания файла с помощью файлового дескриптора
                /*File folder = new File("M:\\AndroidProjects\\FifthLessonAudioPlayer2.0\\app\\src\\main\\assets");
                File[] listOfFiles = folder.listFiles();
                System.out.println(listOfFiles);

                for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
                    if (listOfFiles[i].isFile()) {
                        fileList[i] = listOfFiles[i].getName();
                    }
                }*/
                f = getAssets().list("");
                int countOfMp3 = 0;
                String[] filesList = new String[2];
                for (int i = 0; i < f.length; i ++) {
                    if (f[i].contains(".mp3")) {
                        filesList[countOfMp3] = f[i];
                        countOfMp3 += 1;
                    }
                }
                countOfMp3 = 0;
                System.out.println(filesList);
                AssetFileDescriptor descriptor = getAssets().openFd(filesList[musicList % filesList.length]/* "cure_for_me.mp3"*/);
                // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                // получение мета-данных из аудио файла
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                metaDataAudio = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) +
                        " \n- ";
                String author = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (author == null) {
                    metaDataAudio += "Автор отсутствует";
                } else {
                    metaDataAudio += author;
                }
                mediaMetadataRetriever.release();
                songInformation.setText(metaDataAudio);


                descriptor.close(); // закрытие дескриптора

                mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                //mediaPlayer.setVolume(0.7f, 0.7f); // задание уровня громкости левого и правого динамиков
                mediaPlayer.setLooping(false); // назначение отстутствия повторов
                seekBar.setMax(mediaPlayer.getDuration()); // ограниечение seekBar длинной трека


                mediaPlayer.start(); // старт mediaPlayer
                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                new Thread(this).start(); // запуск дополнительного потока
                mediaPlayer.seekTo(mediaPlayerStopProgress);
                seekBar.setProgress(mediaPlayerStopProgress);
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }
    }

    // при уничтожении активити вызов метода остановки и очиски MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();

        clearMediaPlayer();
    }

    // метод остановки и очиски MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при условии не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека

            } catch (InterruptedException e) { // вызывается в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition); // обновление seekBar текущей позицией трека

        }
    }

   /* private String listAssetFiles(String path) {

        String [] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return null;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }*/
}