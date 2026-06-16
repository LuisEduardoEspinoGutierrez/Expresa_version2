package com.example.tt2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PacienteHomeFragment extends Fragment {

    private TextView tvHola, tvFrase, tvTerminados, tvEnProgreso, tvPendientes, tvPuntos;
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
        progressBar = view.findViewById(R.id.pbHomePaciente);

        mostrarFraseAleatoria();
        cargarNombrePaciente();
        cargarEstadisticas();
        cargarPuntos();
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

    private void cargarEstadisticas() {
        if (currentUserId.equals("anonimo")) return;
        
        progressBar.setVisibility(View.VISIBLE);

        // Primero obtenemos los ejercicios asignados para saber el total y los pendientes
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

                    // Luego obtenemos el progreso para clasificar
                    db.collection("progreso_ejercicios")
                            .whereEqualTo("idPaciente", currentUserId)
                            .get()
                            .addOnSuccessListener(progresos -> {
                                int terminados = 0;
                                int enProgreso = 0;
                                
                                for (QueryDocumentSnapshot doc : progresos) {
                                    String logId = doc.getString("logicalId");
                                    Long porcentaje = doc.getLong("porcentaje");
                                    
                                    // Solo contamos si el ejercicio está asignado
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
