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

public class Ejercicio06_4 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio06_4";
    private ImageView ivRegresarEje064;
    private TextView tvLecturaEje064;
    private Button btnAudioInstruccionesEje064, btnAudioTrabalenguasEje064, btnGrabarEje064, btnDetenerEje064, btnSubirEje064;
    private ImageButton btnPlayRecordedEje064;
    private CardView cvPlaybackEje064;
    private ProgressBar pbUploadEje064;

    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private MediaPlayer mediaPlayerRecorded;
    private MediaRecorder recorder;

    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "6.4";
    private boolean isUploaded = false;
    private boolean isRecording = false;

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> requestPermissionLauncher;

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
        if (mediaPlayerRecorded != null) {
            mediaPlayerRecorded.release();
            mediaPlayerRecorded = null;
        }
        if (recorder != null) {
            if (isRecording) {
                try { recorder.stop(); } catch (Exception ignored) {}
            }
            recorder.release();
            recorder = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio064);

        db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        });

        ivRegresarEje064 = findViewById(R.id.ivRegresarEje064);
        tvLecturaEje064 = findViewById(R.id.tvLecturaEje064);
        btnAudioInstruccionesEje064 = findViewById(R.id.btnAudioInstruccionesEje064);
        btnAudioTrabalenguasEje064 = findViewById(R.id.btnAudioTrabalenguasEje064);
        btnGrabarEje064 = findViewById(R.id.btnGrabarEje064);
        btnDetenerEje064 = findViewById(R.id.btnDetenerEje064);
        btnSubirEje064 = findViewById(R.id.btnSubirEje064);
        btnPlayRecordedEje064 = findViewById(R.id.btnPlayRecordedEje064);
        cvPlaybackEje064 = findViewById(R.id.cvPlaybackEje064);
        pbUploadEje064 = findViewById(R.id.pbUploadEje064);

        btnDetenerEje064.setEnabled(false);
        btnSubirEje064.setEnabled(false);

        configurarTexto();

        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio7);

        ivRegresarEje064.setOnClickListener(this);
        btnAudioInstruccionesEje064.setOnClickListener(this);
        btnAudioTrabalenguasEje064.setOnClickListener(this);
        btnGrabarEje064.setOnClickListener(this);
        btnDetenerEje064.setOnClickListener(this);
        btnSubirEje064.setOnClickListener(this);
        btnPlayRecordedEje064.setOnClickListener(this);

        cargarAsignacionYProgreso();
    }

    private void configurarTexto() {
        String texto = "R con R de guitarra, \nR con R de barril, \nrueda que rueda, \nla rueda \ndel ferrocarril";
        SpannableString spannable = new SpannableString(texto);
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLecturaEje064.setText(spannable);
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
                            btnGrabarEje064.setEnabled(false);
                            btnGrabarEje064.setText("Completado");
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
            File file = new File(getExternalFilesDir(null), "audio_ejercicio6_4.mp4");
            filePath = file.getAbsolutePath();
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setOutputFile(filePath);

            recorder.setMaxDuration(120000); // 2 minutos
            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                    Toast.makeText(Ejercicio06_4.this, "Límite de 2 minutos alcanzado. Grabación finalizada.", Toast.LENGTH_LONG).show();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;

            btnGrabarEje064.setEnabled(false);
            btnDetenerEje064.setEnabled(true);
            btnSubirEje064.setEnabled(false);
            cvPlaybackEje064.setVisibility(View.GONE);
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
                
                btnGrabarEje064.setEnabled(true);
                btnGrabarEje064.setText("Reintentar");
                btnDetenerEje064.setEnabled(false);
                btnSubirEje064.setEnabled(true);
                cvPlaybackEje064.setVisibility(View.VISIBLE);
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
            btnPlayRecordedEje064.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayerRecorded.setOnCompletionListener(mp -> btnPlayRecordedEje064.setImageResource(android.R.drawable.ic_media_play));
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
        String fileName = usuarioID + "_eje6_4_audio_" + timestamp + ".mp4";
        StorageReference ref = storageRef.child("audios/ejercicio6_4/" + fileName);
        pbUploadEje064.setVisibility(View.VISIBLE);
        pbUploadEje064.setProgress(0);
        btnSubirEje064.setEnabled(false);
        btnGrabarEje064.setEnabled(false);
        ref.putFile(file)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbUploadEje064.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    isUploaded = true;
                    pbUploadEje064.setVisibility(View.GONE);
                    btnGrabarEje064.setText("Completado");
                    Toast.makeText(this, "Audio subido al 100% ✓", Toast.LENGTH_LONG).show();
                    procesarFinalizacionConRecompensa();
                })
                .addOnFailureListener(e -> {
                    btnSubirEje064.setEnabled(true);
                    btnGrabarEje064.setEnabled(true);
                    pbUploadEje064.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show();
                });
    }

    private void procesarFinalizacionConRecompensa() {
        if (usuarioID.equals("anonimo") || !isDataLoaded || idAsignacionActual.isEmpty()) {
            actualizarProgresoFirestore();
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
                    Toast.makeText(Ejercicio06_4.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRegresarEje064) {
            if (isRecording) stopRecording();
            finish();
        }
        else if (id == R.id.btnAudioTrabalenguasEje064) reproducirAudios(R.raw.trabalenguas_eje6_4);
        else if (id == R.id.btnAudioInstruccionesEje064) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) mediaPlayerInstrucciones.seekTo(0);
                else mediaPlayerInstrucciones.start();
            }
        }
        else if (id == R.id.btnGrabarEje064) {
            if (isUploaded) {
                Toast.makeText(this, "Ya has subido un audio para este ejercicio", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording();
            else requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
        else if (id == R.id.btnDetenerEje064) stopRecording();
        else if (id == R.id.btnSubirEje064) uploadAudio();
        else if (id == R.id.btnPlayRecordedEje064) playRecordedAudio();
    }
}
