package com.example.tt2.ejercicios;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class Ejercicio09 extends AppCompatActivity implements View.OnClickListener {
    int wordsFoundCount = 0;
    private int totalWords;

    private ChipGroup chipGroupPalabrasEje09;
    SopaDeLetrasViewDalia sopa;
    ImageView ivRegresarEje09;
    Button btnAudioInstruccionesEje09, btnFinalizarEje09;

    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    @Override
    protected void onDestroy() {
        if(mp != null){
            mp.release();
            mp = null;
        }
        if (mediaPlayerInstrucciones != null) {
            mediaPlayerInstrucciones.release();
            mediaPlayerInstrucciones = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio09);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivRegresarEje09 = findViewById(R.id.ivRegresarEje09);

        btnAudioInstruccionesEje09 = findViewById(R.id.btnAudioInstruccionesEje09);
        btnFinalizarEje09 = findViewById(R.id.btnFinalizarEje09);

        sopa = findViewById(R.id.sopaDeLetrasViewEje09);

        chipGroupPalabrasEje09 = findViewById(R.id.chipGroupPalabrasEje09);

        totalWords = chipGroupPalabrasEje09.getChildCount();

        sopa.setGridSize(9, 20);

        setupSopa(sopa);


        sopa.setOnWordFoundListener(word -> {
            removeWordChip(word);
            wordsFoundCount++;

            playWordAudio(word);

            if (wordsFoundCount == totalWords) {
                Toast.makeText(this, "¡Felicidades! Has terminado el ejercicio", Toast.LENGTH_LONG).show();
            }
        });

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio10);

        ivRegresarEje09.setOnClickListener(this);
        btnAudioInstruccionesEje09.setOnClickListener(this);
        btnFinalizarEje09.setOnClickListener(this);
    }

    private void reproducirAudios(int... audios){

        if(mp != null){
            try {
                mp.release();
            } catch (Exception e){
                e.printStackTrace();
            }
            mp = null;
        }

        if(audios.length == 0) return;

        reproducirSecuencia(audios, 0);
    }

    private void reproducirSecuencia(int[] audios, int index){

        mp = MediaPlayer.create(this, audios[index]);

        if(mp == null) return;

        mp.start();

        mp.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.release();

            int siguiente = index + 1;

            if(siguiente < audios.length){
                reproducirSecuencia(audios, siguiente);
            }
        });
    }

    private void setupSopa(SopaDeLetrasViewDalia sopa) {
        String[] template = {
                "TDXCUADROXCXXXLADRÓN",
                "ARXXPXXXXXUXXVXXXXXM",
                "LAXXIXXXXXAXXXIXXXAX",
                "AGXXEESCUADRAXXDXDXX",
                "DOXXDXXLADRILLOXRXXX",
                "RNXXRXXXXXAXXXXEXIZX",
                "OXXSALAMANDRAXXXXXOX",
                "XRARDALXXCOCODRILOXX",
                "DROMEDARIOALMENDRAXX"
        };

        List<SopaDeLetrasViewDalia.Word> words = new ArrayList<>();
        // Horizontales
        words.add(new SopaDeLetrasViewDalia.Word("CUADRO", 0, 3, 0, 8));
        words.add(new SopaDeLetrasViewDalia.Word("LADRÓN", 0, 14, 0, 19));
        words.add(new SopaDeLetrasViewDalia.Word("LADRAR", 7, 6, 7, 1));
        words.add(new SopaDeLetrasViewDalia.Word("ESCUADRA", 3, 5, 3, 12));
        words.add(new SopaDeLetrasViewDalia.Word("LADRILLO", 4, 7, 4, 14));
        words.add(new SopaDeLetrasViewDalia.Word("SALAMANDRA", 6, 3, 6, 12));
        words.add(new SopaDeLetrasViewDalia.Word("DROMEDARIO", 8, 0, 8, 9));
        words.add(new SopaDeLetrasViewDalia.Word("COCODRILO", 7, 9, 7, 17));
        words.add(new SopaDeLetrasViewDalia.Word("ALMENDRA", 8, 10, 8, 17));

        // Verticales
        words.add(new SopaDeLetrasViewDalia.Word("DRAGÓN", 0, 1, 5, 1));
        words.add(new SopaDeLetrasViewDalia.Word("TALADRO", 0, 0, 6, 0));
        words.add(new SopaDeLetrasViewDalia.Word("PIEDRA", 1, 4, 6, 4));
        words.add(new SopaDeLetrasViewDalia.Word("CUADRADO", 0, 10, 7, 10));

        // Diagonal ↘ (abajo-derecha)
        words.add(new SopaDeLetrasViewDalia.Word("VIDRIO", 1, 13, 6, 18));

        // Diagonal ↙ (abajo-izquierda)
        words.add(new SopaDeLetrasViewDalia.Word("MADRE", 1, 19, 5, 15));

        sopa.setBoard(template, words);
    }

    private void removeWordChip(String word) {
        for (int i = 0; i < chipGroupPalabrasEje09.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPalabrasEje09.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(word)) {
                chipGroupPalabrasEje09.removeView(chip);
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
        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, resId);
        if (mp != null) {
            mp.start();
        }
    }

    private void guardarEjercicioCompletado() {
        getSharedPreferences("ejercicios_completados", MODE_PRIVATE)
                .edit()
                .putBoolean("ejercicio08", true)
                .apply();

        Toast.makeText(this, "¡Ejercicio completado y guardado!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarEje09) { finish(); }
        else if (v.getId() == R.id.btnAudioInstruccionesEje09) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (v.getId() == R.id.btnFinalizarEje09) {
            if (wordsFoundCount == 15) {
                guardarEjercicioCompletado();
            } else {
                int faltantes = 15 - wordsFoundCount;
                Toast.makeText(this, "¡Aún faltan " + faltantes + " palabra(s) por encontrar!", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        }

    }
}