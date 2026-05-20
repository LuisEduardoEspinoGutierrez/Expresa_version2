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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Ejercicio7_1Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayerInstrucciones;
    private MediaPlayer mediaPlayerRecorded;
    private MediaRecorder recorder;
    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "7_1";
    private boolean isUploaded = false;
    private boolean isRecording = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Button btnGrabar, btnDetener, btnSubir;
    private ImageButton btnPlayRecorded;
    private CardView cvPlayback;
    private ProgressBar pbUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio7_1);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.ivRegresar);
        btnBack.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            }
            finish();
        });

        TextView tvLectura = findViewById(R.id.tvLectura);
        String texto = "El otro dia me cai del ferrocarril\nal lado de un barril\nEl barril tenia ruedas\nQue raro barril!!\nY con las ruedas\ncai en el barro marron\nFui a mi casa, me bańe rapido\ny dije todo otra vez.";
        SpannableString spannable = new SpannableString(texto);

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLectura.setText(spannable);

        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio7);
        btnAudio.setOnClickListener(v -> {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        });

        btnGrabar = findViewById(R.id.btnGrabar);
        btnDetener = findViewById(R.id.btnDetener);
        btnSubir = findViewById(R.id.btnSubir);
        btnPlayRecorded = findViewById(R.id.btnPlayRecorded);
        cvPlayback = findViewById(R.id.cvPlayback);
        pbUpload = findViewById(R.id.pbUpload);

        btnDetener.setEnabled(false);
        btnSubir.setEnabled(false);

        btnGrabar.setOnClickListener(v -> {
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

        btnDetener.setOnClickListener(v -> stopRecording());
        btnSubir.setOnClickListener(v -> uploadAudio());
        btnPlayRecorded.setOnClickListener(v -> playRecordedAudio());

        checkExistingProgress();
    }

    private void checkExistingProgress() {
        if (usuarioID.equals("anonimo")) return;
        FirebaseFirestore.getInstance().collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long porcentaje = documentSnapshot.getLong("porcentaje");
                        if (porcentaje != null && porcentaje >= 100) {
                            isUploaded = true;
                            btnGrabar.setEnabled(false);
                            btnGrabar.setText("Completado");
                        }
                    }
                });
    }

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ejercicio7_1.mp4");
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
                    Toast.makeText(Ejercicio7_1Activity.this, "Límite de 2 minutos alcanzado. Grabación finalizada.", Toast.LENGTH_LONG).show();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;

            btnGrabar.setEnabled(false);
            btnDetener.setEnabled(true);
            btnSubir.setEnabled(false);
            cvPlayback.setVisibility(View.GONE);
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
                
                btnGrabar.setEnabled(true);
                btnGrabar.setText("Reintentar");
                btnDetener.setEnabled(false);
                btnSubir.setEnabled(true);
                cvPlayback.setVisibility(View.VISIBLE);
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
            btnPlayRecorded.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayerRecorded.setOnCompletionListener(mp -> btnPlayRecorded.setImageResource(android.R.drawable.ic_media_play));
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
        String folderName = "ejercicio7_1";
        String fileName = usuarioID + "_eje7_1_audio_" + timestamp + ".mp4";
        StorageReference ref = storageRef.child("audios/" + folderName + "/" + fileName);

        pbUpload.setVisibility(View.VISIBLE);
        pbUpload.setProgress(0);
        btnSubir.setEnabled(false);
        btnGrabar.setEnabled(false);

        ref.putFile(file)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbUpload.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    isUploaded = true;
                    pbUpload.setVisibility(View.GONE);
                    btnGrabar.setText("Completado");
                    Toast.makeText(this, "Audio subido al 100% ✓", Toast.LENGTH_LONG).show();
                    actualizarProgresoFirestore();
                })
                .addOnFailureListener(e -> {
                    btnSubir.setEnabled(true);
                    btnGrabar.setEnabled(true);
                    pbUpload.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarProgresoFirestore() {
        if (usuarioID.equals("anonimo")) return;
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", 100);
        progreso.put("ultimaModificacion", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .set(progreso);
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
                try { recorder.stop(); } catch (Exception ignored) {}
            }
            recorder.release();
        }
    }
}
