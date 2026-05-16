package com.example.tt2.ejercicios;

import android.app.AlertDialog;
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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Ejercicio15Activity extends AppCompatActivity {

    private static final String TAG = "Ejercicio15Activity";
    private MediaPlayer mediaPlayer; // Para efectos y palabras
    private MediaPlayer mediaPlayerInstrucciones; // Para instrucciones
    private GridLayout containerFuerte, containerLigera;
    private LinearLayout containerOpciones;
    private Button btnEscucharAleatorio, btnFinalizar;

    private int totalAciertos = 0;
    private final int TOTAL_ITEMS = 9;

    private List<EjercicioItem> itemsParaEscuchar = new ArrayList<>();
    private EjercicioItem itemActual = null;

    private String usuarioID;
    private final String numeroEjercicio = "15";
    private FirebaseFirestore db;
    private List<Integer> idsCompletados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio15);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        ivRegresar.setOnClickListener(v -> mostrarConfirmacionSalida());

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
                reproducirAudio(itemActual.audioRes);
            } else if (!itemsParaEscuchar.isEmpty()) {
                Random random = new Random();
                itemActual = itemsParaEscuchar.remove(random.nextInt(itemsParaEscuchar.size()));
                reproducirAudio(itemActual.audioRes);
            } else {
                Toast.makeText(this, "¡Ya clasificaste todas las imágenes!", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            if (totalAciertos == TOTAL_ITEMS) {
                guardarProgreso();
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudio(R.raw.no_has_terminado);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarConfirmacionSalida();
            }
        });

        cargarProgreso();
    }

    private void cargarProgreso() {
        if (usuarioID.equals("anonimo")) {
            setupGame();
            return;
        }

        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Long> ids = (List<Long>) documentSnapshot.get("idsCompletados");
                        if (ids != null) {
                            for (Long idLong : ids) {
                                idsCompletados.add(idLong.intValue());
                            }
                        }
                    }
                    setupGame();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando progreso", e);
                    setupGame();
                });
    }

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo")) return;

        int porcentaje = (totalAciertos * 100) / TOTAL_ITEMS;

        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("idsCompletados", idsCompletados);
        progreso.put("completado", totalAciertos == TOTAL_ITEMS);

        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .set(progreso, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progreso guardado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar progreso", e));
    }

    private void mostrarConfirmacionSalida() {
        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir?")
                .setMessage("Tu progreso se guardará automáticamente.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    guardarProgreso();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupGame() {
        List<EjercicioItem> allItems = new ArrayList<>();
        itemsParaEscuchar.clear();
        totalAciertos = 0;
        itemActual = null;

        // Fuertes
        allItems.add(new EjercicioItem(R.drawable.selimg_carrito, R.raw.selimg_carrito, true));
        allItems.add(new EjercicioItem(R.drawable.selimg_perro, R.raw.selimg_perro, true));
        allItems.add(new EjercicioItem(R.drawable.selimg_raton, R.raw.selimg_raton, true));
        allItems.add(new EjercicioItem(R.drawable.selimg_rio, R.raw.selimg_rio, true));
        allItems.add(new EjercicioItem(R.drawable.selimg_rueda, R.raw.selimg_rueda, true));

        // Ligeras
        allItems.add(new EjercicioItem(R.drawable.selimg_caracol, R.raw.selimg_caracol, false));
        allItems.add(new EjercicioItem(R.drawable.selimg_corazon, R.raw.selimg_corazon, false));
        allItems.add(new EjercicioItem(R.drawable.selimg_periodico, R.raw.selimg_periodico, false));
        allItems.add(new EjercicioItem(R.drawable.selimg_pirata, R.raw.selimg_pirata, false));

        containerOpciones.removeAllViews();
        containerFuerte.removeAllViews();
        containerLigera.removeAllViews();

        for (EjercicioItem item : allItems) {
            if (idsCompletados.contains(item.imgRes)) {
                totalAciertos++;
                agregarImagenAContenedor(item, item.isFuerte ? containerFuerte : containerLigera);
            } else {
                itemsParaEscuchar.add(item);
                agregarImagenAContenedorOpciones(item);
            }
        }

        containerFuerte.setOnDragListener(new MyDragListener(true));
        containerLigera.setOnDragListener(new MyDragListener(false));
    }

    private void agregarImagenAContenedor(EjercicioItem item, GridLayout targetContainer) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(item.imgRes);
        int size = dpToPx(65);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(8, 8, 8, 8);
        iv.setLayoutParams(params);
        iv.setOnClickListener(v1 -> reproducirAudio(item.audioRes));
        targetContainer.addView(iv);
    }

    private void agregarImagenAContenedorOpciones(EjercicioItem item) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(item.imgRes);
        int size = dpToPx(80);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(10, 0, 10, 0);
        iv.setLayoutParams(lp);
        iv.setTag(item);
        iv.setPadding(5, 5, 5, 5);

        iv.setOnTouchListener((v, event) -> {
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
                    idsCompletados.add(item.imgRes);
                    itemActual = null;

                    reproducirAudio(R.raw.muy_bien);
                    guardarProgreso();

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
