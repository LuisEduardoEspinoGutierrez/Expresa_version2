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

public class Ejercicio06_1 extends AppCompatActivity implements View.OnClickListener {

    // VISTAS
    ImageView ivRegresarEje061;
    TextView tvLecturaEje061;
    Button btnAudioInstruccionesEje061, btnAudioTrabalenguasEje061, btnGrabarEje061, btnDetenerEje061, btnSubirEje061;

    // AUDIO
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    MediaRecorder recorder;

    // VARIABLES
    private String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "6_1";

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
        if (recorder != null) {
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

        // Usuario actual
        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // =========================
        // PERMISOS
        // =========================
        requestPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                startRecording();
                            } else {
                                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
                            }
                        });

        // =========================
        // ASIGNAR VISTAS XML
        // =========================
        ivRegresarEje061 = findViewById(R.id.ivRegresarEje061);
        tvLecturaEje061 = findViewById(R.id.tvLecturaEje061);

        btnAudioInstruccionesEje061 = findViewById(R.id.btnAudioInstruccionesEje061);
        btnAudioTrabalenguasEje061 = findViewById(R.id.btnAudioTrabalenguasEje061);
        btnGrabarEje061 = findViewById(R.id.btnGrabarEje061);
        btnDetenerEje061 = findViewById(R.id.btnDetenerEje061);
        btnSubirEje061 = findViewById(R.id.btnSubirEje061);

        // =========================
        // CONFIGURACIONES INICIALES
        // =========================
        btnDetenerEje061.setEnabled(false);
        btnSubirEje061.setEnabled(false);

        configurarTexto();

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio7);

        // =========================
        // EVENTOS
        // =========================
        ivRegresarEje061.setOnClickListener(this);

        btnAudioInstruccionesEje061.setOnClickListener(this);
        btnAudioTrabalenguasEje061.setOnClickListener(this);
        btnGrabarEje061.setOnClickListener(this);
        btnDetenerEje061.setOnClickListener(this);
        btnSubirEje061.setOnClickListener(this);
    }

    private void configurarTexto() {

        String texto = "El moro enamorado de la mora que mira \ndesde el muro la morada del gran Moro, \nespera la hora que la mora desde el muro \nde la casa del moro lo mire enamorada.";

        SpannableString spannable = new SpannableString(texto);

        for (int i = 0; i < texto.length(); i++) {

            char letra = texto.charAt(i);

            if (letra == 'r' || letra == 'R') {

                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                spannable.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        i,
                        i + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        tvLecturaEje061.setText(spannable);
    }

    private void startRecording() {

        try {

            File file = new File(
                    getExternalFilesDir(null),
                    "audio_ejercicio6_1_trabalenguas.mp4"
            );

            filePath = file.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            
            // CONFIGURACIÓN AAC / MPEG_4
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();

            btnGrabarEje061.setEnabled(false);
            btnDetenerEje061.setEnabled(true);
            btnSubirEje061.setEnabled(false);

            Toast.makeText(this,
                    "Grabando en alta calidad...",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(this,
                    "Error al iniciar grabación",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {

        try {

            if (recorder != null) {

                recorder.stop();
                recorder.release();
                recorder = null;

                btnGrabarEje061.setEnabled(true);
                btnDetenerEje061.setEnabled(false);
                btnSubirEje061.setEnabled(true);

                Toast.makeText(this,
                        "Grabación detenida",
                        Toast.LENGTH_SHORT).show();
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

        btnSubirEje061.setEnabled(false);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(new File(filePath));

        long timestamp = System.currentTimeMillis();
        String fileName = usuarioID + "_eje" + numeroEjercicio + "_audio_" + timestamp + ".mp4";
        String folderPath = "audios/ejercicio" + numeroEjercicio + "/";

        StorageReference ref = storageRef.child(folderPath + fileName);

        Toast.makeText(this, "Subiendo audio...", Toast.LENGTH_SHORT).show();

        ref.putFile(file)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(this, "Audio subido correctamente ✓", Toast.LENGTH_LONG).show();
                            Log.d("EJERCICIO_6_1", "URL: " + uri.toString());
                        })
                )
                .addOnFailureListener(e -> {
                    Log.e("EJERCICIO_6_1", "Error al subir", e);
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubirEje061.setEnabled(true);
                });
    }

    private void reproducirAudios(int... audios){

        if(mp != null){
            try {
                mp.release();
            } catch (Exception e){
                e.printStackTrace();
            }
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

        if (v.getId() == R.id.ivRegresarEje061) {
            finish();
        } else if (v.getId() == R.id.btnAudioTrabalenguasEje061) {
            reproducirAudios(R.raw.trabalenguas_eje6_1);
        } else if (v.getId() == R.id.btnAudioInstruccionesEje061) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        }

        else if (v.getId() == R.id.btnGrabarEje061) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED) {

                startRecording();

            } else {
                requestPermissionLauncher.launch(
                        Manifest.permission.RECORD_AUDIO
                );
            }
        }

        else if (v.getId() == R.id.btnDetenerEje061) {
            stopRecording();
        }

        else if (v.getId() == R.id.btnSubirEje061) {
            uploadAudio();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
