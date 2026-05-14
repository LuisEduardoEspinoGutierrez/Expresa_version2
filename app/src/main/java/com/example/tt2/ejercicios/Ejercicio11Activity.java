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

public class Ejercicio11Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayer; // Para palabras y efectos
    private MediaPlayer mediaPlayerInstrucciones; // Para instrucciones
    private ChipGroup chipGroupPalabras;
    private int totalWords;
    private int wordsFoundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio11);

        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        SopaDeLetrasView sopa = findViewById(R.id.sopaDeLetrasView);
        chipGroupPalabras = findViewById(R.id.chipGroupPalabras);
        Button btnFinalizar = findViewById(R.id.btnFinalizarEje11);

        setupSopa(sopa);

        totalWords = chipGroupPalabras.getChildCount();

        ivRegresar.setOnClickListener(v -> finish());

        // Configurar MediaPlayer para instrucciones (usa el mismo que el 10 según código original)
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
        // Tablero 12x12
        String[] template = {
                "FRAMBUESAXXF", // 0
                "FRASCOXXXXXR", // 1
                "XXXXXXFRIO_U".replace('_', 'X'), // 2  -> Col 11: U
                "FRESAXXXXXXT", // 3  -> Col 11: T
                "XXXXXXFRUTAE", // 4  -> Col 11: E
                "COFREFRAUDER", // 5  -> Col 11: R
                "REFRESCOXXFO", // 6  -> Col 11: O (Fin de FRUTERO) y Col 10: F (Inicio de FROTAR)
                "DISFRAZXXXRX", // 7  -> Col 10: R
                "AFRICAXXXXOX", // 8  -> Col 10: O
                "FRENTEXXXXTX", // 9  -> Col 10: T
                "FRACCIONXXAX", // 10 -> Col 10: A
                "FRANELAXXXRX"  // 11 -> Col 10: R (Fin de FROTAR)
        };

        List<SopaDeLetrasView.Word> words = new ArrayList<>();
        // Horizontales
        words.add(new SopaDeLetrasView.Word("FRAMBUESA", 0, 0, 0, 8));
        words.add(new SopaDeLetrasView.Word("FRASCO", 1, 0, 1, 5));
        words.add(new SopaDeLetrasView.Word("FRIO", 2, 6, 2, 9));
        words.add(new SopaDeLetrasView.Word("FRESA", 3, 0, 3, 4));
        words.add(new SopaDeLetrasView.Word("FRUTA", 4, 6, 4, 10));
        words.add(new SopaDeLetrasView.Word("COFRE", 5, 0, 5, 4));
        words.add(new SopaDeLetrasView.Word("FRAUDE", 5, 5, 5, 10));
        words.add(new SopaDeLetrasView.Word("REFRESCO", 6, 0, 6, 7));
        words.add(new SopaDeLetrasView.Word("DISFRAZ", 7, 0, 7, 6));
        words.add(new SopaDeLetrasView.Word("AFRICA", 8, 0, 8, 5));
        words.add(new SopaDeLetrasView.Word("FRENTE", 9, 0, 9, 5));
        words.add(new SopaDeLetrasView.Word("FRACCION", 10, 0, 10, 7));
        words.add(new SopaDeLetrasView.Word("FRANELA", 11, 0, 11, 6));

        // Verticales
        words.add(new SopaDeLetrasView.Word("FRUTERO", 0, 11, 6, 11));
        words.add(new SopaDeLetrasView.Word("FROTAR", 6, 10, 11, 10));

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
        // El nombre del recurso es fr_palabra (ej: fr_frasco)
        String resourceName = "fr_" + word.toLowerCase();
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
