package com.example.tt2.ejercicios;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Ejercicio05 extends AppCompatActivity {

    private static final String TAG = "Ejercicio05";
    // VISTAS
    private ImageView ivRegresarEje05;
    private TextView tvLectura;
    private Button btnAudioInstruccionesEje05, btnGrabarEje05, btnDetenerEje05, btnSubirEje05;
    private ImageButton btnPlayRecordedEje05;
    private CardView cvPlaybackEje05;
    private ProgressBar pbUploadEje05;

    // AUDIO
    private MediaPlayer mediaPlayerInstrucciones;
    private MediaPlayer mediaPlayerRecorded;
    private MediaRecorder recorder;

    // VARIABLES
    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "5";
    private boolean isUploaded = false;
    private boolean isRecording = false;

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;
    private FirebaseFirestore db;

    // PERMISOS
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio05);

        db = FirebaseFirestore.getInstance();
        // Usuario actual
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        // Configuración de márgenes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // =========================
        // PERMISOS
        // =========================
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        });

        // =========================
        // ASIGNAR VISTAS XML
        // =========================
        ivRegresarEje05 = findViewById(R.id.ivRegresarEje05);
        tvLectura = findViewById(R.id.tvLecturaEje05);
        btnAudioInstruccionesEje05 = findViewById(R.id.btnAudioInstruccionesEje05);
        btnGrabarEje05 = findViewById(R.id.btnGrabarEje05);
        btnDetenerEje05 = findViewById(R.id.btnDetenerEje05);
        btnSubirEje05 = findViewById(R.id.btnSubirEje05);
        btnPlayRecordedEje05 = findViewById(R.id.btnPlayRecordedEje05);
        cvPlaybackEje05 = findViewById(R.id.cvPlaybackEje05);
        pbUploadEje05 = findViewById(R.id.pbUploadEje05);

        // =========================
        // CONFIGURACIONES INICIALES
        // =========================
        btnDetenerEje05.setEnabled(false);
        btnSubirEje05.setEnabled(false);
        configurarTexto();

        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio3);

        // =========================
        // EVENTOS
        // =========================
        ivRegresarEje05.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            }
            finish();
        });

        btnAudioInstruccionesEje05.setOnClickListener(v -> {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        });

        btnGrabarEje05.setOnClickListener(v -> {
            if (isUploaded) {
                Toast.makeText(this, "Ya has subido un audio para este ejercicio", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnDetenerEje05.setOnClickListener(v -> stopRecording());
        btnSubirEje05.setOnClickListener(v -> uploadAudio());
        btnPlayRecordedEje05.setOnClickListener(v -> playRecordedAudio());

        cargarAsignacionYProgreso();
    }

    private void configurarTexto() {
        String texto = "Había una vez una carroza, abandonada en un barranco, en la que vivían un perro y un burro, llamados Curro y Tarro. \nUn día, mientras Curro limpiaba la tierra de la carroza, Tarro cogió su guitarra e intentó inventar una canción de carreterra. Pero sólo pudo escribir un párrafo porque Curro le pidió que le ayudara a limpiar la tierra de la carretera. \nEntre los dos dejaron la carroza brillante, arreglaron las rudas y comenzaron un viaje en carretera por toda la Tierra.";
        SpannableString spannable = new SpannableString(texto);

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLectura.setText(spannable);
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
                    checkExistingProgress();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo asignación", e);
                    checkExistingProgress();
                });
    }

    private void checkExistingProgress() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }
        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long porcentaje = documentSnapshot.getLong("porcentaje");
                        if (porcentaje != null && porcentaje >= 100) {
                            isUploaded = true;
                            btnGrabarEje05.setEnabled(false);
                            btnGrabarEje05.setText("Completado");
                        }
                    }
                    isDataLoaded = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando progreso", e);
                    isDataLoaded = true;
                });
    }

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ejercicio5.mp4");
            filePath = file.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setOutputFile(filePath);

            // Límite de 2 minutos (120,000 ms)
            recorder.setMaxDuration(120000);
            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                    Toast.makeText(Ejercicio05.this, "Límite de 2 minutos alcanzado. Grabación finalizada.", Toast.LENGTH_LONG).show();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;

            btnGrabarEje05.setEnabled(false);
            btnDetenerEje05.setEnabled(true);
            btnSubirEje05.setEnabled(false);
            cvPlaybackEje05.setVisibility(View.GONE);
            
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null && isRecording) {
                recorder.stop();
                recorder.release();
                recorder = null;
                isRecording = false;

                btnGrabarEje05.setEnabled(true);
                btnGrabarEje05.setText("Reintentar");
                btnDetenerEje05.setEnabled(false);
                btnSubirEje05.setEnabled(true);
                cvPlaybackEje05.setVisibility(View.VISIBLE);
                
                Toast.makeText(this, "Grabación guardada", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playRecordedAudio() {
        if (filePath == null) return;
        try {
            if (mediaPlayerRecorded != null) {
                mediaPlayerRecorded.release();
            }
            mediaPlayerRecorded = new MediaPlayer();
            mediaPlayerRecorded.setDataSource(filePath);
            mediaPlayerRecorded.prepare();
            mediaPlayerRecorded.start();
            btnPlayRecordedEje05.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayerRecorded.setOnCompletionListener(mp -> btnPlayRecordedEje05.setImageResource(android.R.drawable.ic_media_play));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadAudio() {
        if (filePath == null || isUploaded) return;

        File fileObj = new File(filePath);
        if (!fileObj.exists()) return;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(fileObj);

        long timestamp = System.currentTimeMillis();
        String fileName = usuarioID + "_eje" + numeroEjercicio + "_audio_" + timestamp + ".mp4";
        StorageReference ref = storageRef.child("audios/ejercicio" + numeroEjercicio + "/" + fileName);

        pbUploadEje05.setVisibility(View.VISIBLE);
        pbUploadEje05.setProgress(0);
        btnSubirEje05.setEnabled(false);
        btnGrabarEje05.setEnabled(false);

        ref.putFile(file)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbUploadEje05.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    isUploaded = true;
                    pbUploadEje05.setVisibility(View.GONE);
                    btnGrabarEje05.setText("Completado");
                    Toast.makeText(this, "Audio subido al 100% ✓", Toast.LENGTH_LONG).show();
                    procesarFinalizacionConRecompensa();
                })
                .addOnFailureListener(e -> {
                    btnSubirEje05.setEnabled(true);
                    btnGrabarEje05.setEnabled(true);
                    pbUploadEje05.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show();
                });
    }

    private void procesarFinalizacionConRecompensa() {
        if (usuarioID.equals("anonimo") || !isDataLoaded || idAsignacionActual.isEmpty()) {
            actualizarProgresoFirestore();
            return;
        }

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
            actualizarProgresoFirestore();
            if (recompensaOtorgada) {
                mostrarToastConPuntos("¡Felicidades! Has ganado 5 puntos. Ahora tienes ");
            } else {
                mostrarToastConPuntos("¡Excelente trabajo! Recuerda que ya tienes ");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error en transacción de recompensa", e);
            actualizarProgresoFirestore();
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
                    Toast.makeText(Ejercicio05.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarProgresoFirestore() {
        if (usuarioID.equals("anonimo")) return;
        
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", 100);
        progreso.put("completado", true);
        progreso.put("ultimaModificacion", System.currentTimeMillis());

        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .set(progreso, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progreso actualizado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar progreso", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerInstrucciones != null) {
            mediaPlayerInstrucciones.release();
        }
        if (mediaPlayerRecorded != null) {
            mediaPlayerRecorded.release();
        }
        if (recorder != null) {
            if (isRecording) {
                try {
                    recorder.stop();
                } catch (Exception ignored) {}
            }
            recorder.release();
        }
    }
}
