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

public class Ejercicio4Activity extends AppCompatActivity {

    private MediaPlayer mediaPlayerInstrucciones; // Solo para instrucciones
    private MediaPlayer mediaPlayerGrabacion;     // Solo para reproducir grabación
    private MediaRecorder recorder;

    private String filePath;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Button btnGrabar, btnDetener, btnSubir;
    private String usuarioID;
    private final String numeroEjercicio = "4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio4);

        // Usuario actual
        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        // Permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) startRecording();
                    else Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
                });

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón regresar
        ImageView btnBack = findViewById(R.id.ivRegresar);
        btnBack.setOnClickListener(v -> finish());

        // Texto con resaltado de "r"
        TextView tvLectura = findViewById(R.id.tvLectura);
        String texto = "Raúl pega la cara a la ventana del ferrocarril. Mira cómo una mariposa intenta detenerse en un girasol mientras el viento la empuja y queda atrapada en la tela de una araña. Cerca de la estación, un carro ya lo está esperando. Se levanta y corre para bajar del tren, lleva en su mano un arete que encontró en el asiento, con la prisa, se le cae e intenta como un rayo sostenerlo en el aire, pero no lo logra: alcanza a golpearlo y el arete sale disparado, vuela por el aire y cae dentro de un barril lleno de agua sucia. Raúl, decepcionado, sube al auto mientras escucha cómo las ruedas del ferrocarril avanzan lentamente hasta perderse en el horizonte.";
        SpannableString spannable = new SpannableString(texto);
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == 'r' || letra == 'R') {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvLectura.setText(spannable);

        // Botón instrucciones — MediaPlayer propio
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.r_instrucciones_ejercicio3);
        Button btnAudio = findViewById(R.id.btnAudioInstrucciones);
        btnAudio.setOnClickListener(v -> {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        });

        // Botones grabación
        btnGrabar = findViewById(R.id.btnGrabar);
        btnDetener = findViewById(R.id.btnDetener);
        btnSubir = findViewById(R.id.btnSubir);

        btnDetener.setEnabled(false);
        btnSubir.setEnabled(false); // Deshabilitado hasta que haya grabación

        btnGrabar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
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
            File file = new File(getExternalFilesDir(null), "audio_ejercicio4.3gp");
            filePath = file.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);
            recorder.prepare();
            recorder.start();

            btnGrabar.setEnabled(false);
            btnDetener.setEnabled(true);
            btnSubir.setEnabled(false);
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
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
                btnSubir.setEnabled(true); // Ya hay audio listo para subir
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

        btnSubir.setEnabled(false); // Evitar doble subida

        Uri fileUri = Uri.fromFile(new File(filePath));
        long timestamp = System.currentTimeMillis();
        String fileName = usuarioID + "_" + numeroEjercicio + "_audio_" + timestamp + ".3gp";
        String folderPath = "audios/ejercicio" + numeroEjercicio + "/";

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(folderPath + fileName);

        Toast.makeText(this, "Subiendo audio...", Toast.LENGTH_SHORT).show();

        ref.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.d("EJERCICIO_4", "URL: " + uri.toString());
                            Toast.makeText(this, "Audio subido correctamente ✓", Toast.LENGTH_LONG).show();
                            btnSubir.setEnabled(false); // Ya subido, no repetir
                        }))
                .addOnFailureListener(e -> {
                    Log.e("EJERCICIO_4", "Error al subir", e);
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubir.setEnabled(true); // Permitir reintentar
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerInstrucciones != null) { mediaPlayerInstrucciones.release(); mediaPlayerInstrucciones = null; }
        if (mediaPlayerGrabacion != null) { mediaPlayerGrabacion.release(); mediaPlayerGrabacion = null; }
        if (recorder != null) { recorder.release(); recorder = null; }
    }
}