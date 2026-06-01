package com.example.tt2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tt2.rewards.Reward1;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TerapeutaRecompensasFragment extends Fragment {

    private static final String TAG = "TerapeutaRecompensas";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // Panel superior: Recompensas que el terapeuta ya asignó a pacientes
    private RecyclerView rvAsignacionesPacientes;
    private AsignacionesPacientesAdapter adapterAsignaciones;
    private List<AsignacionReward> listaAsignacionesPacientes;

    // Panel inferior: Recompensas que el administrador asignó a este terapeuta
    private RecyclerView rvDisponibles;
    private DisponiblesAdapter adapterDisponibles;
    private List<AsignacionReward> listaDisponibles;

    public TerapeutaRecompensasFragment() {
    }

    public static TerapeutaRecompensasFragment newInstance() {
        return new TerapeutaRecompensasFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recompensas_terapeuta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar panel superior (Asignaciones a pacientes)
        rvAsignacionesPacientes = view.findViewById(R.id.rvAsignacionesPacientes);
        rvAsignacionesPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        listaAsignacionesPacientes = new ArrayList<>();
        adapterAsignaciones = new AsignacionesPacientesAdapter(listaAsignacionesPacientes);
        rvAsignacionesPacientes.setAdapter(adapterAsignaciones);

        // Configurar panel inferior (Recompensas disponibles para el terapeuta)
        rvDisponibles = view.findViewById(R.id.rvRecompensasDisponibles);
        rvDisponibles.setLayoutManager(new LinearLayoutManager(getContext()));
        listaDisponibles = new ArrayList<>();
        adapterDisponibles = new DisponiblesAdapter(listaDisponibles);
        rvDisponibles.setAdapter(adapterDisponibles);

        cargarDatos();
    }

    private void cargarDatos() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        // 1. Cargar recompensas que el terapeuta tiene permitidas (Asignadas por Admin)
        db.collection("recompensas_asignadas")
                .whereEqualTo("idUsuario", currentUserId)
                .whereEqualTo("tipoUsuario", "Terapeuta")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    listaDisponibles.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionReward reward = doc.toObject(AsignacionReward.class);
                            reward.documentId = doc.getId();
                            listaDisponibles.add(reward);
                        }
                    }
                    adapterDisponibles.notifyDataSetChanged();
                });

        // 2. Cargar recompensas que este terapeuta ha asignado a sus pacientes
        db.collection("recompensas_asignadas")
                .whereEqualTo("idTerapeuta", currentUserId)
                .whereEqualTo("tipoUsuario", "Paciente")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    listaAsignacionesPacientes.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionReward reward = doc.toObject(AsignacionReward.class);
                            reward.documentId = doc.getId();
                            listaAsignacionesPacientes.add(reward);
                        }
                        Collections.sort(listaAsignacionesPacientes, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
                    }
                    adapterAsignaciones.notifyDataSetChanged();
                });
    }

    private void mostrarDialogoPacientes(AsignacionReward reward) {
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Paciente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombresPacientes = new ArrayList<>();
                    List<String> idsPacientes = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        String id = doc.getId();
                        nombresPacientes.add(nombre != null ? nombre : "Sin nombre");
                        idsPacientes.add(id);
                    }

                    if (nombresPacientes.isEmpty()) {
                        Toast.makeText(getContext(), "No tienes pacientes registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Asignar " + reward.rewardName + " a:");

                    ArrayAdapter<String> adapterDialog = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nombresPacientes);
                    builder.setAdapter(adapterDialog, (dialog, which) -> {
                        String idPaciente = idsPacientes.get(which);
                        String nombrePaciente = nombresPacientes.get(which);
                        verificarYAsignarAPaciente(idPaciente, nombrePaciente, reward);
                    });

                    builder.show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar pacientes", Toast.LENGTH_SHORT).show());
    }

    private void verificarYAsignarAPaciente(String idPaciente, String nombrePaciente, AsignacionReward reward) {
        db.collection("recompensas_asignadas")
                .whereEqualTo("idUsuario", idPaciente)
                .whereEqualTo("rewardId", reward.rewardId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), nombrePaciente + " ya tiene esta recompensa.", Toast.LENGTH_LONG).show();
                    } else {
                        realizarAsignacion(idPaciente, nombrePaciente, reward);
                    }
                });
    }

    private void realizarAsignacion(String idPaciente, String nombrePaciente, AsignacionReward reward) {
        Map<String, Object> asignacion = new HashMap<>();
        asignacion.put("idUsuario", idPaciente);
        asignacion.put("nombrePaciente", nombrePaciente); // Para mostrar en la lista del terapeuta
        asignacion.put("rewardId", reward.rewardId);
        asignacion.put("rewardName", reward.rewardName);
        asignacion.put("tipoUsuario", "Paciente");
        asignacion.put("idTerapeuta", auth.getCurrentUser().getUid()); // Quién lo asignó
        asignacion.put("fechaAsignacion", System.currentTimeMillis());

        db.collection("recompensas_asignadas")
                .document(idPaciente + "_" + reward.rewardId)
                .set(asignacion)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Asignado a " + nombrePaciente + " ✓", Toast.LENGTH_SHORT).show());
    }

    private void desasignarDePaciente(String documentId) {
        db.collection("recompensas_asignadas").document(documentId).delete();
    }

    // --- ADAPTADORES ---

    private class DisponiblesAdapter extends RecyclerView.Adapter<DisponiblesAdapter.ViewHolder> {
        private List<AsignacionReward> mData;
        public DisponiblesAdapter(List<AsignacionReward> data) { this.mData = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recompensa_terapeuta, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionReward reward = mData.get(position);
            holder.tvNombre.setText(reward.rewardName);
            holder.tvFecha.setText("Disponible");
            holder.btnAccion.setText("ASIGNAR A PACIENTE");
            holder.btnAccion.setOnClickListener(v -> mostrarDialogoPacientes(reward));
        }

        @Override
        public int getItemCount() { return mData.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvFecha;
            MaterialButton btnAccion;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreReward);
                tvFecha = itemView.findViewById(R.id.tvFechaReward);
                btnAccion = itemView.findViewById(R.id.btnVerReward);
            }
        }
    }

    private class AsignacionesPacientesAdapter extends RecyclerView.Adapter<AsignacionesPacientesAdapter.ViewHolder> {
        private List<AsignacionReward> mData;
        public AsignacionesPacientesAdapter(List<AsignacionReward> data) { this.mData = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asignacion_recompensa_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionReward asig = mData.get(position);
            holder.tvRew.setText(asig.rewardName);
            holder.tvPac.setText("Paciente: " + (asig.nombrePaciente != null ? asig.nombrePaciente : asig.idUsuario));
            holder.btnQuitar.setOnClickListener(v -> desasignarDePaciente(asig.documentId));
        }

        @Override
        public int getItemCount() { return mData.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRew, tvPac;
            Button btnQuitar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRew = itemView.findViewById(R.id.tvRecompensaAsignada);
                tvPac = itemView.findViewById(R.id.tvTerapeutaAsignadoReward);
                btnQuitar = itemView.findViewById(R.id.btnDesasignarReward);
            }
        }
    }

    public static class AsignacionReward {
        public String documentId;
        public String idUsuario;
        public String rewardId;
        public String rewardName;
        public String tipoUsuario;
        public String idTerapeuta;
        public String nombrePaciente;
        public long fechaAsignacion;
        public AsignacionReward() {}
    }
}
