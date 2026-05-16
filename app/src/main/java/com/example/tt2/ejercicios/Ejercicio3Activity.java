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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class Ejercicio3Activity extends AppCompatActivity {

    MediaPlayer mediaPlayer; 
    MediaRecorder recorder;
    String filePath;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    Button btnGrabar, btnDetener, btnSubir;
    private String usuarioID;
    private final String numeroEjercicio = "3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio3);

        // Obtener ID del usuario actual de Firebase Auth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        // 🔐 Configurar lanzador de permisos
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        });

        // Ajuste de márgenes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // BOTÓN REGRESAR
        ImageView btnBack = findViewById(R.id.ivRegresar);
        btnBack.setOnClickListener(v -> finish());

        // TEXTO CON RESALTADO DE "r"
        TextView tvLectura = findViewById(R.id.tvLectura);
        String texto = "Teresa es una niña que está todo el tiempo cuidando un tesoro que le regaló su abuela al morir. El tesoro no es una caja de oro, tampoco un montón de dinero. El tesoro es un corazón de color morado, donde Teresa guarda todos los recuerdos que le dejó su querida abuela al partir.";
        SpannableString spannable = new SpannableString(texto);

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLectura.setText(spannable);

        // BOTÓN ESCUCHAR INSTRUCCIONES
        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        mediaPlayer = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio3);
        btnAudio.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(0);
                } else {
                    mediaPlayer.start();
                }
            }
        });

        // BOTONES DE GRABACIÓN
        btnGrabar = findViewById(R.id.btnGrabar);
        btnDetener = findViewById(R.id.btnDetener);
        btnSubir = findViewById(R.id.btnSubir);

        btnGrabar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnDetener.setOnClickListener(v -> stopRecording());
        btnSubir.setOnClickListener(v -> uploadAudio());
    }

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ejercicio3.mp4");
            filePath = file.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            
            // CONFIGURACIÓN RECOMENDADA (AAC / MPEG_4)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();

            btnGrabar.setEnabled(false);
            btnDetener.setEnabled(true);
            Toast.makeText(this, " Grabando en alta calidad...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                btnGrabar.setEnabled(true);
                btnDetener.setEnabled(false);
                Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadAudio() {
        if (filePath == null) {
            Toast.makeText(this, "Primero graba un audio", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileObj = new File(filePath);
        if (!fileObj.exists()) {
            Toast.makeText(this, "Archivo no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(fileObj);

        // Nueva ruta y nombre de archivo solicitado
        // audios/ejercicio(numero tal)/[usuarioID]_[numeroEjercicico]_[tipoArchivo]_[timestamp].[extension]
        String tipoArchivo = "audio";
        long timestamp = System.currentTimeMillis();
        String extension = "mp4";
        String fileName = usuarioID + "_eje" + numeroEjercicio + "_" + tipoArchivo + "_" + timestamp + "." + extension;
        String folderPath = "audios/ejercicio" + numeroEjercicio + "/";
        
        StorageReference ref = storageRef.child(folderPath + fileName);

        Toast.makeText(this, "Subiendo audio...", Toast.LENGTH_SHORT).show();

        ref.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        Toast.makeText(this, " Audio subido correctamente ✓", Toast.LENGTH_LONG).show();
                        Log.d("EJERCICIO_3", "URL: " + uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EJERCICIO_3", "Error al subir", e);
                    Toast.makeText(this, " Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
