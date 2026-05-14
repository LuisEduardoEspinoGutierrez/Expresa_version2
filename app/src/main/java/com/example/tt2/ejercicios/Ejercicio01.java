package com.example.tt2.ejercicios;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;

public class Ejercicio01 extends AppCompatActivity implements View.OnClickListener {

    private TextView txtInstruccionesEje01;
    private ImageView eje01_img0, eje01_img1, eje01_img2, eje01_img3, eje01_img4, eje01_img5, eje01_img6, eje01_img7, eje01_img8, eje01_img9, eje01_img10, eje01_img11;
    private ImageView ivRegresarEje01;
    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private Button btn_instrucciones_ejercicio_01, btnFinalizarEje01;
    
    private String usuarioID;
    private final String numeroEjercicio = "1";

    // Estados de las imágenes correctas (las que contienen el fonema 'r')
    private boolean img2 = false, img4 = false, img5 = false, img6 = false, img7 = false, img9 = false, img10 = false, img11 = false;

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
        setContentView(R.layout.activity_ejercicio01);

        // Obtener ID del usuario
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        txtInstruccionesEje01 = findViewById(R.id.txtInstruccionesEje01);
        ivRegresarEje01 = findViewById(R.id.ivRegresarEje01);
        btn_instrucciones_ejercicio_01 = findViewById(R.id.btn_instrucciones_ejercicio_01);
        btnFinalizarEje01 = findViewById(R.id.btnFinalizarEje01);

        eje01_img0 = findViewById(R.id.eje01_img0);
        eje01_img1 = findViewById(R.id.eje01_img1);
        eje01_img2 = findViewById(R.id.eje01_img2);
        eje01_img3 = findViewById(R.id.eje01_img3);
        eje01_img4 = findViewById(R.id.eje01_img4);
        eje01_img5 = findViewById(R.id.eje01_img5);
        eje01_img6 = findViewById(R.id.eje01_img6);
        eje01_img7 = findViewById(R.id.eje01_img7);
        eje01_img8 = findViewById(R.id.eje01_img8);
        eje01_img9 = findViewById(R.id.eje01_img9);
        eje01_img10 = findViewById(R.id.eje01_img10);
        eje01_img11 = findViewById(R.id.eje01_img11);

        // Texto de instrucciones con formato HTML
        txtInstruccionesEje01.setText(Html.fromHtml(getString(R.string.ejercicio01_instrucciones), Html.FROM_HTML_MODE_LEGACY));

        // Click listeners
        ivRegresarEje01.setOnClickListener(this);
        btn_instrucciones_ejercicio_01.setOnClickListener(this);
        btnFinalizarEje01.setOnClickListener(this);

        eje01_img0.setOnClickListener(this);
        eje01_img1.setOnClickListener(this);
        eje01_img2.setOnClickListener(this);
        eje01_img3.setOnClickListener(this);
        eje01_img4.setOnClickListener(this);
        eje01_img5.setOnClickListener(this);
        eje01_img6.setOnClickListener(this);
        eje01_img7.setOnClickListener(this);
        eje01_img8.setOnClickListener(this);
        eje01_img9.setOnClickListener(this);
        eje01_img10.setOnClickListener(this);
        eje01_img11.setOnClickListener(this);

        // Configurar MediaPlayer para instrucciones
        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_ejercicio_01);
    }

    private void reproducirAudios(int... audios) {
        if (mp != null) {
            try {
                mp.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.ivRegresarEje01) {
            finish();
        } else if (id == R.id.btn_instrucciones_ejercicio_01) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) {
                    mediaPlayerInstrucciones.seekTo(0);
                } else {
                    mediaPlayerInstrucciones.start();
                }
            }
        } else if (id == R.id.btnFinalizarEje01) {
            if (verificarCompletado()) {
                Toast.makeText(this, "Ejercicio guardado correctamente", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Aún faltan imágenes por completar", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        } else if (id == R.id.eje01_img0) {
            reproducirAudios(R.raw.r);
        } else if (id == R.id.eje01_img1) {
            reproducirAudios(R.raw.cactus, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img2) {
            eje01_img2.setImageResource(R.drawable.r_r_perro);
            img2 = true;
            procesarAcierto(R.raw.r_perro);
        } else if (id == R.id.eje01_img3) {
            reproducirAudios(R.raw.n_luna, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img4) {
            eje01_img4.setImageResource(R.drawable.r_r_cerdo);
            img4 = true;
            procesarAcierto(R.raw.cerdito);
        } else if (id == R.id.eje01_img5) {
            eje01_img5.setImageResource(R.drawable.r_r_cerrucho);
            img5 = true;
            procesarAcierto(R.raw.cerrucho);
        } else if (id == R.id.eje01_img6) {
            eje01_img6.setImageResource(R.drawable.r_r_carro);
            img6 = true;
            procesarAcierto(R.raw.r_carrito);
        } else if (id == R.id.eje01_img7) {
            eje01_img7.setImageResource(R.drawable.r_r_numero);
            img7 = true;
            procesarAcierto(R.raw.cuatro);
        } else if (id == R.id.eje01_img8) {
            reproducirAudios(R.raw.peine, R.raw.intentalo_otra_vez);
        } else if (id == R.id.eje01_img9) {
            eje01_img9.setImageResource(R.drawable.r_r_rana);
            img9 = true;
            procesarAcierto(R.raw.r_rana);
        } else if (id == R.id.eje01_img10) {
            eje01_img10.setImageResource(R.drawable.r_r_letra);
            img10 = true;
            procesarAcierto(R.raw.letra_a);
        } else if (id == R.id.eje01_img11) {
            eje01_img11.setImageResource(R.drawable.r_r_raton);
            img11 = true;
            procesarAcierto(R.raw.r_raton);
        }
    }

    private void procesarAcierto(int audioRes) {
        if (verificarCompletado()) {
            Toast.makeText(this, "¡Felicidades, has terminado el ejercicio!", Toast.LENGTH_LONG).show();
            reproducirAudios(audioRes, R.raw.felicidades);
        } else {
            reproducirAudios(audioRes);
        }
    }

    private boolean verificarCompletado() {
        return img2 && img4 && img5 && img6 &&
                img7 && img9 && img10 && img11;
    }
}