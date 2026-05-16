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

public class Ejercicio05 extends AppCompatActivity {

    // VISTAS
    ImageView ivRegresarEje05;
    TextView tvLectura;
    Button btnAudioInstruccionesEje05, btnGrabarEje05, btnDetenerEje05, btnSubirEje05;

    // AUDIO
    private MediaPlayer mediaPlayerInstrucciones;
    private MediaRecorder recorder;

    // VARIABLES
    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "5";

    // PERMISOS
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio05);

        // Usuario actual
        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

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
        ivRegresarEje05.setOnClickListener(v -> finish());

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnDetenerEje05.setOnClickListener(v -> stopRecording());

        btnSubirEje05.setOnClickListener(v -> uploadAudio());
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

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ejercicio5.mp4");
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

            btnGrabarEje05.setEnabled(false);
            btnDetenerEje05.setEnabled(true);
            btnSubirEje05.setEnabled(false);
            Toast.makeText(this, "Grabando en alta calidad...", Toast.LENGTH_SHORT).show();
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
                btnGrabarEje05.setEnabled(true);
                btnDetenerEje05.setEnabled(false);
                btnSubirEje05.setEnabled(true);
                Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadAudio() {
        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(this, "Primero graba un audio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubirEje05.setEnabled(false);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(new File(filePath));

        long timestamp = System.currentTimeMillis();
        String fileName = usuarioID + "_eje" + numeroEjercicio + "_audio_" + timestamp + ".mp4";
        String folderPath = "audios/ejercicio" + numeroEjercicio + "/";

        StorageReference ref = storageRef.child(folderPath + fileName);

        Toast.makeText(this, "Subiendo audio...", Toast.LENGTH_SHORT).show();

        ref.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        Toast.makeText(this, "Audio subido correctamente ✓", Toast.LENGTH_LONG).show();
                        Log.d("EJERCICIO_5", "URL: " + uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EJERCICIO_5", "Error al subir", e);
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubirEje05.setEnabled(true);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerInstrucciones != null) {
            mediaPlayerInstrucciones.release();
            mediaPlayerInstrucciones = null;
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}