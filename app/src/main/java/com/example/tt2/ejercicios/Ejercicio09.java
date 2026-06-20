package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
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

public class Ejercicio09 extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Ejercicio09";
    private int wordsFoundCount = 0;
    private final int totalWords = 15;

    private ChipGroup chipGroupPalabrasEje09;
    private SopaDeLetrasViewDalia sopa;
    private ImageView ivRegresarEje09;
    private Button btnAudioInstruccionesEje09, btnFinalizarEje09;

    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    private String usuarioID;
    private final String numeroEjercicio = "9";
    private FirebaseFirestore db;
    private List<String> palabrasEncontradas = new ArrayList<>();

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

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

        ivRegresarEje09 = findViewById(R.id.ivRegresarEje09);
        btnAudioInstruccionesEje09 = findViewById(R.id.btnAudioInstruccionesEje09);
        btnFinalizarEje09 = findViewById(R.id.btnFinalizarEje09);

        sopa = findViewById(R.id.sopaDeLetrasViewEje09);
        chipGroupPalabrasEje09 = findViewById(R.id.chipGroupPalabrasEje09);

        sopa.setGridSize(9, 20);
        setupSopa(sopa);

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

        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio10);

        ivRegresarEje09.setOnClickListener(this);
        btnAudioInstruccionesEje09.setOnClickListener(this);
        btnFinalizarEje09.setOnClickListener(this);

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
                    Toast.makeText(Ejercicio09.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
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

    private void reproducirAudios(int... audios){
        if(mp != null){
            try { mp.release(); } catch (Exception e){ e.printStackTrace(); }
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
        words.add(new SopaDeLetrasViewDalia.Word("CUADRO", 0, 3, 0, 8));
        words.add(new SopaDeLetrasViewDalia.Word("LADRÓN", 0, 14, 0, 19));
        words.add(new SopaDeLetrasViewDalia.Word("LADRAR", 7, 6, 7, 1));
        words.add(new SopaDeLetrasViewDalia.Word("ESCUADRA", 3, 5, 3, 12));
        words.add(new SopaDeLetrasViewDalia.Word("LADRILLO", 4, 7, 4, 14));
        words.add(new SopaDeLetrasViewDalia.Word("SALAMANDRA", 6, 3, 6, 12));
        words.add(new SopaDeLetrasViewDalia.Word("DROMEDARIO", 8, 0, 8, 9));
        words.add(new SopaDeLetrasViewDalia.Word("COCODRILO", 7, 9, 7, 17));
        words.add(new SopaDeLetrasViewDalia.Word("ALMENDRA", 8, 10, 8, 17));
        words.add(new SopaDeLetrasViewDalia.Word("DRAGÓN", 0, 1, 5, 1));
        words.add(new SopaDeLetrasViewDalia.Word("TALADRO", 0, 0, 6, 0));
        words.add(new SopaDeLetrasViewDalia.Word("PIEDRA", 1, 4, 6, 4));
        words.add(new SopaDeLetrasViewDalia.Word("CUADRADO", 0, 10, 7, 10));
        words.add(new SopaDeLetrasViewDalia.Word("VIDRIO", 1, 13, 6, 18));
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
        if (resId != 0) { playAudio(resId); } else { playAudio(R.raw.muy_bien); }
    }

    private void playAudio(int resId) {
        if (mp != null) { mp.release(); }
        mp = MediaPlayer.create(this, resId);
        if (mp != null) { mp.start(); }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRegresarEje09) { mostrarConfirmacionSalida(); }
        else if (id == R.id.btnAudioInstruccionesEje09) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) { mediaPlayerInstrucciones.seekTo(0); }
                else { mediaPlayerInstrucciones.start(); }
            }
        } else if (id == R.id.btnFinalizarEje09) {
            if (wordsFoundCount == totalWords) {
                procesarFinalizacionConRecompensa();
            } else {
                int faltantes = totalWords - wordsFoundCount;
                Toast.makeText(this, "¡Aún faltan " + faltantes + " palabra(s) por encontrar!", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        }
    }
}