package com.example.tt2.rewards;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Reward1 extends AppCompatActivity implements View.OnClickListener {
    ImageView ivRegresarRew1,
            ivEsc1, ivEsc2,
            ivC1, ivC2, ivC3,
            ivRp1, ivRp2,
            ivReward1;
    View btnRegresarBarra;
    MaterialCardView cardReward;
    
    // true = Desbloqueado (Comprado), false = Bloqueado
    boolean EscLocked1 = false, EscLocked2 = false, RpLocked1 = false, RpLocked2 = false, CLocked1 = false, CLocked2 = false, CLocked3 = false;
    
    MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;
    private String dinosaurioSeleccionado = "";
    private String trajeSeleccionado = ""; // "" (ninguno), "conjunto", "sudadera"
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
        setContentView(R.layout.activity_reward1);
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
        btnRegresarBarra = findViewById(R.id.btnRegresarBarra);

        ivEsc1.setOnClickListener(this);
        ivEsc2.setOnClickListener(this);
        ivC1.setOnClickListener(this);
        ivC2.setOnClickListener(this);
        ivC3.setOnClickListener(this);
        ivRp1.setOnClickListener(this);
        ivRp2.setOnClickListener(this);
        ivRegresarRew1.setOnClickListener(this);
        if (btnRegresarBarra != null) {
            btnRegresarBarra.setOnClickListener(this);
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
                        if (data != null && data.containsKey("reward1")) {
                            Map<String, Object> reward1 = (Map<String, Object>) data.get("reward1");
                            if (reward1 != null) {
                                // 1. Cargar artículos desbloqueados
                                if (reward1.containsKey("unlocked")) {
                                    Map<String, Object> unlocked = (Map<String, Object>) reward1.get("unlocked");
                                    if (unlocked != null) {
                                        if (Boolean.TRUE.equals(unlocked.get("Esc1"))) { EscLocked1 = true; ivEsc1.setImageResource(R.drawable.reward1_esc1); }
                                        if (Boolean.TRUE.equals(unlocked.get("Esc2"))) { EscLocked2 = true; ivEsc2.setImageResource(R.drawable.reward1_esc2); }
                                        if (Boolean.TRUE.equals(unlocked.get("Rp1"))) { RpLocked1 = true; ivRp1.setImageResource(R.drawable.reward1_r1_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("Rp2"))) { RpLocked2 = true; ivRp2.setImageResource(R.drawable.reward1_r2_unlocked); }
                                        if (Boolean.TRUE.equals(unlocked.get("C1"))) { CLocked1 = true; ivC1.setImageResource(R.drawable.reward1_c1); }
                                        if (Boolean.TRUE.equals(unlocked.get("C2"))) { CLocked2 = true; ivC2.setImageResource(R.drawable.reward1_c2); }
                                        if (Boolean.TRUE.equals(unlocked.get("C3"))) { CLocked3 = true; ivC3.setImageResource(R.drawable.reward1_c3); }
                                    }
                                }
                                
                                // 2. Cargar apariencia guardada
                                if (reward1.containsKey("apariencia")) {
                                    Map<String, Object> apariencia = (Map<String, Object>) reward1.get("apariencia");
                                    if (apariencia != null) {
                                        dinosaurioSeleccionado = (String) apariencia.getOrDefault("dinosaurio", "");
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
        // Aplicar escenario
        if (escenarioSeleccionado.equals("Esc1")) cardReward.setBackgroundResource(R.drawable.reward1_esc1);
        else if (escenarioSeleccionado.equals("Esc2")) cardReward.setBackgroundResource(R.drawable.reward1_esc2);

        // Aplicar dinosaurio y traje
        if (!dinosaurioSeleccionado.isEmpty()) {
            if (trajeSeleccionado.equals("conjunto")) {
                ponerTraje(false);
            } else if (trajeSeleccionado.equals("sudadera")) {
                ponerSudadera(false);
            } else {
                // Solo dinosaurio base
                if (dinosaurioSeleccionado.equals("reward1_din1")) ivReward1.setImageResource(R.drawable.reward1_din1);
                else if (dinosaurioSeleccionado.equals("reward1_din2")) ivReward1.setImageResource(R.drawable.reward1_din2);
                else if (dinosaurioSeleccionado.equals("reward1_din3")) ivReward1.setImageResource(R.drawable.reward1_din3);
            }
        }
    }

    private void guardarAparienciaTotal() {
        if (currentUserId.equals("anonimo")) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("reward1.apariencia.dinosaurio", dinosaurioSeleccionado);
        updates.put("reward1.apariencia.traje", trajeSeleccionado);
        updates.put("reward1.apariencia.escenario", escenarioSeleccionado);
        updates.put("reward1.apariencia.ultimo_cambio", FieldValue.serverTimestamp());
        
        db.collection("recompensas_pacientes").document(currentUserId)
                .update(updates)
                .addOnFailureListener(e -> {
                    // Si el documento no existe, usamos set con merge
                    Map<String, Object> aparienciaMap = new HashMap<>();
                    aparienciaMap.put("dinosaurio", dinosaurioSeleccionado);
                    aparienciaMap.put("traje", trajeSeleccionado);
                    aparienciaMap.put("escenario", escenarioSeleccionado);
                    aparienciaMap.put("ultimo_cambio", FieldValue.serverTimestamp());
                    Map<String, Object> reward1Map = new HashMap<>();
                    reward1Map.put("apariencia", aparienciaMap);
                    Map<String, Object> finalData = new HashMap<>();
                    finalData.put("reward1", reward1Map);
                    db.collection("recompensas_pacientes").document(currentUserId).set(finalData, SetOptions.merge());
                });
    }

    private void comprarArticulo(String idArticulo, Runnable onExito) {
        if (currentUserId.equals("anonimo")) {
            Toast.makeText(this, "Inicia sesión para comprar artículos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (misPuntos >= 10) {
            // 1. Restar puntos en la colección usuarios
            db.collection("usuarios").document(currentUserId)
                    .update("puntos", FieldValue.increment(-10))
                    .addOnSuccessListener(aVoid -> {
                        // 2. Guardar el objeto desbloqueado en la entidad independiente recompensas_pacientes
                        db.collection("recompensas_pacientes").document(currentUserId)
                                .update("reward1.unlocked." + idArticulo, true)
                                .addOnSuccessListener(aVoid2 -> {
                                    onExito.run();
                                    Toast.makeText(this, "¡Artículo desbloqueado!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Si el documento o el mapa anidado no existen, lo intentamos con set merge
                                    Map<String, Object> nestedMap = new HashMap<>();
                                    nestedMap.put(idArticulo, true);
                                    Map<String, Object> unlockedMap = new HashMap<>();
                                    unlockedMap.put("unlocked", nestedMap);
                                    Map<String, Object> rewardUpdate = new HashMap<>();
                                    rewardUpdate.put("reward1", unlockedMap);
                                    db.collection("recompensas_pacientes").document(currentUserId)
                                            .set(rewardUpdate, SetOptions.merge())
                                            .addOnSuccessListener(aVoid3 -> {
                                                onExito.run();
                                                Toast.makeText(this, "¡Artículo desbloqueado!", Toast.LENGTH_SHORT).show();
                                            });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al procesar la compra", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Te faltan monedas para comprar este artículo", Toast.LENGTH_SHORT).show();
        }
    }

    private void ponerTraje(boolean guardar){
        if (dinosaurioSeleccionado.isEmpty()) return;
        trajeSeleccionado = "conjunto";
        if (guardar) guardarAparienciaTotal();
        
        switch (dinosaurioSeleccionado){
            case "reward1_din1": ivReward1.setImageResource(R.drawable.reward1_din1_r1); break;
            case "reward1_din2": ivReward1.setImageResource(R.drawable.reward1_din2_r1); break;
            case "reward1_din3": ivReward1.setImageResource(R.drawable.reward1_din3_r1); break;
        }
    }

    private void ponerSudadera(boolean guardar){
        if (dinosaurioSeleccionado.isEmpty()) return;
        trajeSeleccionado = "sudadera";
        if (guardar) guardarAparienciaTotal();
        
        switch (dinosaurioSeleccionado){
            case "reward1_din1": ivReward1.setImageResource(R.drawable.reward_din1_r2); break;
            case "reward1_din2": ivReward1.setImageResource(R.drawable.reward1_din2_r2); break;
            case "reward1_din3": ivReward1.setImageResource(R.drawable.reward1_din3_r2); break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivRegresarRew1 || v.getId() == R.id.btnRegresarBarra) {
            finish();
        }
        else if (v.getId() == R.id.ivEsc1) {
            if (EscLocked1) {
                escenarioSeleccionado = "Esc1";
                cardReward.setBackgroundResource(R.drawable.reward1_esc1);
                guardarAparienciaTotal();
            } else {
                mostrarDialogoCompra("Escenario bloqueado", "Este escenario cuesta 10 monedas", "Esc1", () -> {
                    EscLocked1 = true;
                    ivEsc1.setImageResource(R.drawable.reward1_esc1);
                    escenarioSeleccionado = "Esc1";
                    cardReward.setBackgroundResource(R.drawable.reward1_esc1);
                    guardarAparienciaTotal();
                });
            }
        }
        else if (v.getId() == R.id.ivEsc2) {
            if (EscLocked2) {
                escenarioSeleccionado = "Esc2";
                cardReward.setBackgroundResource(R.drawable.reward1_esc2);
                guardarAparienciaTotal();
            } else {
                mostrarDialogoCompra("Escenario bloqueado", "Este escenario cuesta 10 monedas", "Esc2", () -> {
                    EscLocked2 = true;
                    ivEsc2.setImageResource(R.drawable.reward1_esc2);
                    escenarioSeleccionado = "Esc2";
                    cardReward.setBackgroundResource(R.drawable.reward1_esc2);
                    guardarAparienciaTotal();
                });
            }
        }
        else if (v.getId() == R.id.ivRp1) {
            if (RpLocked1) {
                ponerTraje(true);
            } else {
                mostrarDialogoCompra("Conjunto bloqueado", "Este conjunto cuesta 10 monedas", "Rp1", () -> {
                    RpLocked1 = true;
                    ivRp1.setImageResource(R.drawable.reward1_r1_unlocked);
                    ponerTraje(true);
                });
            }
        } else if (v.getId() == R.id.ivRp2) {
            if (RpLocked2) {
                ponerSudadera(true);
            } else {
                mostrarDialogoCompra("Conjunto bloqueado", "Este conjunto cuesta 10 monedas", "Rp2", () -> {
                    RpLocked2 = true;
                    ivRp2.setImageResource(R.drawable.reward1_r2_unlocked);
                    ponerSudadera(true);
                });
            }
        } else if (v.getId() == R.id.ivC1) {
            if (CLocked1) {
                seleccionarDinosaurio("reward1_din1", R.drawable.reward1_din1);
            } else {
                mostrarDialogoCompra("Dinosaurio bloqueado", "Este dinosaurio cuesta 10 monedas", "C1", () -> {
                    CLocked1 = true;
                    ivC1.setImageResource(R.drawable.reward1_c1);
                    seleccionarDinosaurio("reward1_din1", R.drawable.reward1_din1);
                });
            }
        } else if (v.getId() == R.id.ivC2) {
            if (CLocked2) {
                seleccionarDinosaurio("reward1_din2", R.drawable.reward1_din2);
            } else {
                mostrarDialogoCompra("Dinosaurio bloqueado", "Este dinosaurio cuesta 10 monedas", "C2", () -> {
                    CLocked2 = true;
                    ivC2.setImageResource(R.drawable.reward1_c2);
                    seleccionarDinosaurio("reward1_din2", R.drawable.reward1_din2);
                });
            }
        } else if (v.getId() == R.id.ivC3) {
            if (CLocked3) {
                seleccionarDinosaurio("reward1_din3", R.drawable.reward1_din3);
            } else {
                mostrarDialogoCompra("Dinosaurio bloqueado", "Este dinosaurio cuesta 10 monedas", "C3", () -> {
                    CLocked3 = true;
                    ivC3.setImageResource(R.drawable.reward1_c3);
                    seleccionarDinosaurio("reward1_din3", R.drawable.reward1_din3);
                });
            }
        }
    }

    private void seleccionarDinosaurio(String id, int resId) {
        dinosaurioSeleccionado = id;
        ivReward1.setImageResource(resId);
        trajeSeleccionado = ""; // Al cambiar de dinosaurio, se quita el traje actual
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
