package com.example.tt2.ejercicios;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;

public class Ejercicio16 extends AppCompatActivity {

    private ImageView ivRegresar, ivPreview;
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private Button btnAudio, btnGrabar, btnPlay, btnPause;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio16);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivRegresar = findViewById(R.id.ivRegresarEje16);
        ivPreview = findViewById(R.id.ivVideoPreviewEje16);
        textureView = findViewById(R.id.textureViewEjercicio16);
        btnAudio = findViewById(R.id.btnAudioInstruccionesEje16);
        btnGrabar = findViewById(R.id.btnGrabarVideoEje16);
        btnPlay = findViewById(R.id.btnPlayVideoEje16);
        btnPause = findViewById(R.id.btnPauseVideoEje16);
        
        videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ra;

        // Asegurar que la previsualización sea visible al entrar
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setScaleType(ImageView.ScaleType.FIT_XY);

        // Extraer el primer frame real (tiempo 0) y ajustar tamaño de forma asíncrona
        setInitialPreviewAndSize();

        // Importante para la transparencia en TextureView
        textureView.setOpaque(true);

        ivRegresar.setOnClickListener(v -> finish());

        btnAudio.setOnClickListener(v -> {
            Toast.makeText(this, "Reproduciendo instrucciones...", Toast.LENGTH_SHORT).show();
        });

        btnGrabar.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo cámara para grabar...", Toast.LENGTH_SHORT).show();
        });

        // Controles de video
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                Surface surface = new Surface(surfaceTexture);
                setupMediaPlayer(surface);
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

    /**
     * Extrae el primer frame real del video (microsegundo 0) para evitar que salga en la mitad.
     * También ajusta las dimensiones de ambas vistas para que coincidan con la proporción del video.
     */
    private void setInitialPreviewAndSize() {
        new Thread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.ra);
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                // Extraer el frame en el tiempo 0 exacto (microsegundo 0)
                Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                
                String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                
                if (bitmap != null) {
                    final int vWidth = (widthStr != null) ? Integer.parseInt(widthStr) : bitmap.getWidth();
                    final int vHeight = (heightStr != null) ? Integer.parseInt(heightStr) : bitmap.getHeight();
                    
                    runOnUiThread(() -> {
                        ivPreview.setImageBitmap(bitmap);
                        // Esperar a que la vista esté lista para obtener el tamaño del contenedor y ajustar el aspecto
                        textureView.post(() -> adjustAspectRatio(vWidth, vHeight));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            
            // Listener para ajustar el tamaño del TextureView cuando se conozca el tamaño real del video
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> adjustAspectRatio(width, height));

            // Sincronización perfecta: ocultar preview solo cuando el video realmente comienza a renderizar
            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    ivPreview.setVisibility(View.GONE);
                    return true;
                }
                return false;
            });
            
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                // Se posiciona en el milisegundo 1 para que el primer frame esté listo en el TextureView
                mp.seekTo(1);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajusta el tamaño del TextureView y la previsualización para que coincidan exactamente con la proporción del video.
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        View container = (View) textureView.getParent();
        int viewWidth = container.getWidth();
        int viewHeight = container.getHeight();

        if (viewWidth == 0 || viewHeight == 0) return;

        double videoAspectRatio = (double) videoWidth / videoHeight;
        double containerAspectRatio = (double) viewWidth / viewHeight;

        int newWidth, newHeight;
        if (videoAspectRatio > containerAspectRatio) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth / videoAspectRatio);
        } else {
            newHeight = viewHeight;
            newWidth = (int) (viewHeight * videoAspectRatio);
        }

        // Aplicamos el tamaño exacto al TextureView centrado en su FrameLayout
        if (textureView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textureView.getLayoutParams();
            lp.width = newWidth;
            lp.height = newHeight;
            lp.gravity = Gravity.CENTER;
            textureView.setLayoutParams(lp);
        }

        // Aplicamos el tamaño exacto a la previsualización centrada
        if (ivPreview.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams lpPreview = (FrameLayout.LayoutParams) ivPreview.getLayoutParams();
            lpPreview.width = newWidth;
            lpPreview.height = newHeight;
            lpPreview.gravity = Gravity.CENTER;
            ivPreview.setLayoutParams(lpPreview);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
