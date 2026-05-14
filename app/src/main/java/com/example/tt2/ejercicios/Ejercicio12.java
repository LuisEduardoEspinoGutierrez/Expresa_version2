package com.example.tt2.ejercicios;

import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Ejercicio12 extends AppCompatActivity implements View.OnClickListener {

    ImageView ivRegresarEje12, eje12_imgR;
    ImageView eje12_img1, eje12_img2, eje12_img3, eje12_img4, eje12_img5, eje12_img6, eje12_img7, eje12_img8, eje12_img9, eje12_img10;
    Button btnAudioInstruccionesEje12, btnFinalizarEje12;
    FlechaConexionView flechaView;
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    Set<String> correctas = new HashSet<>(Arrays.asList(
            "img1", // regalo
            "img4", // rayo
            "img5", //raqueta
            "img6", // raton
            "img7", // radio
            "img8", // rey
            "img9" // rinoceronte
    ));

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
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio12);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // FindViewById
        ivRegresarEje12       = findViewById(R.id.ivRegresarEje12);
        btnAudioInstruccionesEje12 = findViewById(R.id.btnAudioInstruccionesEje12);
        btnFinalizarEje12     = findViewById(R.id.btnFinalizarEje12);
        eje12_imgR            = findViewById(R.id.eje12_imgR);

        eje12_img1 = findViewById(R.id.eje12_img1);
        eje12_img2 = findViewById(R.id.eje12_img2);
        eje12_img3 = findViewById(R.id.eje12_img3);
        eje12_img4 = findViewById(R.id.eje12_img4);
        eje12_img5 = findViewById(R.id.eje12_img5);
        eje12_img6 = findViewById(R.id.eje12_img6);
        eje12_img7 = findViewById(R.id.eje12_img7);
        eje12_img8 = findViewById(R.id.eje12_img8);
        eje12_img9 = findViewById(R.id.eje12_img9);
        eje12_img10 = findViewById(R.id.eje12_img10);

        flechaView = findViewById(R.id.flechaView);

        // Listener
        flechaView.setOnConexionListener(new FlechaConexionView.OnConexionListener() {
            @Override
            public void onConectado(String id) {

                if (correctas.contains(id)) {
                    flechaView.confirmarLinea(); // 👈 guardar línea
                    Toast.makeText(Ejercicio12.this, "¡Correcto!", Toast.LENGTH_SHORT).show();
                    reproducirAudios(R.raw.muy_bien);
                } else {
                    Toast.makeText(Ejercicio12.this, "Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                    reproducirAudios(R.raw.intentalo_otra_vez);
                }
            }

            @Override
            public void onIncorrecto() {
                Toast.makeText(Ejercicio12.this, "Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
            }
        });

        // Se define la zona de la r
        eje12_imgR.post(() -> {
            RectF rect = new RectF(
                    eje12_imgR.getX(),
                    eje12_imgR.getY(),
                    eje12_imgR.getX() + eje12_imgR.getWidth(),
                    eje12_imgR.getY() + eje12_imgR.getHeight()
            );
            flechaView.setZonaR(rect);
        });

        // Configuracion del drag
        configurarDrag(eje12_img1, "img1");
        configurarDrag(eje12_img2, "img2");
        configurarDrag(eje12_img3, "img3");
        configurarDrag(eje12_img4, "img4");
        configurarDrag(eje12_img5, "img5");
        configurarDrag(eje12_img6, "img6");
        configurarDrag(eje12_img7, "img7");
        configurarDrag(eje12_img8, "img8");
        configurarDrag(eje12_img9, "img9");
        configurarDrag(eje12_img10, "img10");

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_eje12);

        // Clicks normales
        ivRegresarEje12.setOnClickListener(this);
        btnAudioInstruccionesEje12.setOnClickListener(this);
        btnFinalizarEje12.setOnClickListener(this);

        // Audios de las imagenes
        eje12_img1.setOnClickListener(this);
        eje12_img2.setOnClickListener(this);
        eje12_img3.setOnClickListener(this);
        eje12_img4.setOnClickListener(this);
        eje12_img5.setOnClickListener(this);
        eje12_img6.setOnClickListener(this);
        eje12_img7.setOnClickListener(this);
        eje12_img8.setOnClickListener(this);
        eje12_img9.setOnClickListener(this);
        eje12_img10.setOnClickListener(this);
    }


    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try { mp.release(); } catch (Exception e) { e.printStackTrace(); }
            mp = null;
        }
        if (audios.length == 0) return;
        reproducirSecuencia(audios, 0);
    }

    private void configurarDrag(ImageView img, String id) {

        img.setOnTouchListener(new View.OnTouchListener() {

            float downX, downY;
            boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int[] location = new int[2];
                flechaView.getLocationOnScreen(location);

                float x = event.getRawX() - location[0];
                float y = event.getRawY() - location[1];

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        isDragging = false;

                        flechaView.iniciarArrastre(id, x, y);
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float dx = Math.abs(event.getRawX() - downX);
                        float dy = Math.abs(event.getRawY() - downY);

                        if (dx > 20 || dy > 20) {
                            isDragging = true;
                            flechaView.moverArrastre(x, y);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:

                        if (isDragging) {
                            flechaView.terminarArrastre(x, y);
                        } else {
                            // 👇 CLICK normal
                            v.performClick();
                        }
                        return true;
                }

                return false;
            }
        });
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarEje12) {
            finish();
        } else if (v.getId() == R.id.btnAudioInstruccionesEje12) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (v.getId() == R.id.btnFinalizarEje12) {
            int totalCorrectas = correctas.size();
            int totalConectadas = flechaView.getYaConectados().size();

            if (totalConectadas == totalCorrectas) {
                Toast.makeText(this, "¡Ejercicio completado!", Toast.LENGTH_LONG).show();
                reproducirAudios(R.raw.felicidades);
                // Opcional: cerrar pantalla
                finish();

            } else {

                Toast.makeText(this, "Aún faltan palabras", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        } else if (v.getId() == R.id.eje12_imgR)
        {
            reproducirAudios(R.raw.r);
        } else if (v.getId() == R.id.eje12_img1)
        {
            reproducirAudios(R.raw.audio_img1_eje12);
        } else if (v.getId() == R.id.eje12_img2)
        {
            reproducirAudios(R.raw.audio_img2_eje12);
        } else if (v.getId() == R.id.eje12_img3)
        {
            reproducirAudios(R.raw.audio_img3_eje12);
        } else if (v.getId() == R.id.eje12_img4)
        {
            reproducirAudios(R.raw.audio_img4_eje12);
        } else if (v.getId() == R.id.eje12_img5)
        {
            reproducirAudios(R.raw.audio_img5_eje12);
        } else if (v.getId() == R.id.eje12_img6)
        {
            reproducirAudios(R.raw.audio_img6_eje12);
        } else if (v.getId() == R.id.eje12_img7)
        {
            reproducirAudios(R.raw.audio_img7_eje12);
        } else if (v.getId() == R.id.eje12_img8)
        {
            reproducirAudios(R.raw.audio_img8_eje12);
        } else if (v.getId() == R.id.eje12_img9)
        {
            reproducirAudios(R.raw.audio_img9_eje12);
        } else if (v.getId() == R.id.eje12_img10)
        {
            reproducirAudios(R.raw.audio_img10_eje12);
        }
    }
}