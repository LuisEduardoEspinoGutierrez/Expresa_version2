package com.example.tt2.rewards;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.android.material.card.MaterialCardView;

public class Reward2 extends AppCompatActivity implements View.OnClickListener {

    ImageView ivRegresarRew2,
            ivRew2Esc1, ivRew2Esc2,
            ivRew2C1, ivRew2C2, ivRew2C3, ivRew2C4,
            ivRew2Rp1, ivRew2Rp2,
            ivReward2;
    MaterialCardView cardReward2;
    boolean EscLocked1 = false, EscLocked2 = false, RpLocked1 = false, RpLocked2 = false, CLocked1 = false, CLocked2 = false, CLocked3 = false, CLocked4 = false;
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private String unicornioSeleccionado = "";
    private String trajeSeleccionado = "";

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
        setContentView(R.layout.activity_reward2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardReward2 = findViewById(R.id.cardReward2);

        ivRew2Esc1 = findViewById(R.id.ivRew2Esc1);
        ivReward2 = findViewById(R.id.ivReward2);
        ivRew2Esc2 = findViewById(R.id.ivRew2Esc2);
        ivRew2C1 = findViewById(R.id.ivRew2C1);
        ivRew2C2 = findViewById(R.id.ivRew2C2);
        ivRew2C3 = findViewById(R.id.ivRew2C3);
        ivRew2C4 = findViewById(R.id.ivRew2C4);
        ivRew2Rp1 = findViewById(R.id.ivRew2Rp1);
        ivRew2Rp2 = findViewById(R.id.ivRew2Rp2);
        ivRegresarRew2 = findViewById(R.id.ivRegresarRew2);

        ivRew2Esc1.setOnClickListener(this);
        ivRew2Esc2.setOnClickListener(this);
        ivRew2C1.setOnClickListener(this);
        ivRew2C2.setOnClickListener(this);
        ivRew2C3.setOnClickListener(this);
        ivRew2C4.setOnClickListener(this);
        ivRew2Rp1.setOnClickListener(this);
        ivRew2Rp2.setOnClickListener(this);
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

    private void trajeBallet(){

        switch (unicornioSeleccionado){

            case "reward2_uni1":
                ivReward2.setImageResource(R.drawable.reward2_uni1_r1);
                break;

            case "reward2_uni2":
                ivReward2.setImageResource(R.drawable.reward2_uni2_r1);
                break;

            case "reward2_uni3":
                ivReward2.setImageResource(R.drawable.reward2_uni3_r1);
                break;

            case "reward2_uni4":
                ivReward2.setImageResource(R.drawable.reward2_uni4_r1);
                break;

        }

    }
    private void trajeVestido(){

        switch (unicornioSeleccionado){

            case "reward2_uni1":
                ivReward2.setImageResource(R.drawable.reward2_uni1_r2);
                break;

            case "reward2_uni2":
                ivReward2.setImageResource(R.drawable.reward2_uni2_r2);
                break;

            case "reward2_uni3":
                ivReward2.setImageResource(R.drawable.reward2_uni3_r2);
                break;

            case "reward2_uni4":
                ivReward2.setImageResource(R.drawable.reward2_uni4_r2);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarRew1) {
            finish();
        }
        else if (v.getId() == R.id.ivRew2Esc1) {
            if (EscLocked1) {
                cardReward2.setBackgroundResource(R.drawable.reward2_esc1_unlocked);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Escenario bloqueado");
                builder.setMessage("Este escenario cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    EscLocked1 = true;
                    ivRew2Esc1.setImageResource(R.drawable.reward2_esc1_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
        else if (v.getId() == R.id.ivRew2Esc2) {
            if (EscLocked2) {
                cardReward2.setBackgroundResource(R.drawable.reward2_esc2_unlocked);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Escenario bloqueado");
                builder.setMessage("Este escenario cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    EscLocked2 = true;
                    ivRew2Esc2.setImageResource(R.drawable.reward2_esc2_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
        else if (v.getId() == R.id.ivRew2Rp1) {
            if (RpLocked1) {
                trajeBallet();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Conjunto bloqueado");
                builder.setMessage("Este conjunto cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    RpLocked1 = true;
                    ivRew2Rp1.setImageResource(R.drawable.reward2_r1_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRew2Rp2) {
            if (RpLocked2) {
                trajeVestido();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Conjunto bloqueado");
                builder.setMessage("Este conjunto cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    RpLocked2 = true;
                    ivRew2Rp2.setImageResource(R.drawable.reward2_r2_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRew2C1) {
            if (CLocked1) {
                unicornioSeleccionado = "reward2_uni1";
                ivReward2.setImageResource(R.drawable.reward2_uni1);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Color de cabello bloqueado");
                builder.setMessage("Este color de cabello cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked1 = true;
                    ivRew2C1.setImageResource(R.drawable.reward2_c1);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRew2C2) {
            if (CLocked2) {
                unicornioSeleccionado = "reward2_uni2";
                ivReward2.setImageResource(R.drawable.reward2_uni2);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Color de cabello bloqueado");
                builder.setMessage("Este color de cabello cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked2 = true;
                    ivRew2C2.setImageResource(R.drawable.reward2_c2);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRew2C3) {
            if (CLocked3) {
                unicornioSeleccionado = "reward2_uni3";
                ivReward2.setImageResource(R.drawable.reward2_uni3);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Color de cabello bloqueado");
                builder.setMessage("Este color de cabello cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked3 = true;
                    ivRew2C3.setImageResource(R.drawable.reward2_c3);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRew2C4) {
            if (CLocked4) {
                unicornioSeleccionado = "reward2_uni4";
                ivReward2.setImageResource(R.drawable.reward2_uni4);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Color de cabello bloqueado");
                builder.setMessage("Este color de cabello cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked4 = true;
                    ivRew2C4.setImageResource(R.drawable.reward2_c4);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
    }

}