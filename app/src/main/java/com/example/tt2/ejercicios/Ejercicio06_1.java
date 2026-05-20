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

public class Ejercicio06_1 extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivRegresarEje061;
    private TextView tvLecturaEje061;
    private Button btnAudioInstruccionesEje061, btnAudioTrabalenguasEje061, btnGrabarEje061, btnDetenerEje061, btnSubirEje061;
    private ImageButton btnPlayRecordedEje061;
    private CardView cvPlaybackEje061;
    private ProgressBar pbUploadEje061;

    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private MediaPlayer mediaPlayerRecorded;
    private MediaRecorder recorder;

    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "6_1";
    private boolean isUploaded = false;
    private boolean isRecording = false;

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
        setContentView(R.layout.activity_ejercicio06_1);

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

        ivRegresarEje061 = findViewById(R.id.ivRegresarEje061);
        tvLecturaEje061 = findViewById(R.id.tvLecturaEje061);
        btnAudioInstruccionesEje061 = findViewById(R.id.btnAudioInstruccionesEje061);
        btnAudioTrabalenguasEje061 = findViewById(R.id.btnAudioTrabalenguasEje061);
        btnGrabarEje061 = findViewById(R.id.btnGrabarEje061);
        btnDetenerEje061 = findViewById(R.id.btnDetenerEje061);
        btnSubirEje061 = findViewById(R.id.btnSubirEje061);
        btnPlayRecordedEje061 = findViewById(R.id.btnPlayRecordedEje061);
        cvPlaybackEje061 = findViewById(R.id.cvPlaybackEje061);
        pbUploadEje061 = findViewById(R.id.pbUploadEje061);

        btnDetenerEje061.setEnabled(false);
        btnSubirEje061.setEnabled(false);

        configurarTexto();

        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio7);

        ivRegresarEje061.setOnClickListener(this);
        btnAudioInstruccionesEje061.setOnClickListener(this);
        btnAudioTrabalenguasEje061.setOnClickListener(this);
        btnGrabarEje061.setOnClickListener(this);
        btnDetenerEje061.setOnClickListener(this);
        btnSubirEje061.setOnClickListener(this);
        btnPlayRecordedEje061.setOnClickListener(this);

        checkExistingProgress();
    }

    private void configurarTexto() {
        String texto = "El moro enamorado de la mora que mira \ndesde el muro la morada del gran Moro, \nespera la hora que la mora desde el muro \nde la casa del moro lo mire enamorada.";
        SpannableString spannable = new SpannableString(texto);
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLecturaEje061.setText(spannable);
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
                            btnGrabarEje061.setEnabled(false);
                            btnGrabarEje061.setText("Completado");
                        }
                    }
                });
    }

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ejercicio6_1.mp4");
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
                    Toast.makeText(Ejercicio06_1.this, "Límite de 2 minutos alcanzado.", Toast.LENGTH_LONG).show();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;
            
            btnGrabarEje061.setEnabled(false);
            btnDetenerEje061.setEnabled(true);
            btnSubirEje061.setEnabled(false);
            cvPlaybackEje061.setVisibility(View.GONE);
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
                
                btnGrabarEje061.setEnabled(true);
                btnGrabarEje061.setText("Reintentar");
                btnDetenerEje061.setEnabled(false);
                btnSubirEje061.setEnabled(true);
                cvPlaybackEje061.setVisibility(View.VISIBLE);
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
            btnPlayRecordedEje061.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayerRecorded.setOnCompletionListener(mp -> btnPlayRecordedEje061.setImageResource(android.R.drawable.ic_media_play));
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
        String fileName = usuarioID + "_eje6_1_audio_" + timestamp + ".mp4";
        StorageReference ref = storageRef.child("audios/ejercicio6.1/" + fileName);
        pbUploadEje061.setVisibility(View.VISIBLE);
        pbUploadEje061.setProgress(0);
        btnSubirEje061.setEnabled(false);
        btnGrabarEje061.setEnabled(false);
        ref.putFile(file)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    pbUploadEje061.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    isUploaded = true;
                    pbUploadEje061.setVisibility(View.GONE);
                    btnGrabarEje061.setText("Completado");
                    Toast.makeText(this, "Audio subido al 100% ✓", Toast.LENGTH_LONG).show();
                    actualizarProgresoFirestore();
                })
                .addOnFailureListener(e -> {
                    btnSubirEje061.setEnabled(true);
                    btnGrabarEje061.setEnabled(true);
                    pbUploadEje061.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarProgresoFirestore() {
        if (usuarioID.equals("anonimo")) return;
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", "6.1");
        progreso.put("porcentaje", 100);
        progreso.put("ultimaModificacion", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("progreso_ejercicios")
                .document(usuarioID + "_6.1")
                .set(progreso);
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
        if (id == R.id.ivRegresarEje061) {
            if (isRecording) stopRecording();
            finish();
        }
        else if (id == R.id.btnAudioTrabalenguasEje061) reproducirAudios(R.raw.trabalenguas_eje6_1);
        else if (id == R.id.btnAudioInstruccionesEje061) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) mediaPlayerInstrucciones.seekTo(0);
                else mediaPlayerInstrucciones.start();
            }
        }
        else if (id == R.id.btnGrabarEje061) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording();
            else requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
        else if (id == R.id.btnDetenerEje061) stopRecording();
        else if (id == R.id.btnSubirEje061) uploadAudio();
        else if (id == R.id.btnPlayRecordedEje061) playRecordedAudio();
    }
}
