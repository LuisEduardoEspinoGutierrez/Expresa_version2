package com.example.tt2.ejercicios;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tt2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Ejercicio2Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerInstrucciones;
    private int aciertos = 0;
    // Eliminamos flores, quedan 16 correctas + 4 incorrectas = 20 imágenes (4x5)
    private final int TOTAL_CORRECTAS = 16;

    // Cola para manejar los audios uno tras otro
    private final Queue<Integer> audioQueue = new LinkedList<>();
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio2);

        // Flecha regresar
        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        ivRegresar.setOnClickListener(v -> finish());

        // Botón Audio Instrucciones
        Button btnAudioInstrucciones = findViewById(R.id.btnAudioInstrucciones);
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio2);
        btnAudioInstrucciones.setOnClickListener(v -> {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        });

        // Botón Finalizar
        Button btnFinalizar = findViewById(R.id.btnFinalizarEje02);
        btnFinalizar.setOnClickListener(v -> {
            if (aciertos == TOTAL_CORRECTAS) {
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudio(R.raw.no_has_terminado);
            }
        });

        GridLayout grid = findViewById(R.id.gridImagenes);
        ImageView zonaR = findViewById(R.id.zonaR);

        List<ItemImagen> listaImagenes = new ArrayList<>();
        // CORRECTAS (16 imágenes)
        listaImagenes.add(new ItemImagen(R.drawable.r_carrito, R.raw.r_carrito, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_frasco, R.raw.r_frasco, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_gorra, R.raw.r_gorra, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_perro, R.raw.r_perro, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_pizarron, R.raw.r_pizarron, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_radio, R.raw.r_radio, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_rama, R.raw.r_rama, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_rana, R.raw.r_rana, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_raqueta, R.raw.r_raqueta, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_raton, R.raw.r_raton, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_rayo, R.raw.r_rayo, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_regalo, R.raw.r_regalo, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_regla, R.raw.r_regla, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_reloj, R.raw.r_reloj, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_rosa, R.raw.r_rosa, true));
        listaImagenes.add(new ItemImagen(R.drawable.r_tornado, R.raw.r_tornado, true));

        // INCORRECTAS (4 imágenes)
        listaImagenes.add(new ItemImagen(R.drawable.n_casa, R.raw.n_casa, false));
        listaImagenes.add(new ItemImagen(R.drawable.n_gato, R.raw.n_gato, false));
        listaImagenes.add(new ItemImagen(R.drawable.n_luna, R.raw.n_luna, false));
        listaImagenes.add(new ItemImagen(R.drawable.n_mesa, R.raw.n_mesa, false));

        Collections.shuffle(listaImagenes);

        for (ItemImagen item : listaImagenes) {
            agregarImagenAlGrid(grid, item);
        }

        zonaR.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                View view = (View) event.getLocalState();
                boolean esCorrecta = (boolean) view.getTag();
                if (esCorrecta) {
                    grid.removeView(view); // Usamos removeView para que se active animateLayoutChanges
                    aciertos++;
                    reproducirAudio(R.raw.muy_bien);
                    if (aciertos == TOTAL_CORRECTAS) {
                        Toast.makeText(this, "¡Felicidades! Has terminado el ejercicio", Toast.LENGTH_LONG).show();
                    }
                } else {
                    reproducirAudio(R.raw.intentalo_otra_vez);
                }
            }
            return true;
        });
    }

    private void agregarImagenAlGrid(GridLayout grid, ItemImagen item) {
        ImageView img = new ImageView(this);
        img.setImageResource(item.imgRes);
        
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int size = (screenWidth / 4) - 40; // 4 columnas
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(10, 10, 10, 10);
        img.setLayoutParams(params);
        img.setPadding(10, 10, 10, 10);
        img.setTag(item.esCorrecta);

        img.setOnClickListener(v -> reproducirAudio(item.audioRes));
        img.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadow, v, 0);
                return true;
            }
            return false;
        });
        grid.addView(img);
    }

    private void reproducirAudio(int audioRes) {
        audioQueue.add(audioRes);
        if (!isPlaying) {
            playNextInQueue();
        }
    }

    private void playNextInQueue() {
        if (audioQueue.isEmpty()) {
            isPlaying = false;
            return;
        }

        isPlaying = true;
        Integer nextAudio = audioQueue.poll();
        if (nextAudio == null) {
            playNextInQueue();
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, nextAudio);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> playNextInQueue());
            mediaPlayer.start();
        } else {
            playNextInQueue();
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
        audioQueue.clear();
    }

    private static class ItemImagen {
        int imgRes, audioRes;
        boolean esCorrecta;
        ItemImagen(int imgRes, int audioRes, boolean esCorrecta) {
            this.imgRes = imgRes;
            this.audioRes = audioRes;
            this.esCorrecta = esCorrecta;
        }
    }
}