package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio14 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio14";
    ImageView ivRegresarEje14;
    ImageView eje14_ra, eje14_esfera, eje14_raton, eje14_pez,
            eje14_re, eje14_reloj, eje14_telefono, eje14_taza,
            eje14_ri, eje14_pico, eje14_mariquita, eje14_emoji,
            eje14_ro, eje14_robot, eje14_sombrero, eje14_carta,
            eje14_ru, eje14_burro, eje14_pajaro, eje14_rueda;
    Button btnAudioInstruccionesEje14, btnFinalizarEje14;
    private int aciertos = 0;
    private final int TOTAL_ACIERTOS = 6;
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    private String usuarioID;
    private final String numeroEjercicio = "14";
    private FirebaseFirestore db;
    private List<Integer> idsEncontrados = new ArrayList<>();

    @Override
    protected void onDestroy() {
        if (mp != null) {
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
        setContentView(R.layout.activity_ejercicio14);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivRegresarEje14 = findViewById(R.id.ivRegresarEje14);

        eje14_ra = findViewById(R.id.eje14_ra);
        eje14_re = findViewById(R.id.eje14_re);
        eje14_ri = findViewById(R.id.eje14_ri);
        eje14_ro = findViewById(R.id.eje14_ro);
        eje14_ru = findViewById(R.id.eje14_ru);
        eje14_esfera = findViewById(R.id.eje14_esfera);
        eje14_raton = findViewById(R.id.eje14_raton);
        eje14_pez = findViewById(R.id.eje14_pez);
        eje14_reloj = findViewById(R.id.eje14_reloj);
        eje14_telefono = findViewById(R.id.eje14_telefono);
        eje14_taza = findViewById(R.id.eje14_taza);
        eje14_pico = findViewById(R.id.eje14_pico);
        eje14_mariquita = findViewById(R.id.eje14_mariquita);
        eje14_emoji = findViewById(R.id.eje14_emoji);
        eje14_robot = findViewById(R.id.eje14_robot);
        eje14_sombrero = findViewById(R.id.eje14_sombrero);
        eje14_carta = findViewById(R.id.eje14_carta);
        eje14_burro = findViewById(R.id.eje14_burro);
        eje14_pajaro = findViewById(R.id.eje14_pajaro);
        eje14_rueda = findViewById(R.id.eje14_rueda);

        btnAudioInstruccionesEje14 = findViewById(R.id.btnAudioInstruccionesEje14);
        btnFinalizarEje14 = findViewById(R.id.btnFinalizarEje14);

        ivRegresarEje14.setOnClickListener(this);

        eje14_ra.setOnClickListener(this);
        eje14_re.setOnClickListener(this);
        eje14_ri.setOnClickListener(this);
        eje14_ro.setOnClickListener(this);
        eje14_ru.setOnClickListener(this);
        
        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_eje14);
        btnAudioInstruccionesEje14.setOnClickListener(this);
        btnFinalizarEje14.setOnClickListener(this);

        // CORRECTAS
        configurarDrag(eje14_raton, "ra", R.raw.audio_raton_eje14);
        configurarDrag(eje14_reloj, "re", R.raw.audio_reloj_eje14);
        configurarDrag(eje14_emoji, "ri", R.raw.audio_risa_eje14);
        configurarDrag(eje14_robot, "ro", R.raw.audio_robot_eje14);
        configurarDrag(eje14_burro, "ru", R.raw.audio_burro_eje14);
        configurarDrag(eje14_rueda, "ru", R.raw.audio_rueda_eje14);


        // INCORRECTAS
        configurarDrag(eje14_esfera, "incorrecta", R.raw.audio_esfera_eje14);
        configurarDrag(eje14_pez, "incorrecta", R.raw.audio_pez_eje14);
        configurarDrag(eje14_telefono, "incorrecta", R.raw.audio_telefono_eje14);
        configurarDrag(eje14_taza, "incorrecta", R.raw.audio_taza_eje14);
        configurarDrag(eje14_pico, "incorrecta", R.raw.audio_pico_eje14);
        configurarDrag(eje14_mariquita, "incorrecta", R.raw.audio_mariquita_eje14);
        configurarDrag(eje14_sombrero, "incorrecta", R.raw.audio_sombrero_eje14);
        configurarDrag(eje14_carta, "incorrecta", R.raw.audio_carta_eje14);
        configurarDrag(eje14_pajaro, "incorrecta", R.raw.audio_pajaro_eje14);

        eje14_ra.setOnDragListener(dragListener);
        eje14_re.setOnDragListener(dragListener);
        eje14_ri.setOnDragListener(dragListener);
        eje14_ro.setOnDragListener(dragListener);
        eje14_ru.setOnDragListener(dragListener);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarConfirmacionSalida();
            }
        });

        cargarProgreso();
    }

    private void cargarProgreso() {
        if (usuarioID.equals("anonimo")) return;
        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Long> ids = (List<Long>) documentSnapshot.get("idsEncontrados");
                        if (ids != null) {
                            for (Long idLong : ids) {
                                int id = idLong.intValue();
                                idsEncontrados.add(id);
                                View v = findViewById(id);
                                if (v != null) v.setVisibility(View.INVISIBLE);
                            }
                            aciertos = idsEncontrados.size();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando progreso", e));
    }

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo")) return;
        int porcentaje = (aciertos * 100) / TOTAL_ACIERTOS;
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("idsEncontrados", idsEncontrados);
        progreso.put("completado", aciertos == TOTAL_ACIERTOS);

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

    private final View.OnDragListener dragListener = (v, event) -> {

        if (event.getAction() == DragEvent.ACTION_DROP) {

            View imagenArrastrada = (View) event.getLocalState();
            String silabaImagen = (String) imagenArrastrada.getTag();
            String silabaZona = "";

            if (v.getId() == R.id.eje14_ra) silabaZona = "ra";
            else if (v.getId() == R.id.eje14_re) silabaZona = "re";
            else if (v.getId() == R.id.eje14_ri) silabaZona = "ri";
            else if (v.getId() == R.id.eje14_ro) silabaZona = "ro";
            else if (v.getId() == R.id.eje14_ru) silabaZona = "ru";

            if (silabaImagen.equals(silabaZona)) {
                imagenArrastrada.setVisibility(View.INVISIBLE);
                idsEncontrados.add(imagenArrastrada.getId());
                aciertos = idsEncontrados.size();
                guardarProgreso();

                if (aciertos == TOTAL_ACIERTOS) {
                    reproducirAudios(R.raw.muy_bien, R.raw.felicidades);
                    Toast.makeText(this, "¡Felicidades! Has terminado el ejercicio", Toast.LENGTH_LONG).show();
                } else {
                    reproducirAudios(R.raw.muy_bien);
                }
            } else {
                reproducirAudios(R.raw.intentalo_otra_vez);
            }
        }
        return true;
    };

    private void configurarDrag(ImageView imageView, String silabaCorrecta, int audioRes) {
        imageView.setTag(silabaCorrecta);
        imageView.setOnClickListener(v -> reproducirAudios(audioRes));
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                reproducirAudios(audioRes);
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadow, v, 0);
                return true;
            }
            return false;
        });
    }

    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try { mp.release(); } catch (Exception e) { e.printStackTrace(); }
            mp = null;
        }
        if (audios.length == 0) return;
        reproducirSecuencia(audios, 0);
    }

    private void reproducirSecuencia(int[] audios, int index) {
        mp = MediaPlayer.create(this, audios[index]);
        if (mp == null) return;
        mp.start();
        mp.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.release();
            int siguiente = index + 1;
            if (siguiente < audios.length) {
                reproducirSecuencia(audios, siguiente);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRegresarEje14) {
            mostrarConfirmacionSalida();
        } else if (id == R.id.btnAudioInstruccionesEje14) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (id == R.id.eje14_ra) {
            reproducirAudios(R.raw.audio_ra_eje14);
        } else if (id == R.id.eje14_re) {
            reproducirAudios(R.raw.audio_re_eje14);
        } else if (id == R.id.eje14_ri) {
            reproducirAudios(R.raw.audio_ri_eje14);
        } else if (id == R.id.eje14_ro) {
            reproducirAudios(R.raw.audio_ro_eje14);
        } else if (id == R.id.eje14_ru) {
            reproducirAudios(R.raw.audio_ru_eje14);
        } else if (id == R.id.btnFinalizarEje14) {
            if (aciertos == TOTAL_ACIERTOS) {
                guardarProgreso();
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        }
    }
}