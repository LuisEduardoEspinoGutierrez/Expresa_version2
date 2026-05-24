package com.example.tt2.rewards;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;

import com.example.tt2.R;

public class Reward1 extends AppCompatActivity implements View.OnClickListener {
    ImageView ivRegresarRew1,
            ivEsc1, ivEsc2,
            ivC1, ivC2, ivC3,
            ivRp1, ivRp2,
            ivReward1;
    MaterialCardView cardReward;
    boolean EscLocked1 = false, EscLocked2 = false, RpLocked1 = false, RpLocked2 = false, CLocked1 = false, CLocked2 = false, CLocked3 = false;
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private String dinosaurioSeleccionado = "";
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
        setContentView(R.layout.activity_reward1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardReward = findViewById(R.id.cardReward);

        ivEsc1 = findViewById(R.id.ivEsc1);
        ivReward1 = findViewById(R.id.ivReward1);
        ivEsc2 = findViewById(R.id.ivEsc2);
        ivC1 = findViewById(R.id.ivC1);
        ivC2 = findViewById(R.id.ivC2);
        ivC3 = findViewById(R.id.ivC3);
        ivRp1 = findViewById(R.id.ivRp1);
        ivRp2 = findViewById(R.id.ivRp2);
        ivRegresarRew1 = findViewById(R.id.ivRegresarRew1);

        ivEsc1.setOnClickListener(this);
        ivEsc2.setOnClickListener(this);
        ivC1.setOnClickListener(this);
        ivC2.setOnClickListener(this);
        ivC3.setOnClickListener(this);
        ivRp1.setOnClickListener(this);
        ivRp2.setOnClickListener(this);
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

    private void ponerTraje(){

        switch (dinosaurioSeleccionado){

            case "reward1_din1":
                ivReward1.setImageResource(R.drawable.reward1_din1_r1);
                break;

            case "reward1_din2":
                ivReward1.setImageResource(R.drawable.reward1_din2_r1);
                break;

            case "reward1_din3":
                ivReward1.setImageResource(R.drawable.reward1_din3_r1);
                break;

        }

    }
    private void ponerSudadera(){

        switch (dinosaurioSeleccionado){

            case "reward1_din1":
                ivReward1.setImageResource(R.drawable.reward_din1_r2);
                break;

            case "reward1_din2":
                ivReward1.setImageResource(R.drawable.reward1_din2_r2);
                break;

            case "reward1_din3":
                ivReward1.setImageResource(R.drawable.reward1_din3_r2);
                break;

        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarRew1) {
            finish();
        }
        else if (v.getId() == R.id.ivEsc1) {
            if (EscLocked1) {
                cardReward.setBackgroundResource(R.drawable.reward1_esc1);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Escenario bloqueado");
                builder.setMessage("Este escenario cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    EscLocked1 = true;
                    ivEsc1.setImageResource(R.drawable.reward1_esc1);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
        else if (v.getId() == R.id.ivEsc2) {
            if (EscLocked2) {
                cardReward.setBackgroundResource(R.drawable.reward1_esc2);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Escenario bloqueado");
                builder.setMessage("Este escenario cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    EscLocked2 = true;
                    ivEsc2.setImageResource(R.drawable.reward1_esc2);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
        else if (v.getId() == R.id.ivRp1) {
            if (RpLocked1) {
                ponerTraje();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Conjunto bloqueado");
                builder.setMessage("Este conjunto cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    RpLocked1 = true;
                    ivRp1.setImageResource(R.drawable.reward1_r1_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivRp2) {
            if (RpLocked2) {
                ponerSudadera();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Conjunto bloqueado");
                builder.setMessage("Este conjunto cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    RpLocked2 = true;
                    ivRp2.setImageResource(R.drawable.reward1_r2_unlocked);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivC1) {
            if (CLocked1) {
                dinosaurioSeleccionado = "reward1_din1";
                ivReward1.setImageResource(R.drawable.reward1_din1);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Dinosaurio bloqueado");
                builder.setMessage("Este dinosaurio cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked1 = true;
                    ivC1.setImageResource(R.drawable.reward1_c1);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivC2) {
            if (CLocked2) {
                dinosaurioSeleccionado = "reward1_din2";
                ivReward1.setImageResource(R.drawable.reward1_din2);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Dinosaurio bloqueado");
                builder.setMessage("Este dinosaurio cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked2 = true;
                    ivC2.setImageResource(R.drawable.reward1_c2);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        } else if (v.getId() == R.id.ivC3) {
            if (CLocked3) {
                dinosaurioSeleccionado = "reward1_din3";
                ivReward1.setImageResource(R.drawable.reward1_din3);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Dinosaurio bloqueado");
                builder.setMessage("Este dinosaurio cuesta 10 monedas");

                builder.setPositiveButton("Sí", (dialog, which) -> {
                    CLocked3 = true;
                    ivC3.setImageResource(R.drawable.reward1_c3);
                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        }
    }
}