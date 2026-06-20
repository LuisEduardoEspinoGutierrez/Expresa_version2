package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tt2.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio10Activity extends AppCompatActivity {

    private static final String TAG = "Ejercicio10Activity";
    private MediaPlayer mediaPlayer; // Para palabras y efectos
    private MediaPlayer mediaPlayerInstrucciones; // Para instrucciones
    private ChipGroup chipGroupPalabras;
    private SopaDeLetrasView sopa;
    private int totalWords;
    private int wordsFoundCount = 0;

    private String usuarioID;
    private final String numeroEjercicio = "10";
    private FirebaseFirestore db;
    private List<String> palabrasEncontradas = new ArrayList<>();

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio10);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        ImageView ivRegresar = findViewById(R.id.ivRegresar);
        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        sopa = findViewById(R.id.sopaDeLetrasView);
        chipGroupPalabras = findViewById(R.id.chipGroupPalabras);
        Button btnFinalizar = findViewById(R.id.btnFinalizarEje10);

        setupSopa(sopa);

        totalWords = chipGroupPalabras.getChildCount();

        ivRegresar.setOnClickListener(v -> mostrarConfirmacionSalida());

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
                procesarFinalizacionConRecompensa();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                playAudio(R.raw.no_has_terminado);
            }
        });

        sopa.setOnWordFoundListener(word -> {
            if (!palabrasEncontradas.contains(word.toUpperCase())) {
                palabrasEncontradas.add(word.toUpperCase());
                removeWordChip(word);
                wordsFoundCount = palabrasEncontradas.size();
                playWordAudio(word);
                guardarProgreso();

                if (wordsFoundCount == totalWords) {
                    Toast.makeText(this, "¡Felicidades! Has terminado el ejercicio", Toast.LENGTH_LONG).show();
                }
            }
        });

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
                        List<String> palabras = (List<String>) documentSnapshot.get("palabrasEncontradas");
                        if (palabras != null) {
                            palabrasEncontradas = palabras;
                            wordsFoundCount = palabrasEncontradas.size();
                            for (String word : palabrasEncontradas) {
                                removeWordChip(word);
                                sopa.marcarPalabraComoEncontrada(word);
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

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo")) return;
        int porcentaje = (wordsFoundCount * 100) / totalWords;
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("palabrasEncontradas", palabrasEncontradas);
        progreso.put("completado", wordsFoundCount == totalWords);
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
                    Toast.makeText(Ejercicio10Activity.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
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
