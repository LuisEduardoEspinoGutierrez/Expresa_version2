package com.example.tt2.ejercicios;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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
import java.util.Random;

public class Ejercicio13 extends AppCompatActivity implements View.OnClickListener {

    int totalGiros = 0;
    int totalGrabaciones = 0;
    boolean audioGrabado = false;
    ImageView ivRegresarEje13;
    Button btnAudioInstruccionesEje13;
    Button btnGirar;
    ImageView ivRuleta;
    TextView tvResultado;

    float currentRotation = 0f;

    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    MediaRecorder recorder;

    Button btnGrabarEje13, btnDetenerEje13, btnSubirEje13, btnFinalizarEje13;

    String filePath;
    private String usuarioID;
    private final String numeroEjercicio = "13";
    int contadorIntentos = 0;
    boolean palabraGenerada = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onDestroy() {
        if (mp != null) {
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
        setContentView(R.layout.activity_ejercicio13);

        // Usuario actual
        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                startRecording();
                            } else {
                                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                            }
                        });

        ivRegresarEje13 = findViewById(R.id.ivRegresarEje13);
        btnAudioInstruccionesEje13 = findViewById(R.id.btnAudioInstruccionesEje13);
        btnFinalizarEje13 = findViewById(R.id.btnFinalizarEje13);

        ivRuleta = findViewById(R.id.ivRuleta);
        btnGirar = findViewById(R.id.btnGirar);
        tvResultado = findViewById(R.id.tvResultado);

        btnGrabarEje13 = findViewById(R.id.btnGrabarEje13);
        btnDetenerEje13 = findViewById(R.id.btnDetenerEje13);
        btnSubirEje13 = findViewById(R.id.btnSubirEje13);

        btnSubirEje13.setOnClickListener(this);
        btnGirar.setOnClickListener(this);
        btnGrabarEje13.setOnClickListener(this);
        btnDetenerEje13.setOnClickListener(this);
        btnFinalizarEje13.setOnClickListener(this);

        btnDetenerEje13.setEnabled(false);
        btnGrabarEje13.setEnabled(false);
        btnSubirEje13.setEnabled(false);
        btnFinalizarEje13.setEnabled(false);

        ivRegresarEje13.setOnClickListener(this);
        
        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_eje13);
        btnAudioInstruccionesEje13.setOnClickListener(this);
    }

    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try { mp.release(); } catch (Exception e) { e.printStackTrace(); }
            mp = null;
        }
        if (audios.length == 0) return;
        reproducirSecuencia(audios, 0);
    }

    private void reproducirSecuencia(int[] audios, int index) {
        mp = MediaPlayer.create(this, audios[index]);
        if (mp == null) return;
        mp.start();
        mp.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.release();
            int siguiente = index + 1;
            if (siguiente < audios.length) {
                reproducirSecuencia(audios, siguiente);
            }
        });
    }

    private void girarRuleta() {
        Random random = new Random();
        int gradosExtra = random.nextInt(360);
        int rotacion = 3600 + gradosExtra;

        ObjectAnimator animator = ObjectAnimator.ofFloat(
                ivRuleta,
                "rotation",
                currentRotation,
                currentRotation + rotacion
        );

        animator.setDuration(4500);
        animator.setInterpolator(new DecelerateInterpolator());
        totalGiros++;
        audioGrabado = false;
        animator.start();

        currentRotation = (currentRotation + rotacion) % 360;

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                detectarSeccion(currentRotation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void detectarSeccion(float grados) {
        float tamanoSeccion = 360f / 11f;
        float gradosNormalizados = (360 - grados + (tamanoSeccion / 2f)) % 360;
        int seccion = Math.round(gradosNormalizados / tamanoSeccion) % 11;

        switch (seccion) {
            case 0:
                tvResultado.setText("Reloj");
                reproducirAudios(R.raw.audio_reloj_eje13);
                break;
            case 1:
                tvResultado.setText("Zorro");
                reproducirAudios(R.raw.audio_zorro_eje13);
                break;
            case 2:
                tvResultado.setText("Rinoceronte");
                reproducirAudios(R.raw.audio_rinoceronte_eje13);
                break;
            case 3:
                tvResultado.setText("Regla");
                reproducirAudios(R.raw.audio_regla_eje13);
                break;
            case 4:
                tvResultado.setText("Rosa");
                reproducirAudios(R.raw.audio_rosa_eje13);
                break;
            case 5:
                tvResultado.setText("Color rosa");
                reproducirAudios(R.raw.audio_rosa_eje13);
                break;
            case 6:
                tvResultado.setText("Arroz");
                reproducirAudios(R.raw.audio_arroz_eje13);
                break;
            case 7:
                tvResultado.setText("Gorro");
                reproducirAudios(R.raw.audio_gorro_eje13);
                break;
            case 8:
                tvResultado.setText("Ratón");
                reproducirAudios(R.raw.audio_raton_eje13);
                break;
            case 9:
                tvResultado.setText("Barril");
                reproducirAudios(R.raw.audio_barril_eje13);
                break;
            case 10:
                tvResultado.setText("Regadera");
                reproducirAudios(R.raw.audio_regadera_eje13);
                break;
        }
        palabraGenerada = true;
        btnGrabarEje13.setEnabled(true);
        btnSubirEje13.setEnabled(false);
    }

    private void startRecording() {
        try {
            File file = new File(getExternalFilesDir(null), "audio_ruleta_" + contadorIntentos + ".mp4");
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

            btnGrabarEje13.setEnabled(false);
            btnDetenerEje13.setEnabled(true);
            btnSubirEje13.setEnabled(false);

            Toast.makeText(this, "Grabando en alta calidad...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al grabar", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;

                contadorIntentos++;
                btnDetenerEje13.setEnabled(false);
                btnSubirEje13.setEnabled(true);
                palabraGenerada = false;
                audioGrabado = true;
                totalGrabaciones++;
                validarFinalizacion();

                if (contadorIntentos >= 7) {
                    btnGirar.setEnabled(false);
                    btnGrabarEje13.setEnabled(false);
                    tvResultado.setText("Ejercicio terminado");
                    Toast.makeText(this, "Has alcanzado el límite de intentos sugeridos", Toast.LENGTH_LONG).show();
                } else {
                    btnGirar.setEnabled(true);
                    btnGrabarEje13.setEnabled(false);
                    Toast.makeText(this, "Grabación guardada ✓. Intento " + contadorIntentos, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadAudio() {
        if (filePath == null || !new File(filePath).exists()) {
            Toast.makeText(this, "No hay grabación para subir", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubirEje13.setEnabled(false);
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
                        Toast.makeText(this, "Audio subido correctamente ✓", Toast.LENGTH_SHORT).show();
                        Log.d("EJERCICIO_13", "URL: " + uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EJERCICIO_13", "Error al subir", e);
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubirEje13.setEnabled(true);
                });
    }

    private void validarFinalizacion() {
        if (totalGiros >= 7 && totalGrabaciones >= 7) {
            btnFinalizarEje13.setEnabled(true);
            Toast.makeText(this, "¡Ya puedes finalizar!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarEje13) {
            finish();
        } else if (v.getId() == R.id.btnAudioInstruccionesEje13) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (v.getId() == R.id.btnGirar) {
            if (palabraGenerada && !audioGrabado) {
                Toast.makeText(this, "Primero graba la palabra actual", Toast.LENGTH_SHORT).show();
                return;
            }
            girarRuleta();
        } else if (v.getId() == R.id.btnGrabarEje13) {
            if (!palabraGenerada) {
                Toast.makeText(this, "Primero gira la ruleta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        } else if (v.getId() == R.id.btnDetenerEje13) {
            stopRecording();
        } else if (v.getId() == R.id.btnSubirEje13) {
            uploadAudio();
        } else if (v.getId() == R.id.btnFinalizarEje13) {
            if (totalGiros >= 7 && totalGrabaciones >= 7) {
                Toast.makeText(this, "Ejercicio completado", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Debes completar al menos 7 giros y grabaciones", Toast.LENGTH_LONG).show();
            }
        }
    }
}
