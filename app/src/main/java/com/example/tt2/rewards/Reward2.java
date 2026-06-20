package com.example.tt2.rewards;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Reward2 extends AppCompatActivity implements View.OnClickListener {

    ImageView ivRegresarRew2,
            ivRew2Esc1, ivRew2Esc2,
            ivRew2C1, ivRew2C2, ivRew2C3, ivRew2C4,
            ivRew2Rp1, ivRew2Rp2,
            ivReward2;
    LinearLayout btnRegresarBarraRew2;
    MaterialCardView cardReward2;
    
    // true = Desbloqueado (Comprado), false = Bloqueado
    boolean EscLocked1 = false, EscLocked2 = false, RpLocked1 = false, RpLocked2 = false, CLocked1 = false, CLocked2 = false, CLocked3 = false, CLocked4 = false;
    
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private String unicornioSeleccionado = "";
    private String trajeSeleccionado = ""; // "conjunto", "vestido" o ""
    private String escenarioSeleccionado = "";

    private FirebaseFirestore db;
    private String currentUserId;
    private long misPuntos = 0;

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

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cargarPuntos();
            cargarProgresoYApariencia();
        } else {
            currentUserId = "anonimo";
        }

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
        btnRegresarBarraRew2 = findViewById(R.id.btnRegresarBarraRew2);

        ivRew2Esc1.setOnClickListener(this);
        ivRew2Esc2.setOnClickListener(this);
        ivRew2C1.setOnClickListener(this);
        ivRew2C2.setOnClickListener(this);
        ivRew2C3.setOnClickListener(this);
        ivRew2C4.setOnClickListener(this);
        ivRew2Rp1.setOnClickListener(this);
        ivRew2Rp2.setOnClickListener(this);
        if (btnRegresarBarraRew2 != null) {
            btnRegresarBarraRew2.setOnClickListener(this);
        } else {
            ivRegresarRew2.setOnClickListener(this);
        }
    }

    private void cargarPuntos() {
        if (currentUserId.equals("anonimo")) return;

        db.collection("usuarios").document(currentUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) return;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Long puntos = documentSnapshot.getLong("puntos");
                        misPuntos = (puntos != null ? puntos : 0);
                    }
                });
    }

    private void cargarProgresoYApariencia() {
        if (currentUserId.equals("anonimo")) return;

        db.collection("recompensas_pacientes").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null && data.containsKey("reward2")) {
                            Map<String, Object> reward2 = (Map<String, Object>) data.get("reward2");
                            if (reward2 != null) {
                                if (reward2.containsKey("unlocked")) {
                                    Map<String, Object> unlocked = (Map<String, Object>) reward2.get("unlocked");
                                    if (unlocked != null) {
                                        if (Boolean.TRUE.equals(unlocked.get("Esc1"))) { EscLocked1 = true; ivRew2Esc1.setImageResource(R.drawable.reward2_esc1_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("Esc2"))) { EscLocked2 = true; ivRew2Esc2.setImageResource(R.drawable.reward2_esc2_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("Rp1"))) { RpLocked1 = true; ivRew2Rp1.setImageResource(R.drawable.reward2_r1_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("Rp2"))) { RpLocked2 = true; ivRew2Rp2.setImageResource(R.drawable.reward2_r2_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("C1"))) { CLocked1 = true; ivRew2C1.setImageResource(R.drawable.reward2_c1); }
                                        if (Boolean.TRUE.equals(unlocked.get("C2"))) { CLocked2 = true; ivRew2C2.setImageResource(R.drawable.reward2_c2); }
                                        if (Boolean.TRUE.equals(unlocked.get("C3"))) { CLocked3 = true; ivRew2C3.setImageResource(R.drawable.reward2_c3); }
                                        if (Boolean.TRUE.equals(unlocked.get("C4"))) { CLocked4 = true; ivRew2C4.setImageResource(R.drawable.reward2_c4); }
                                    }
                                }
                                
                                if (reward2.containsKey("apariencia")) {
                                    Map<String, Object> apariencia = (Map<String, Object>) reward2.get("apariencia");
                                    if (apariencia != null) {
                                        unicornioSeleccionado = (String) apariencia.getOrDefault("unicornio", "");
                                        trajeSeleccionado = (String) apariencia.getOrDefault("traje", "");
                                        escenarioSeleccionado = (String) apariencia.getOrDefault("escenario", "");
                                        aplicarAparienciaActual();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void aplicarAparienciaActual() {
        if (escenarioSeleccionado.equals("Esc1")) cardReward2.setBackgroundResource(R.drawable.reward2_esc1_unlocked);
        else if (escenarioSeleccionado.equals("Esc2")) cardReward2.setBackgroundResource(R.drawable.reward2_esc2_unlocked);

        if (!unicornioSeleccionado.isEmpty()) {
            if (trajeSeleccionado.equals("conjunto")) {
                trajeBallet(false);
            } else if (trajeSeleccionado.equals("vestido")) {
                trajeVestido(false);
            } else {
                if (unicornioSeleccionado.equals("reward2_uni1")) ivReward2.setImageResource(R.drawable.reward2_uni1);
                else if (unicornioSeleccionado.equals("reward2_uni2")) ivReward2.setImageResource(R.drawable.reward2_uni2);
                else if (unicornioSeleccionado.equals("reward2_uni3")) ivReward2.setImageResource(R.drawable.reward2_uni3);
                else if (unicornioSeleccionado.equals("reward2_uni4")) ivReward2.setImageResource(R.drawable.reward2_uni4);
            }
        }
    }

    private void guardarAparienciaTotal() {
        if (currentUserId.equals("anonimo")) return;
        
        Map<String, Object> aparienciaMap = new HashMap<>();
        aparienciaMap.put("unicornio", unicornioSeleccionado);
        aparienciaMap.put("traje", trajeSeleccionado);
        aparienciaMap.put("escenario", escenarioSeleccionado);
        aparienciaMap.put("ultimo_cambio", FieldValue.serverTimestamp());
        
        Map<String, Object> reward2Map = new HashMap<>();
        reward2Map.put("apariencia", aparienciaMap);
        
        Map<String, Object> finalData = new HashMap<>();
        finalData.put("reward2", reward2Map);
        
        db.collection("recompensas_pacientes").document(currentUserId)
                .set(finalData, SetOptions.merge());
    }

    private void comprarArticulo(String idArticulo, Runnable onExito) {
        if (currentUserId.equals("anonimo")) {
            Toast.makeText(this, "Inicia sesión para comprar artículos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (misPuntos >= 10) {
            db.collection("usuarios").document(currentUserId)
                    .update("puntos", FieldValue.increment(-10))
                    .addOnSuccessListener(aVoid -> {
                        Map<String, Object> unlockedMap = new HashMap<>();
                        unlockedMap.put(idArticulo, true);
                        
                        Map<String, Object> reward2Map = new HashMap<>();
                        reward2Map.put("unlocked", unlockedMap);
                        
                        Map<String, Object> finalData = new HashMap<>();
                        finalData.put("reward2", reward2Map);

                        db.collection("recompensas_pacientes").document(currentUserId)
                                .set(finalData, SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> {
                                    onExito.run();
                                    Toast.makeText(this, "¡Artículo desbloqueado!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar el progreso", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al procesar la compra", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Te faltan monedas para comprar este artículo", Toast.LENGTH_SHORT).show();
        }
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

    private void trajeBallet(boolean guardar){
        if (unicornioSeleccionado.isEmpty()) return;
        trajeSeleccionado = "conjunto";
        if (guardar) guardarAparienciaTotal();
        
        switch (unicornioSeleccionado){
            case "reward2_uni1": ivReward2.setImageResource(R.drawable.reward2_uni1_r1); break;
            case "reward2_uni2": ivReward2.setImageResource(R.drawable.reward2_uni2_r1); break;
            case "reward2_uni3": ivReward2.setImageResource(R.drawable.reward2_uni3_r1); break;
            case "reward2_uni4": ivReward2.setImageResource(R.drawable.reward2_uni4_r1); break;
        }
    }

    private void trajeVestido(boolean guardar){
        if (unicornioSeleccionado.isEmpty()) return;
        trajeSeleccionado = "vestido";
        if (guardar) guardarAparienciaTotal();
        
        switch (unicornioSeleccionado){
            case "reward2_uni1": ivReward2.setImageResource(R.drawable.reward2_uni1_r2); break;
            case "reward2_uni2": ivReward2.setImageResource(R.drawable.reward2_uni2_r2); break;
            case "reward2_uni3": ivReward2.setImageResource(R.drawable.reward2_uni3_r2); break;
            case "reward2_uni4": ivReward2.setImageResource(R.drawable.reward2_uni4_r2); break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegresarBarraRew2 || v.getId() == R.id.ivRegresarRew2) {
            finish();
        }
        else if (v.getId() == R.id.ivRew2Esc1) {
            if (EscLocked1) {
                escenarioSeleccionado = "Esc1";
                cardReward2.setBackgroundResource(R.drawable.reward2_esc1_unlocked);
                guardarAparienciaTotal();
            } else {
                mostrarDialogoCompra("Escenario bloqueado", "Este escenario cuesta 10 monedas", "Esc1", () -> {
                    EscLocked1 = true;
                    ivRew2Esc1.setImageResource(R.drawable.reward2_esc1_unlocked);
                    escenarioSeleccionado = "Esc1";
                    cardReward2.setBackgroundResource(R.drawable.reward2_esc1_unlocked);
                    guardarAparienciaTotal();
                });
            }
        }
        else if (v.getId() == R.id.ivRew2Esc2) {
            if (EscLocked2) {
                escenarioSeleccionado = "Esc2";
                cardReward2.setBackgroundResource(R.drawable.reward2_esc2_unlocked);
                guardarAparienciaTotal();
            } else {
                mostrarDialogoCompra("Escenario bloqueado", "Este escenario cuesta 10 monedas", "Esc2", () -> {
                    EscLocked2 = true;
                    ivRew2Esc2.setImageResource(R.drawable.reward2_esc2_unlocked);
                    escenarioSeleccionado = "Esc2";
                    cardReward2.setBackgroundResource(R.drawable.reward2_esc2_unlocked);
                    guardarAparienciaTotal();
                });
            }
        }
        else if (v.getId() == R.id.ivRew2Rp1) {
            if (RpLocked1) {
                trajeBallet(true);
            } else {
                mostrarDialogoCompra("Conjunto bloqueado", "Este conjunto cuesta 10 monedas", "Rp1", () -> {
                    RpLocked1 = true;
                    ivRew2Rp1.setImageResource(R.drawable.reward2_r1_unlocked);
                    trajeBallet(true);
                });
            }
        } else if (v.getId() == R.id.ivRew2Rp2) {
            if (RpLocked2) {
                trajeVestido(true);
            } else {
                mostrarDialogoCompra("Conjunto bloqueado", "Este conjunto cuesta 10 monedas", "Rp2", () -> {
                    RpLocked2 = true;
                    ivRew2Rp2.setImageResource(R.drawable.reward2_r2_unlocked);
                    trajeVestido(true);
                });
            }
        } else if (v.getId() == R.id.ivRew2C1) {
            if (CLocked1) {
                seleccionarUnicornio("reward2_uni1", R.drawable.reward2_uni1);
            } else {
                mostrarDialogoCompra("Color bloqueado", "Este color cuesta 10 monedas", "C1", () -> {
                    CLocked1 = true;
                    ivRew2C1.setImageResource(R.drawable.reward2_c1);
                    seleccionarUnicornio("reward2_uni1", R.drawable.reward2_uni1);
                });
            }
        } else if (v.getId() == R.id.ivRew2C2) {
            if (CLocked2) {
                seleccionarUnicornio("reward2_uni2", R.drawable.reward2_uni2);
            } else {
                mostrarDialogoCompra("Color bloqueado", "Este color cuesta 10 monedas", "C2", () -> {
                    CLocked2 = true;
                    ivRew2C2.setImageResource(R.drawable.reward2_c2);
                    seleccionarUnicornio("reward2_uni2", R.drawable.reward2_uni2);
                });
            }
        } else if (v.getId() == R.id.ivRew2C3) {
            if (CLocked3) {
                seleccionarUnicornio("reward2_uni3", R.drawable.reward2_uni3);
            } else {
                mostrarDialogoCompra("Color bloqueado", "Este color cuesta 10 monedas", "C3", () -> {
                    CLocked3 = true;
                    ivRew2C3.setImageResource(R.drawable.reward2_c3);
                    seleccionarUnicornio("reward2_uni3", R.drawable.reward2_uni3);
                });
            }
        } else if (v.getId() == R.id.ivRew2C4) {
            if (CLocked4) {
                seleccionarUnicornio("reward2_uni4", R.drawable.reward2_uni4);
            } else {
                mostrarDialogoCompra("Color bloqueado", "Este color cuesta 10 monedas", "C4", () -> {
                    CLocked4 = true;
                    ivRew2C4.setImageResource(R.drawable.reward2_c4);
                    seleccionarUnicornio("reward2_uni4", R.drawable.reward2_uni4);
                });
            }
        }
    }

    private void seleccionarUnicornio(String id, int resId) {
        unicornioSeleccionado = id;
        ivReward2.setImageResource(resId);
        trajeSeleccionado = ""; 
        guardarAparienciaTotal();
    }

    private void mostrarDialogoCompra(String titulo, String mensaje, String idArticulo, Runnable onExito) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton("Sí", (dialog, which) -> {
            comprarArticulo(idArticulo, onExito);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
