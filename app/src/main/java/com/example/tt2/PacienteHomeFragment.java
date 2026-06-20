package com.example.tt2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PacienteHomeFragment extends Fragment {

    private static final String TAG = "PacienteHomeFragment";
    private TextView tvHola, tvFrase, tvTerminados, tvEnProgreso, tvPendientes, tvPuntos;
    private ImageView ivPersonaje, ivEscenarioHeader;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private String currentUserId;
    
    private final String[] frasesMotivadoras = {
            "¡Hoy es un gran día para aprender!",
            "¡Sigue esforzándote, lo haces genial!",
            "¡Cada paso cuenta, sigue adelante!",
            "¡Eres muy inteligente y capaz!",
            "¡Tu esfuerzo dará grandes frutos!",
            "¡Sigue practicando y serás un experto!",
            "¡Me encanta ver cómo progresas!"
    };

    public PacienteHomeFragment() {
    }

    public static PacienteHomeFragment newInstance() {
        return new PacienteHomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "anonimo";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_paciente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHola = view.findViewById(R.id.tvHolaPaciente);
        tvFrase = view.findViewById(R.id.tvFraseMotivadora);
        tvTerminados = view.findViewById(R.id.tvTerminados);
        tvEnProgreso = view.findViewById(R.id.tvEnProgreso);
        tvPendientes = view.findViewById(R.id.tvPendientes);
        tvPuntos = view.findViewById(R.id.tvPuntos);
        ivPersonaje = view.findViewById(R.id.ivPersonaje);
        ivEscenarioHeader = view.findViewById(R.id.ivEscenarioHeader);
        progressBar = view.findViewById(R.id.pbHomePaciente);

        mostrarFraseAleatoria();
        cargarNombrePaciente();
        cargarEstadisticas();
        cargarPuntos();
        cargarAparienciaPersonaje();
    }

    private void mostrarFraseAleatoria() {
        Random random = new Random();
        int index = random.nextInt(frasesMotivadoras.length);
        tvFrase.setText(frasesMotivadoras[index]);
    }

    private void cargarNombrePaciente() {
        if (currentUserId.equals("anonimo")) return;
        
        db.collection("usuarios").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        tvHola.setText("¡Hola, " + (nombre != null ? nombre : "amigo") + "!");
                    }
                });
    }

    private void cargarPuntos() {
        if (currentUserId.equals("anonimo")) return;

        db.collection("usuarios").document(currentUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) return;
                    if (isAdded() && documentSnapshot != null && documentSnapshot.exists()) {
                        Long puntos = documentSnapshot.getLong("puntos");
                        tvPuntos.setText(String.valueOf(puntos != null ? puntos : 0));
                    }
                });
    }

    private void cargarAparienciaPersonaje() {
        if (currentUserId.equals("anonimo")) return;

        db.collection("recompensas_pacientes").document(currentUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error escuchando apariencia", error);
                        return;
                    }
                    
                    if (isAdded() && documentSnapshot != null && documentSnapshot.exists()) {
                        Map<String, Object> reward1 = (Map<String, Object>) documentSnapshot.get("reward1");
                        Map<String, Object> reward2 = (Map<String, Object>) documentSnapshot.get("reward2");

                        Timestamp t1 = null, t2 = null;
                        Map<String, Object> ap1 = null, ap2 = null;

                        if (reward1 != null && reward1.containsKey("apariencia")) {
                            ap1 = (Map<String, Object>) reward1.get("apariencia");
                            if (ap1 != null) {
                                Object ts = ap1.get("ultimo_cambio");
                                if (ts instanceof Timestamp) t1 = (Timestamp) ts;
                            }
                        }
                        
                        if (reward2 != null && reward2.containsKey("apariencia")) {
                            ap2 = (Map<String, Object>) reward2.get("apariencia");
                            if (ap2 != null) {
                                Object ts = ap2.get("ultimo_cambio");
                                if (ts instanceof Timestamp) t2 = (Timestamp) ts;
                            }
                        }

                        // Lógica para decidir cuál personaje mostrar basado en el último cambio
                        if (t1 != null && t2 != null) {
                            if (t1.compareTo(t2) >= 0) aplicarReward1(ap1);
                            else aplicarReward2(ap2);
                        } else if (t1 != null) {
                            aplicarReward1(ap1);
                        } else if (t2 != null) {
                            aplicarReward2(ap2);
                        } else if (ap1 != null) {
                            aplicarReward1(ap1);
                        } else if (ap2 != null) {
                            aplicarReward2(ap2);
                        } else {
                            ivPersonaje.setImageResource(R.drawable.user);
                            ivEscenarioHeader.setImageResource(R.drawable.bg_terapeuta_header);
                        }
                    } else {
                        ivPersonaje.setImageResource(R.drawable.user);
                        ivEscenarioHeader.setImageResource(R.drawable.bg_terapeuta_header);
                    }
                });
    }

    private void aplicarReward1(Map<String, Object> ap) {
        if (ap == null) return;
        String dinosaurio = (String) ap.get("dinosaurio");
        String traje = (String) ap.get("traje");
        String escenario = (String) ap.get("escenario");
        
        Log.d(TAG, "Mostrando Reward1: " + dinosaurio + ", traje: " + traje + ", escenario: " + escenario);

        // Aplicar Personaje
        int resIdPersonaje = R.drawable.user;
        if ("reward1_din1".equals(dinosaurio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward1_din1_r1;
            else if ("sudadera".equals(traje)) resIdPersonaje = R.drawable.reward_din1_r2;
            else resIdPersonaje = R.drawable.reward1_din1;
        } else if ("reward1_din2".equals(dinosaurio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward1_din2_r1;
            else if ("sudadera".equals(traje)) resIdPersonaje = R.drawable.reward1_din2_r2;
            else resIdPersonaje = R.drawable.reward1_din2;
        } else if ("reward1_din3".equals(dinosaurio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward1_din3_r1;
            else if ("sudadera".equals(traje)) resIdPersonaje = R.drawable.reward1_din3_r2;
            else resIdPersonaje = R.drawable.reward1_din3;
        }
        ivPersonaje.setImageResource(resIdPersonaje);

        // Aplicar Escenario en el Header
        int resIdEscenario = R.drawable.bg_terapeuta_header;
        if ("Esc1".equals(escenario)) resIdEscenario = R.drawable.reward1_esc1;
        else if ("Esc2".equals(escenario)) resIdEscenario = R.drawable.reward1_esc2;
        ivEscenarioHeader.setImageResource(resIdEscenario);
    }

    private void aplicarReward2(Map<String, Object> ap) {
        if (ap == null) return;
        String unicornio = (String) ap.get("unicornio");
        String traje = (String) ap.get("traje");
        String escenario = (String) ap.get("escenario");
        
        Log.d(TAG, "Mostrando Reward2: " + unicornio + ", traje: " + traje + ", escenario: " + escenario);

        // Aplicar Personaje
        int resIdPersonaje = R.drawable.user;
        if ("reward2_uni1".equals(unicornio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward2_uni1_r1;
            else if ("vestido".equals(traje)) resIdPersonaje = R.drawable.reward2_uni1_r2;
            else resIdPersonaje = R.drawable.reward2_uni1;
        } else if ("reward2_uni2".equals(unicornio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward2_uni2_r1;
            else if ("vestido".equals(traje)) resIdPersonaje = R.drawable.reward2_uni2_r2;
            else resIdPersonaje = R.drawable.reward2_uni2;
        } else if ("reward2_uni3".equals(unicornio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward2_uni3_r1;
            else if ("vestido".equals(traje)) resIdPersonaje = R.drawable.reward2_uni3_r2;
            else resIdPersonaje = R.drawable.reward2_uni3;
        } else if ("reward2_uni4".equals(unicornio)) {
            if ("conjunto".equals(traje)) resIdPersonaje = R.drawable.reward2_uni4_r1;
            else if ("vestido".equals(traje)) resIdPersonaje = R.drawable.reward2_uni4_r2;
            else resIdPersonaje = R.drawable.reward2_uni4;
        }
        ivPersonaje.setImageResource(resIdPersonaje);

        // Aplicar Escenario en el Header
        int resIdEscenario = R.drawable.bg_terapeuta_header;
        if ("Esc1".equals(escenario)) resIdEscenario = R.drawable.reward2_esc1_unlocked;
        else if ("Esc2".equals(escenario)) resIdEscenario = R.drawable.reward2_esc2_unlocked;
        ivEscenarioHeader.setImageResource(resIdEscenario);
    }

    private void cargarEstadisticas() {
        if (currentUserId.equals("anonimo")) return;
        
        progressBar.setVisibility(View.VISIBLE);

        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", currentUserId)
                .get()
                .addOnSuccessListener(asignaciones -> {
                    int totalAsignados = asignaciones.size();
                    Map<String, Boolean> ejerciciosAsignados = new HashMap<>();
                    for (QueryDocumentSnapshot doc : asignaciones) {
                        String logId = doc.getString("logicalId");
                        if (logId != null) ejerciciosAsignados.put(logId, true);
                    }

                    db.collection("progreso_ejercicios")
                            .whereEqualTo("idPaciente", currentUserId)
                            .get()
                            .addOnSuccessListener(progresos -> {
                                int terminados = 0;
                                int enProgreso = 0;
                                
                                for (QueryDocumentSnapshot doc : progresos) {
                                    String logId = doc.getString("logicalId");
                                    Long porcentaje = doc.getLong("porcentaje");
                                    
                                    if (logId != null && ejerciciosAsignados.containsKey(logId)) {
                                        if (porcentaje != null) {
                                            if (porcentaje >= 100) {
                                                terminados++;
                                            } else if (porcentaje > 0) {
                                                enProgreso++;
                                            }
                                        }
                                    }
                                }

                                int pendientes = totalAsignados - terminados - enProgreso;

                                if (isAdded()) {
                                    tvTerminados.setText(String.valueOf(terminados));
                                    tvEnProgreso.setText(String.valueOf(enProgreso));
                                    tvPendientes.setText(String.valueOf(pendientes));
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) progressBar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) progressBar.setVisibility(View.GONE);
                });
    }
}
