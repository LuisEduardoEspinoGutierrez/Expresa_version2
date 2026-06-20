package com.example.tt2.ejercicios;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Random;

public class Ejercicio13 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio13";
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
    private FirebaseFirestore db;
    int contadorIntentos = 0;
    boolean palabraGenerada = false;
    private boolean isRecording = false;

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

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
            if (isRecording) {
                try {
                    recorder.stop();
                } catch (Exception ignored) {}
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
        setContentView(R.layout.activity_ejercicio13);

        db = FirebaseFirestore.getInstance();
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
                        Long giros = documentSnapshot.getLong("totalGiros");
                        Long grabaciones = documentSnapshot.getLong("totalGrabaciones");
                        Long intentos = documentSnapshot.getLong("contadorIntentos");

                        if (giros != null) totalGiros = giros.intValue();
                        if (grabaciones != null) totalGrabaciones = grabaciones.intValue();
                        if (intentos != null) contadorIntentos = intentos.intValue();

                        validarFinalizacion();
                        if (contadorIntentos >= 7) {
                            btnGirar.setEnabled(false);
                            tvResultado.setText("Retoma el ejercicio girando");
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

        int porcentaje = (Math.min(totalGrabaciones, 5) * 100) / 5;

        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("totalGiros", totalGiros);
        progreso.put("totalGrabaciones", totalGrabaciones);
        progreso.put("contadorIntentos", contadorIntentos);
        progreso.put("completado", porcentaje == 100);
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
                    Toast.makeText(Ejercicio13.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
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
                guardarProgreso();
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
                    Toast.makeText(Ejercicio13.this, "Límite de 2 minutos alcanzado. Grabación finalizada.", Toast.LENGTH_LONG).show();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;

            btnGrabarEje13.setEnabled(false);
            btnDetenerEje13.setEnabled(true);
            btnSubirEje13.setEnabled(false);

            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al grabar", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null && isRecording) {
                recorder.stop();
                recorder.release();
                recorder = null;
                isRecording = false;

                contadorIntentos++;
                btnDetenerEje13.setEnabled(false);
                btnSubirEje13.setEnabled(true);
                palabraGenerada = false;
                audioGrabado = true;
                totalGrabaciones++;
                validarFinalizacion();
                guardarProgreso();

                if (contadorIntentos >= 5) {
                    btnGirar.setEnabled(false);
                    btnGrabarEje13.setEnabled(false);
                    tvResultado.setText("Ejercicio terminado");
                    Toast.makeText(this, "Has alcanzado el límite de intentos", Toast.LENGTH_LONG).show();
                } else {
                    btnGirar.setEnabled(true);
                    btnGrabarEje13.setEnabled(false);
                    Toast.makeText(this, "Grabación guardada ✓", Toast.LENGTH_SHORT).show();
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
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir", Toast.LENGTH_LONG).show();
                    btnSubirEje13.setEnabled(true);
                });
    }

    private void validarFinalizacion() {
        if (totalGiros >= 5 && totalGrabaciones >= 5) {
            btnFinalizarEje13.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarEje13) {
            mostrarConfirmacionSalida();
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
            if (totalGiros >= 5 && totalGrabaciones >= 5) {
                procesarFinalizacionConRecompensa();
            } else {
                Toast.makeText(this, "Debes completar al menos 7 giros y grabaciones", Toast.LENGTH_LONG).show();
            }
        }
    }
}
