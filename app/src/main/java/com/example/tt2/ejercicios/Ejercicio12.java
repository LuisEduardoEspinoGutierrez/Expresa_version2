package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ejercicio12 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio12";
    ImageView ivRegresarEje12, eje12_imgR;
    ImageView eje12_img1, eje12_img2, eje12_img3, eje12_img4, eje12_img5, eje12_img6, eje12_img7, eje12_img8, eje12_img9, eje12_img10;
    Button btnAudioInstruccionesEje12, btnFinalizarEje12;
    FlechaConexionView flechaView;
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    private String usuarioID;
    private final String numeroEjercicio = "12";
    private FirebaseFirestore db;

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

    Set<String> correctas = new HashSet<>(Arrays.asList(
            "img1", // regalo
            "img4", // rayo
            "img5", //raqueta
            "img6", // raton
            "img7", // radio
            "img8", // rey
            "img9" // rinoceronte
    ));

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
        setContentView(R.layout.activity_ejercicio12);

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

        // FindViewById
        ivRegresarEje12       = findViewById(R.id.ivRegresarEje12);
        btnAudioInstruccionesEje12 = findViewById(R.id.btnAudioInstruccionesEje12);
        btnFinalizarEje12     = findViewById(R.id.btnFinalizarEje12);
        eje12_imgR            = findViewById(R.id.eje12_imgR);

        eje12_img1 = findViewById(R.id.eje12_img1);
        eje12_img2 = findViewById(R.id.eje12_img2);
        eje12_img3 = findViewById(R.id.eje12_img3);
        eje12_img4 = findViewById(R.id.eje12_img4);
        eje12_img5 = findViewById(R.id.eje12_img5);
        eje12_img6 = findViewById(R.id.eje12_img6);
        eje12_img7 = findViewById(R.id.eje12_img7);
        eje12_img8 = findViewById(R.id.eje12_img8);
        eje12_img9 = findViewById(R.id.eje12_img9);
        eje12_img10 = findViewById(R.id.eje12_img10);

        flechaView = findViewById(R.id.flechaView);

        // Listener
        flechaView.setOnConexionListener(new FlechaConexionView.OnConexionListener() {
            @Override
            public void onConectado(String id) {
                if (correctas.contains(id)) {
                    flechaView.confirmarLinea();
                    Toast.makeText(Ejercicio12.this, "¡Correcto!", Toast.LENGTH_SHORT).show();
                    reproducirAudios(R.raw.muy_bien);
                    guardarProgreso();
                } else {
                    Toast.makeText(Ejercicio12.this, "Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                    reproducirAudios(R.raw.intentalo_otra_vez);
                }
            }

            @Override
            public void onIncorrecto() {
                Toast.makeText(Ejercicio12.this, "Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
            }
        });

        // Se define la zona de la r
        eje12_imgR.post(() -> {
            RectF rect = new RectF(
                    eje12_imgR.getX(),
                    eje12_imgR.getY(),
                    eje12_imgR.getX() + eje12_imgR.getWidth(),
                    eje12_imgR.getY() + eje12_imgR.getHeight()
            );
            flechaView.setZonaR(rect);
        });

        // Configuracion del drag
        configurarDrag(eje12_img1, "img1");
        configurarDrag(eje12_img2, "img2");
        configurarDrag(eje12_img3, "img3");
        configurarDrag(eje12_img4, "img4");
        configurarDrag(eje12_img5, "img5");
        configurarDrag(eje12_img6, "img6");
        configurarDrag(eje12_img7, "img7");
        configurarDrag(eje12_img8, "img8");
        configurarDrag(eje12_img9, "img9");
        configurarDrag(eje12_img10, "img10");

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_eje12);

        // Clicks normales
        ivRegresarEje12.setOnClickListener(this);
        btnAudioInstruccionesEje12.setOnClickListener(this);
        btnFinalizarEje12.setOnClickListener(this);

        // Audios de las imagenes
        eje12_img1.setOnClickListener(this);
        eje12_img2.setOnClickListener(this);
        eje12_img3.setOnClickListener(this);
        eje12_img4.setOnClickListener(this);
        eje12_img5.setOnClickListener(this);
        eje12_img6.setOnClickListener(this);
        eje12_img7.setOnClickListener(this);
        eje12_img8.setOnClickListener(this);
        eje12_img9.setOnClickListener(this);
        eje12_img10.setOnClickListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarConfirmacionSalida();
            }
        });

        cargarAsignacionYProgreso();
    }

    private void cargarAsignacionYProgreso() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }

        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", usuarioID)
                .whereEqualTo("logicalId", numeroEjercicio)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        idAsignacionActual = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "Asignación actual: " + idAsignacionActual);
                    }
                    cargarProgreso();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo asignación", e);
                    cargarProgreso();
                });
    }

    private void cargarProgreso() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }
        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> conectados = (List<String>) documentSnapshot.get("conectados");
                        List<Map<String, Double>> lineasMap = (List<Map<String, Double>>) documentSnapshot.get("lineas");
                        
                        if (conectados != null && lineasMap != null) {
                            Set<String> conectadosSet = new HashSet<>(conectados);
                            List<float[]> lineasFloat = new ArrayList<>();
                            for (Map<String, Double> m : lineasMap) {
                                lineasFloat.add(new float[]{
                                        m.get("x1").floatValue(),
                                        m.get("y1").floatValue(),
                                        m.get("x2").floatValue(),
                                        m.get("y2").floatValue()
                                });
                            }
                            flechaView.restaurarEstado(conectadosSet, lineasFloat);
                        }
                    }
                    isDataLoaded = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando progreso", e);
                    isDataLoaded = true;
                });
    }

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo")) return;
        
        Set<String> conectados = flechaView.getYaConectados();
        List<float[]> lineas = flechaView.getLineasConfirmadas();
        int porcentaje = (conectados.size() * 100) / correctas.size();

        List<Map<String, Float>> lineasMap = new ArrayList<>();
        for (float[] l : lineas) {
            Map<String, Float> m = new HashMap<>();
            m.put("x1", l[0]); m.put("y1", l[1]);
            m.put("x2", l[2]); m.put("y2", l[3]);
            lineasMap.add(m);
        }

        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("conectados", new ArrayList<>(conectados));
        progreso.put("lineas", lineasMap);
        progreso.put("completado", porcentaje == 100);
        progreso.put("ultimaModificacion", System.currentTimeMillis());

        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .set(progreso, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progreso guardado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar progreso", e));
    }

    private void procesarFinalizacionConRecompensa() {
        if (usuarioID.equals("anonimo") || !isDataLoaded || idAsignacionActual.isEmpty()) {
            guardarProgreso();
            finish();
            return;
        }

        db.runTransaction(transaction -> {
            DocumentReference asigRef = db.collection("pacientes_ejercicios").document(idAsignacionActual);
            DocumentReference userRef = db.collection("usuarios").document(usuarioID);

            DocumentSnapshot asigSnap = transaction.get(asigRef);
            Boolean entregada = asigSnap.getBoolean("recompensaEntregada");

            if (entregada == null || !entregada) {
                transaction.update(asigRef, "recompensaEntregada", true);
                transaction.update(userRef, "puntos", FieldValue.increment(5));
                return true;
            }
            return false;
        }).addOnSuccessListener(recompensaOtorgada -> {
            guardarProgreso();
            if (recompensaOtorgada) {
                mostrarToastConPuntos("¡Felicidades! Has ganado 5 puntos. Ahora tienes ");
            } else {
                mostrarToastConPuntos("¡Excelente trabajo! Recuerda que ya tienes ");
            }
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error en transacción de recompensa", e);
            guardarProgreso();
            finish();
        });
    }

    private void mostrarToastConPuntos(String mensajeBase) {
        db.collection("usuarios").document(usuarioID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long puntos = 0L;
                    if (documentSnapshot.exists()) {
                        puntos = documentSnapshot.getLong("puntos");
                        if (puntos == null) puntos = 0L;
                    }
                    Toast.makeText(Ejercicio12.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
                });
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

    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try { mp.release(); } catch (Exception e) { e.printStackTrace(); }
            mp = null;
        }
        if (audios.length == 0) return;
        reproducirSecuencia(audios, 0);
    }

    private void configurarDrag(ImageView img, String id) {
        img.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int[] location = new int[2];
                flechaView.getLocationOnScreen(location);
                float x = event.getRawX() - location[0];
                float y = event.getRawY() - location[1];

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        isDragging = false;
                        flechaView.iniciarArrastre(id, x, y);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getRawX() - downX);
                        float dy = Math.abs(event.getRawY() - downY);
                        if (dx > 20 || dy > 20) {
                            isDragging = true;
                            flechaView.moverArrastre(x, y);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            flechaView.terminarArrastre(x, y);
                        } else {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
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
        if (v.getId() == R.id.ivRegresarEje12) {
            mostrarConfirmacionSalida();
        } else if (v.getId() == R.id.btnAudioInstruccionesEje12) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (v.getId() == R.id.btnFinalizarEje12) {
            int totalCorrectas = correctas.size();
            int totalConectadas = flechaView.getYaConectados().size();

            if (totalConectadas == totalCorrectas) {
                procesarFinalizacionConRecompensa();
            } else {
                Toast.makeText(this, "Aún faltan palabras", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        } else if (v.getId() == R.id.eje12_imgR) {
            reproducirAudios(R.raw.r);
        } else if (v.getId() == R.id.eje12_img1) {
            reproducirAudios(R.raw.audio_img1_eje12);
        } else if (v.getId() == R.id.eje12_img2) {
            reproducirAudios(R.raw.audio_img2_eje12);
        } else if (v.getId() == R.id.eje12_img3) {
            reproducirAudios(R.raw.audio_img3_eje12);
        } else if (v.getId() == R.id.eje12_img4) {
            reproducirAudios(R.raw.audio_img4_eje12);
        } else if (v.getId() == R.id.eje12_img5) {
            reproducirAudios(R.raw.audio_img5_eje12);
        } else if (v.getId() == R.id.eje12_img6) {
            reproducirAudios(R.raw.audio_img6_eje12);
        } else if (v.getId() == R.id.eje12_img7) {
            reproducirAudios(R.raw.audio_img7_eje12);
        } else if (v.getId() == R.id.eje12_img8) {
            reproducirAudios(R.raw.audio_img8_eje12);
        } else if (v.getId() == R.id.eje12_img9) {
            reproducirAudios(R.raw.audio_img9_eje12);
        } else if (v.getId() == R.id.eje12_img10) {
            reproducirAudios(R.raw.audio_img10_eje12);
        }
    }
}
