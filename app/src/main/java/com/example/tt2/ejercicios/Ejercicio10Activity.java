package com.example.tt2.ejercicios;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tt2.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio10Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayer; // Para palabras y efectos
    private MediaPlayer mediaPlayerInstrucciones; // Para instrucciones
    private ChipGroup chipGroupPalabras;
    private int totalWords;
    private int wordsFoundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio10);

        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        SopaDeLetrasView sopa = findViewById(R.id.sopaDeLetrasView);
        chipGroupPalabras = findViewById(R.id.chipGroupPalabras);
        Button btnFinalizar = findViewById(R.id.btnFinalizarEje10);

        setupSopa(sopa);

        totalWords = chipGroupPalabras.getChildCount();

        ivRegresar.setOnClickListener(v -> finish());

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio10);

        btnAudio.setOnClickListener(v -> {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            if (wordsFoundCount == totalWords) {
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                playAudio(R.raw.no_has_terminado);
            }
        });

        sopa.setOnWordFoundListener(word -> {
            removeWordChip(word);
            wordsFoundCount++;

            playWordAudio(word);

            if (wordsFoundCount == totalWords) {
                Toast.makeText(this, "¡Felicidades! Has terminado el ejercicio", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSopa(SopaDeLetrasView sopa) {
        String[] template = {
                "TRICICLOXXXX",
                "TRENZATRONCO",
                "RXTRENXXXTXX",
                "UXTROFEOXRTX",
                "CXXXXXXXXERS",
                "HXTRACTORSEA",
                "AXTROMPAXNBS",
                "XMATRIMONIOT",
                "ESTRELLAXXLR",
                "TROMPETAXXXE",
                "MAESTRAXXXXX",
                "XXXXXXXXXXXX"
        };

        List<SopaDeLetrasView.Word> words = new ArrayList<>();
        // Horizontales
        words.add(new SopaDeLetrasView.Word("TRICICLO", 0, 0, 0, 7));
        words.add(new SopaDeLetrasView.Word("TRENZA", 1, 0, 1, 5));
        words.add(new SopaDeLetrasView.Word("TRONCO", 1, 6, 1, 11));
        words.add(new SopaDeLetrasView.Word("TREN", 2, 2, 2, 5));
        words.add(new SopaDeLetrasView.Word("TROFEO", 3, 2, 3, 7));
        words.add(new SopaDeLetrasView.Word("TRACTOR", 5, 2, 5, 8));
        words.add(new SopaDeLetrasView.Word("TROMPA", 6, 2, 6, 7));
        words.add(new SopaDeLetrasView.Word("MATRIMONIO", 7, 1, 7, 10));
        words.add(new SopaDeLetrasView.Word("ESTRELLA", 8, 0, 8, 7));
        words.add(new SopaDeLetrasView.Word("TROMPETA", 9, 0, 9, 7));
        words.add(new SopaDeLetrasView.Word("MAESTRA", 10, 0, 10, 6));

        // Verticales
        words.add(new SopaDeLetrasView.Word("TRUCHA", 1, 0, 6, 0));
        words.add(new SopaDeLetrasView.Word("TRES", 2, 9, 5, 9));
        words.add(new SopaDeLetrasView.Word("TREBOL", 3, 10, 8, 10));
        words.add(new SopaDeLetrasView.Word("SASTRE", 4, 11, 9, 11));

        sopa.setBoard(template, words);
    }

    private void removeWordChip(String word) {
        for (int i = 0; i < chipGroupPalabras.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPalabras.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(word)) {
                chipGroupPalabras.removeView(chip);
                break;
            }
        }
    }

    private void playWordAudio(String word) {
        String resourceName = "tr_" + word.toLowerCase();
        int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());

        if (resId != 0) {
            playAudio(resId);
        } else {
            playAudio(R.raw.muy_bien);
        }
    }

    private void playAudio(int resId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayerInstrucciones != null) {
            mediaPlayerInstrucciones.release();
            mediaPlayerInstrucciones = null;
        }
    }
}
