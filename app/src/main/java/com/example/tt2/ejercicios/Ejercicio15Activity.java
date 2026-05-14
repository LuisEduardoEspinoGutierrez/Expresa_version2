package com.example.tt2.ejercicios;

import android.content.ClipData;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tt2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ejercicio15Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayer; // Para efectos y palabras
    private MediaPlayer mediaPlayerInstrucciones; // Para instrucciones
    private GridLayout containerFuerte, containerLigera;
    private LinearLayout containerOpciones;
    private Button btnEscucharAleatorio, btnFinalizar;

    private int totalAciertos = 0;
    private final int TOTAL_ITEMS = 9;

    private List<EjercicioItem> itemsParaEscuchar = new ArrayList<>();
    private EjercicioItem itemActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio15);

        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        ivRegresar.setOnClickListener(v -> finish());

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio15);
        findViewById(R.id.btnAudioInstrucciones)
                .setOnClickListener(v -> {
                    if (mediaPlayerInstrucciones != null) {
                        if (mediaPlayerInstrucciones.isPlaying()) {
                            mediaPlayerInstrucciones.seekTo(0);
                        } else {
                            mediaPlayerInstrucciones.start();
                        }
                    }
                });

        containerFuerte = findViewById(R.id.containerFuerte);
        containerLigera = findViewById(R.id.containerLigera);
        containerOpciones = findViewById(R.id.containerOpciones);
        btnEscucharAleatorio = findViewById(R.id.btnEscucharAleatorio);
        btnFinalizar = findViewById(R.id.btnFinalizarEje15);

        btnEscucharAleatorio.setOnClickListener(v -> {
            if (itemActual != null) {
                // Si ya hay una palabra seleccionada pero no se ha clasificado, repetir el audio
                reproducirAudio(itemActual.audioRes);
            } else if (!itemsParaEscuchar.isEmpty()) {
                // Solo elegir una nueva palabra si la anterior ya fue clasificada
                Random random = new Random();
                itemActual = itemsParaEscuchar.remove(random.nextInt(itemsParaEscuchar.size()));
                reproducirAudio(itemActual.audioRes);
            } else {
                Toast.makeText(this, "¡Ya clasificaste todas las imágenes!", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            if (totalAciertos == TOTAL_ITEMS) {
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudio(R.raw.no_has_terminado);
            }
        });

        setupGame();
    }

    private void setupGame() {
        List<EjercicioItem> items = new ArrayList<>();

        // 🔥 limpiar lista antes de usar
        itemsParaEscuchar.clear();
        totalAciertos = 0;
        itemActual = null;

        // Fuertes
        items.add(new EjercicioItem(R.drawable.selimg_carrito, R.raw.selimg_carrito, true));
        items.add(new EjercicioItem(R.drawable.selimg_perro, R.raw.selimg_perro, true));
        items.add(new EjercicioItem(R.drawable.selimg_raton, R.raw.selimg_raton, true));
        items.add(new EjercicioItem(R.drawable.selimg_rio, R.raw.selimg_rio, true));
        items.add(new EjercicioItem(R.drawable.selimg_rueda, R.raw.selimg_rueda, true));

        // Ligeras
        items.add(new EjercicioItem(R.drawable.selimg_caracol, R.raw.selimg_caracol, false));
        items.add(new EjercicioItem(R.drawable.selimg_corazon, R.raw.selimg_corazon, false));
        items.add(new EjercicioItem(R.drawable.selimg_periodico, R.raw.selimg_periodico, false));
        items.add(new EjercicioItem(R.drawable.selimg_pirata, R.raw.selimg_pirata, false));

        itemsParaEscuchar.addAll(items);
        Collections.shuffle(items);

        for (EjercicioItem item : items) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(item.imgRes);

            int size = dpToPx(80);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(10, 0, 10, 0);
            iv.setLayoutParams(lp);

            iv.setTag(item);
            iv.setPadding(5, 5, 5, 5);

            iv.setOnTouchListener((v, event) -> {

                // 🔥 bloquear si no ha escuchado audio
                if (itemActual == null) {
                    Toast.makeText(Ejercicio15Activity.this, "Primero escucha una palabra", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                    return true;
                }
                return false;
            });

            containerOpciones.addView(iv);
        }

        containerFuerte.setOnDragListener(new MyDragListener(true));
        containerLigera.setOnDragListener(new MyDragListener(false));
    }

    private class MyDragListener implements View.OnDragListener {
        boolean esParaFuerte;

        MyDragListener(boolean esParaFuerte) {
            this.esParaFuerte = esParaFuerte;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {

            if (event.getAction() == DragEvent.ACTION_DROP) {

                View draggedView = (View) event.getLocalState();
                EjercicioItem item = (EjercicioItem) draggedView.getTag();

                // 🔥 VALIDACIÓN COMPLETA
                if (itemActual != null && item == itemActual && item.isFuerte == esParaFuerte) {

                    ViewGroup parent = (ViewGroup) draggedView.getParent();
                    if (parent != null) {
                        parent.removeView(draggedView);
                    }

                    GridLayout targetContainer = (GridLayout) v;

                    int size = dpToPx(65);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = size;
                    params.height = size;
                    params.setMargins(8, 8, 8, 8);

                    draggedView.setLayoutParams(params);
                    targetContainer.addView(draggedView);

                    draggedView.setOnTouchListener(null);
                    draggedView.setOnClickListener(v1 -> reproducirAudio(item.audioRes));

                    totalAciertos++;
                    itemActual = null;

                    reproducirAudio(R.raw.muy_bien);

                    if (totalAciertos == TOTAL_ITEMS) {
                        Toast.makeText(Ejercicio15Activity.this, "¡Excelente trabajo!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    reproducirAudio(R.raw.intentalo_otra_vez);
                }
            }

            return true;
        }
    }

    private void reproducirAudio(int audioRes) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(this, audioRes);

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
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

    private static class EjercicioItem {
        int imgRes;
        int audioRes;
        boolean isFuerte;

        EjercicioItem(int imgRes, int audioRes, boolean isFuerte) {
            this.imgRes = imgRes;
            this.audioRes = audioRes;
            this.isFuerte = isFuerte;
        }
    }
}