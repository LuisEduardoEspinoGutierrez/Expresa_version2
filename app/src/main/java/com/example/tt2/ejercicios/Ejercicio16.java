package com.example.tt2.ejercicios;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ejercicio16 extends AppCompatActivity {

    private static final String TAG = "Ejercicio16";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private ImageView ivPreview;
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerInstructions;
    private String videoPath;
    private View videoContainer;
    private View cardVideoContainer;
    private int mVideoRotation = 0;
    private ScrollView scrollView;

    // CameraX variables
    private PreviewView previewViewCamera;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;
    private View cardCameraOverlay;
    private Button btnIntento; // El de "Ya lo vi..."
    private Button btnComenzar;
    private Button btnDetener;
    private Button btnSubir;
    private View tvStatus;

    // Playback of recorded video
    private TextureView textureViewReproduccion;
    private MediaPlayer mediaPlayerUser;
    private Uri lastSavedUri;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio16);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupWindowInsets();
        initializeViews();
        setupVideoGuide();
        
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void setupWindowInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void initializeViews() {
        ImageView ivRegresar = findViewById(R.id.ivRegresarEje16);
        ivPreview    = findViewById(R.id.ivVideoPreviewEje16);
        textureView  = findViewById(R.id.textureViewEjercicio16);
        videoContainer = findViewById(R.id.videoContainer);
        cardVideoContainer = findViewById(R.id.cardVideoContainer);
        scrollView = findViewById(R.id.scrollViewEje16);
        Button btnAudio = findViewById(R.id.btnAudioInstruccionesEje16);
        
        btnIntento = findViewById(R.id.btnGrabarVideoEje16);
        btnComenzar = findViewById(R.id.btnComenzarGrabacion);
        btnDetener = findViewById(R.id.btnDetenerGrabacion);
        btnSubir = findViewById(R.id.btnSubirVideo);
        
        previewViewCamera = findViewById(R.id.previewViewCameraUser);
        cardCameraOverlay = findViewById(R.id.cardUserCamera);
        textureViewReproduccion = findViewById(R.id.textureViewUserPlayback);
        tvStatus = findViewById(R.id.tvRecordingStatusUser);
        
        Button btnPlay = findViewById(R.id.btnPlayVideoEje16);
        Button btnPause = findViewById(R.id.btnPauseVideoEje16);

        if (ivRegresar != null) ivRegresar.setOnClickListener(v -> finish());

        if (btnAudio != null) {
            btnAudio.setOnClickListener(v -> playInstructions());
        }

        if (btnIntento != null) {
            btnIntento.setOnClickListener(v -> {
                stopInstructions();
                if (allPermissionsGranted()) {
                    startCameraPreview();
                } else {
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }
            });
        }

        if (btnComenzar != null) {
            btnComenzar.setOnClickListener(v -> captureVideo());
        }

        if (btnDetener != null) {
            btnDetener.setOnClickListener(v -> stopRecording());
        }

        if (btnSubir != null) {
            btnSubir.setOnClickListener(v -> {
                if (lastSavedUri != null) {
                    uploadVideo(lastSavedUri);
                }
            });
        }

        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> {
                stopInstructions();
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) mediaPlayer.start();
            });
        }

        if (btnPause != null) {
            btnPause.setOnClickListener(v -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            });
        }
    }

    private void playInstructions() {
        stopInstructions();
        mediaPlayerInstructions = MediaPlayer.create(this, R.raw.instrucciones_video_modelo);
        if (mediaPlayerInstructions != null) {
            mediaPlayerInstructions.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayerInstructions = null;
            });
            mediaPlayerInstructions.start();
        }
    }

    private void stopInstructions() {
        if (mediaPlayerInstructions != null) {
            if (mediaPlayerInstructions.isPlaying()) {
                mediaPlayerInstructions.stop();
            }
            mediaPlayerInstructions.release();
            mediaPlayerInstructions = null;
        }
    }

    private void setupVideoGuide() {
        videoPath = "android.resource://" + getPackageName() + "/" + R.raw.r_sonido;
        if (ivPreview != null) {
            ivPreview.setVisibility(View.VISIBLE);
            ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        setInitialPreviewAndSize();

        if (textureView != null) {
            textureView.setOpaque(true);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                    setupMediaPlayer(new Surface(surfaceTexture));
                }
                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}
                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    return true;
                }
                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
            });
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void startCameraPreview() {
        if (cardCameraOverlay != null) cardCameraOverlay.setVisibility(View.VISIBLE);
        if (previewViewCamera != null) previewViewCamera.setVisibility(View.VISIBLE);
        if (textureViewReproduccion != null) textureViewReproduccion.setVisibility(View.GONE);
        
        if (btnIntento != null) btnIntento.setVisibility(View.GONE);
        if (btnComenzar != null) btnComenzar.setVisibility(View.VISIBLE);
        if (btnDetener != null) btnDetener.setVisibility(View.GONE);
        if (btnSubir != null) btnSubir.setVisibility(View.GONE);

        // Desplazar hacia abajo para ver cámara y video automáticamente
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }

        // Asegurarse de que el video de guía continúe o se inicie
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (cameraProvider == null) return;

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewViewCamera.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                    Toast.makeText(this, "Error al conectar con la cámara", Toast.LENGTH_SHORT).show();
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error getting camera provider", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("MissingPermission")
    private void captureVideo() {
        if (this.videoCapture == null) return;

        if (btnComenzar != null) btnComenzar.setVisibility(View.GONE);
        if (btnDetener != null) {
            btnDetener.setVisibility(View.VISIBLE);
            btnDetener.setEnabled(false);
        }

        long timeStamp = System.currentTimeMillis();
        String name = "Ejercicio16_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(timeStamp);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Expresa-Ejercicios");

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        recording = videoCapture.getOutput()
                .prepareRecording(this, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), recordEvent -> {
                    if (recordEvent instanceof VideoRecordEvent.Start) {
                        if (btnDetener != null) btnDetener.setEnabled(true);
                        if (tvStatus != null) tvStatus.setVisibility(View.VISIBLE);
                    } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                        if (!finalizeEvent.hasError()) {
                            lastSavedUri = finalizeEvent.getOutputResults().getOutputUri();
                            Toast.makeText(getBaseContext(), "Grabación finalizada.", Toast.LENGTH_SHORT).show();
                            
                            showRecordedVideo(lastSavedUri);
                            
                            if (btnDetener != null) btnDetener.setVisibility(View.GONE);
                            if (btnSubir != null) btnSubir.setVisibility(View.VISIBLE);
                        } else {
                            if (recording != null) {
                                recording.close();
                                recording = null;
                            }
                            Log.e(TAG, "Video capture ends with error: " + finalizeEvent.getError());
                            if (btnDetener != null) btnDetener.setVisibility(View.GONE);
                            if (btnComenzar != null) btnComenzar.setVisibility(View.VISIBLE);
                        }
                        if (tvStatus != null) tvStatus.setVisibility(View.GONE);
                    }
                });
    }

    private void showRecordedVideo(Uri videoUri) {
        if (previewViewCamera != null) previewViewCamera.setVisibility(View.GONE);
        if (textureViewReproduccion != null) {
            textureViewReproduccion.setVisibility(View.VISIBLE);
            if (textureViewReproduccion.isAvailable()) {
                setupUserMediaPlayer(new Surface(textureViewReproduccion.getSurfaceTexture()), videoUri);
            } else {
                textureViewReproduccion.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                        setupUserMediaPlayer(new Surface(surface), videoUri);
                    }
                    @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}
                    @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                        if (mediaPlayerUser != null) {
                            mediaPlayerUser.release();
                            mediaPlayerUser = null;
                        }
                        return true;
                    }
                    @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
                });
            }
        }
    }

    private void setupUserMediaPlayer(Surface surface, Uri videoUri) {
        try {
            if (mediaPlayerUser != null) {
                mediaPlayerUser.release();
            }
            mediaPlayerUser = new MediaPlayer();
            mediaPlayerUser.setDataSource(this, videoUri);
            mediaPlayerUser.setSurface(surface);
            mediaPlayerUser.setLooping(true);
            mediaPlayerUser.prepareAsync();
            mediaPlayerUser.setOnPreparedListener(mp -> mp.start());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up user media player", e);
        }
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
        // Pausar el video de guía al finalizar la grabación para que el paciente vea su propio video
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void uploadVideo(Uri videoUri) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnSubir.setEnabled(false);
        Toast.makeText(this, "Subiendo video...", Toast.LENGTH_SHORT).show();

        String userId = mAuth.getCurrentUser().getUid();
        String fileName = "ejercicio16_" + userId + "_" + System.currentTimeMillis() + ".mp4";
        StorageReference storageRef = storage.getReference().child("videos_ejercicios/" + userId + "/" + fileName);

        storageRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(this::saveToFirestore))
                .addOnFailureListener(e -> {
                    btnSubir.setEnabled(true);
                    Toast.makeText(Ejercicio16.this, "Error al subir video: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveToFirestore(Uri downloadUri) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("idPaciente", userId);
        data.put("logicalId", "16");
        data.put("videoUrl", downloadUri.toString());
        data.put("timestamp", Timestamp.now());
        data.put("completado", true);
        data.put("porcentaje", 100);

        db.collection("progreso_ejercicios")
                .document(userId + "_16")
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Ejercicio16.this, "¡Ejercicio guardado con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubir.setEnabled(true);
                    Toast.makeText(Ejercicio16.this, "Error al guardar en BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraPreview();
            } else {
                Toast.makeText(this, "Permisos no concedidos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setInitialPreviewAndSize() {
        new Thread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.r_sonido);
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                if (bitmap != null) {
                    runOnUiThread(() -> {
                        if (ivPreview != null) ivPreview.setImageBitmap(bitmap);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading initial preview", e);
            } finally {
                try { retriever.release(); } catch (Exception ignored) {}
            }
        }).start();
    }

    private void setupMediaPlayer(Surface surface) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, Uri.parse(videoPath));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    if (ivPreview != null) ivPreview.setVisibility(View.GONE);
                    return true;
                }
                return false;
            });

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mp.seekTo(1);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up MediaPlayer", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInstructions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInstructions();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayerUser != null) {
            mediaPlayerUser.release();
            mediaPlayerUser = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
