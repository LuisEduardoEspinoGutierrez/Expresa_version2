package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

public class Ejercicio01 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio01";
    private TextView txtInstruccionesEje01;
    private ImageView eje01_img0, eje01_img1, eje01_img2, eje01_img3, eje01_img4, eje01_img5, eje01_img6, eje01_img7, eje01_img8, eje01_img9, eje01_img10, eje01_img11;
    private ImageView ivRegresarEje01;
    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private Button btn_instrucciones_ejercicio_01, btnFinalizarEje01;
    
    private String usuarioID;
    private final String numeroEjercicio = "1";
    private FirebaseFirestore db;
    
    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

    // Estados de las imágenes correctas (las que contienen el fonema 'r')
    private boolean img2 = false, img4 = false, img5 = false, img6 = false, img7 = false, img9 = false, img10 = false, img11 = false;

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
        setContentView(R.layout.activity_ejercicio01);

        db = FirebaseFirestore.getInstance();

        // Obtener ID del usuario
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

        // Inicializar vistas
        txtInstruccionesEje01 = findViewById(R.id.txtInstruccionesEje01);
        ivRegresarEje01 = findViewById(R.id.ivRegresarEje01);
        btn_instrucciones_ejercicio_01 = findViewById(R.id.btn_instrucciones_ejercicio_01);
        btnFinalizarEje01 = findViewById(R.id.btnFinalizarEje01);

        eje01_img0 = findViewById(R.id.eje01_img0);
        eje01_img1 = findViewById(R.id.eje01_img1);
        eje01_img2 = findViewById(R.id.eje01_img2);
        eje01_img3 = findViewById(R.id.eje01_img3);
        eje01_img4 = findViewById(R.id.eje01_img4);
        eje01_img5 = findViewById(R.id.eje01_img5);
        eje01_img6 = findViewById(R.id.eje01_img6);
        eje01_img7 = findViewById(R.id.eje01_img7);
        eje01_img8 = findViewById(R.id.eje01_img8);
        eje01_img9 = findViewById(R.id.eje01_img9);
        eje01_img10 = findViewById(R.id.eje01_img10);
        eje01_img11 = findViewById(R.id.eje01_img11);

        // Texto de instrucciones con formato HTML
        txtInstruccionesEje01.setText(Html.fromHtml(getString(R.string.ejercicio01_instrucciones), Html.FROM_HTML_MODE_LEGACY));

        // Click listeners
        ivRegresarEje01.setOnClickListener(this);
        btn_instrucciones_ejercicio_01.setOnClickListener(this);
        btnFinalizarEje01.setOnClickListener(this);

        eje01_img0.setOnClickListener(this);
        eje01_img1.setOnClickListener(this);
        eje01_img2.setOnClickListener(this);
        eje01_img3.setOnClickListener(this);
        eje01_img4.setOnClickListener(this);
        eje01_img5.setOnClickListener(this);
        eje01_img6.setOnClickListener(this);
        eje01_img7.setOnClickListener(this);
        eje01_img8.setOnClickListener(this);
        eje01_img9.setOnClickListener(this);
        eje01_img10.setOnClickListener(this);
        eje01_img11.setOnClickListener(this);

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_ejercicio_01);

        // Manejar botón atrás del sistema
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarConfirmacionSalida();
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }

        // 1. Obtener la asignación actual para este ejercicio
        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", usuarioID)
                .whereEqualTo("logicalId", numeroEjercicio)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        idAsignacionActual = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "Asignación actual: " + idAsignacionActual);
                    }
                    // 2. Cargar el progreso de las imágenes
                    cargarProgreso();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo asignación", e);
                    cargarProgreso();
                });
    }

    private void cargarProgreso() {
        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null && data.containsKey("estados")) {
                            Map<String, Boolean> estados = (Map<String, Boolean>) data.get("estados");
                            if (estados != null) {
                                img2 = estados.getOrDefault("img2", false);
                                img4 = estados.getOrDefault("img4", false);
                                img5 = estados.getOrDefault("img5", false);
                                img6 = estados.getOrDefault("img6", false);
                                img7 = estados.getOrDefault("img7", false);
                                img9 = estados.getOrDefault("img9", false);
                                img10 = estados.getOrDefault("img10", false);
                                img11 = estados.getOrDefault("img11", false);
                                actualizarInterfazDesdeProgreso();
                            }
                        }
                    }
                    isDataLoaded = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando progreso", e);
                    isDataLoaded = true;
                });
    }

    private void actualizarInterfazDesdeProgreso() {
        if (img2) eje01_img2.setImageResource(R.drawable.r_r_perro);
        if (img4) eje01_img4.setImageResource(R.drawable.r_r_cerdo);
        if (img5) eje01_img5.setImageResource(R.drawable.r_r_cerrucho);
        if (img6) eje01_img6.setImageResource(R.drawable.r_r_carro);
        if (img7) eje01_img7.setImageResource(R.drawable.r_r_numero);
        if (img9) eje01_img9.setImageResource(R.drawable.r_r_rana);
        if (img10) eje01_img10.setImageResource(R.drawable.r_r_letra);
        if (img11) eje01_img11.setImageResource(R.drawable.r_r_raton);
    }

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo") || !isDataLoaded) return;

        int totalItems = 8;
        int completados = 0;
        if (img2) completados++;
        if (img4) completados++;
        if (img5) completados++;
        if (img6) completados++;
        if (img7) completados++;
        if (img9) completados++;
        if (img10) completados++;
        if (img11) completados++;

        int porcentaje = (completados * 100) / totalItems;

        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        
        Map<String, Boolean> estados = new HashMap<>();
        estados.put("img2", img2);
        estados.put("img4", img4);
        estados.put("img5", img5);
        estados.put("img6", img6);
        estados.put("img7", img7);
        estados.put("img9", img9);
        estados.put("img10", img10);
        estados.put("img11", img11);
        progreso.put("estados", estados);
        progreso.put("completado", porcentaje == 100);

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

    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try {
                mp.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        if (id == R.id.ivRegresarEje01) {
            mostrarConfirmacionSalida();
        } else if (id == R.id.btn_instrucciones_ejercicio_01) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (id == R.id.btnFinalizarEje01) {
            if (verificarCompletado()) {
                procesarFinalizacionConRecompensa();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        } else if (id == R.id.eje01_img0) {
            reproducirAudios(R.raw.r);
        } else if (id == R.id.eje01_img1) {
            reproducirAudios(R.raw.cactus, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img2) {
            eje01_img2.setImageResource(R.drawable.r_r_perro);
            img2 = true;
            procesarAcierto(R.raw.r_perro);
            guardarProgreso();
        } else if (id == R.id.eje01_img3) {
            reproducirAudios(R.raw.n_luna, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img4) {
            eje01_img4.setImageResource(R.drawable.r_r_cerdo);
            img4 = true;
            procesarAcierto(R.raw.cerdito);
            guardarProgreso();
        } else if (id == R.id.eje01_img5) {
            eje01_img5.setImageResource(R.drawable.r_r_cerrucho);
            img5 = true;
            procesarAcierto(R.raw.cerrucho);
            guardarProgreso();
        } else if (id == R.id.eje01_img6) {
            eje01_img6.setImageResource(R.drawable.r_r_carro);
            img6 = true;
            procesarAcierto(R.raw.r_carrito);
            guardarProgreso();
        } else if (id == R.id.eje01_img7) {
            eje01_img7.setImageResource(R.drawable.r_r_numero);
            img7 = true;
            procesarAcierto(R.raw.cuatro);
            guardarProgreso();
        } else if (id == R.id.eje01_img8) {
            reproducirAudios(R.raw.peine, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img9) {
            eje01_img9.setImageResource(R.drawable.r_r_rana);
            img9 = true;
            procesarAcierto(R.raw.r_rana);
            guardarProgreso();
        } else if (id == R.id.eje01_img10) {
            eje01_img10.setImageResource(R.drawable.r_r_letra);
            img10 = true;
            procesarAcierto(R.raw.letra_a);
            guardarProgreso();
        } else if (id == R.id.eje01_img11) {
            eje01_img11.setImageResource(R.drawable.r_r_raton);
            img11 = true;
            procesarAcierto(R.raw.r_raton);
            guardarProgreso();
        }
    }

    private void procesarFinalizacionConRecompensa() {
        if (usuarioID.equals("anonimo") || !isDataLoaded || idAsignacionActual.isEmpty()) {
            Toast.makeText(this, "¡Felicidades, has terminado el ejercicio!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnFinalizarEje01.setEnabled(false);

        // Usar una transacción para asegurar que la recompensa se de una sola vez por asignación específica
        db.runTransaction(transaction -> {
            DocumentReference asigRef = db.collection("pacientes_ejercicios").document(idAsignacionActual);
            DocumentReference userRef = db.collection("usuarios").document(usuarioID);

            DocumentSnapshot asigSnap = transaction.get(asigRef);
            Boolean entregada = asigSnap.getBoolean("recompensaEntregada");

            if (entregada == null || !entregada) {
                // No se ha entregado, la marcamos y sumamos puntos
                transaction.update(asigRef, "recompensaEntregada", true);
                transaction.update(userRef, "puntos", FieldValue.increment(5));
                return true; // Recompensa otorgada ahora
            }
            return false; // Ya se había otorgado
        }).addOnSuccessListener(recompensaOtorgada -> {
            if (recompensaOtorgada) {
                mostrarToastConPuntos("¡Felicidades! Has ganado 5 puntos. Ahora tienes ");
            } else {
                mostrarToastConPuntos("¡Excelente trabajo! Recuerda que ya tienes ");
            }
        }).addOnFailureListener(e -> {
            btnFinalizarEje01.setEnabled(true);
            Log.e(TAG, "Error en transacción de recompensa", e);
            Toast.makeText(this, "Error al procesar recompensa", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Ejercicio01.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Ejercicio01.this, "¡Felicidades, has terminado el ejercicio!", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void procesarAcierto(int audioRes) {
        if (verificarCompletado()) {
            reproducirAudios(audioRes, R.raw.felicidades);
        } else {
            reproducirAudios(audioRes);
        }
    }

    private boolean verificarCompletado() {
        return img2 && img4 && img5 && img6 &&
                img7 && img9 && img10 && img11;
    }
}
