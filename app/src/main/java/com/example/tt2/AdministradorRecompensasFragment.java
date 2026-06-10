package com.example.tt2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdministradorRecompensasFragment extends Fragment {

    private static final String TAG = "AdminRecompensas";
    private FirebaseFirestore db;
    private MaterialButton btnAsignarReward1, btnAsignarReward2;
    
    private RecyclerView rvAsignaciones;
    private AsignacionesRewardsAdapter adapter;
    private List<AsignacionReward> listaAsignaciones;

    public AdministradorRecompensasFragment() {
    }

    public static AdministradorRecompensasFragment newInstance() {
        return new AdministradorRecompensasFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recompensas_administrador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAsignarReward1 = view.findViewById(R.id.btnAsignarReward1);
        btnAsignarReward2 = view.findViewById(R.id.btnAsignarReward2);
        rvAsignaciones = view.findViewById(R.id.rvAsignacionesRecompensas);

        if (rvAsignaciones != null) {
            rvAsignaciones.setLayoutManager(new LinearLayoutManager(getContext()));
            listaAsignaciones = new ArrayList<>();
            adapter = new AsignacionesRewardsAdapter(listaAsignaciones);
            rvAsignaciones.setAdapter(adapter);
            cargarAsignacionesRewards();
        }

        if (btnAsignarReward1 != null) {
            btnAsignarReward1.setOnClickListener(v -> mostrarDialogoTerapeutas("Reward1", "Recompensa 1"));
        }
        
        if (btnAsignarReward2 != null) {
            btnAsignarReward2.setOnClickListener(v -> mostrarDialogoTerapeutas("Reward2", "Recompensa 2"));
        }
    }

    private void cargarAsignacionesRewards() {
        db.collection("recompensas_asignadas")
                .whereEqualTo("tipoUsuario", "Terapeuta")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando asignaciones", error);
                        return;
                    }
                    listaAsignaciones.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionReward asig = doc.toObject(AsignacionReward.class);
                            asig.documentId = doc.getId();
                            listaAsignaciones.add(asig);
                        }
                        // Ordenar por fecha
                        Collections.sort(listaAsignaciones, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
    }

    private void mostrarDialogoTerapeutas(String rewardId, String rewardName) {
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Terapeuta")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombresTerapeutas = new ArrayList<>();
                    List<String> idsTerapeutas = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        String id = doc.getId();
                        nombresTerapeutas.add(nombre != null ? nombre : "Sin nombre");
                        idsTerapeutas.add(id);
                    }

                    if (nombresTerapeutas.isEmpty()) {
                        Toast.makeText(getContext(), "No hay terapeutas registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Asignar " + rewardName + " a:");

                    ArrayAdapter<String> adapterDialog = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nombresTerapeutas);
                    builder.setAdapter(adapterDialog, (dialog, which) -> {
                        String idTerapeuta = idsTerapeutas.get(which);
                        String nombreTerapeuta = nombresTerapeutas.get(which);
                        verificarYAsignarReward(idTerapeuta, nombreTerapeuta, rewardId, rewardName);
                    });

                    builder.show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar terapeutas", Toast.LENGTH_SHORT).show());
    }

    private void verificarYAsignarReward(String idTerapeuta, String nombreTerapeuta, String rewardId, String rewardName) {
        db.collection("recompensas_asignadas")
                .whereEqualTo("idUsuario", idTerapeuta)
                .whereEqualTo("rewardId", rewardId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "¡Aviso! " + nombreTerapeuta + " ya tiene asignada esta recompensa.", Toast.LENGTH_LONG).show();
                    } else {
                        realizarAsignacion(idTerapeuta, nombreTerapeuta, rewardId, rewardName);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al verificar duplicados", Toast.LENGTH_SHORT).show());
    }

    private void realizarAsignacion(String idTerapeuta, String nombreTerapeuta, String rewardId, String rewardName) {
        Map<String, Object> asignacion = new HashMap<>();
        asignacion.put("idUsuario", idTerapeuta);
        asignacion.put("nombreTerapeuta", nombreTerapeuta);
        asignacion.put("rewardId", rewardId);
        asignacion.put("rewardName", rewardName);
        asignacion.put("tipoUsuario", "Terapeuta");
        asignacion.put("fechaAsignacion", System.currentTimeMillis());

        db.collection("recompensas_asignadas")
                .document(idTerapeuta + "_" + rewardId)
                .set(asignacion)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), rewardName + " asignada correctamente ✓", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar asignación", Toast.LENGTH_SHORT).show());
    }

    private void desasignarReward(String documentId) {
        db.collection("recompensas_asignadas").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Recompensa quitada ✓", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTADOR ---

    private class AsignacionesRewardsAdapter extends RecyclerView.Adapter<AsignacionesRewardsAdapter.ViewHolder> {
        private List<AsignacionReward> mData;
        public AsignacionesRewardsAdapter(List<AsignacionReward> data) { this.mData = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asignacion_recompensa_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionReward asig = mData.get(position);
            holder.tvRew.setText(asig.rewardName != null ? asig.rewardName : asig.rewardId);
            holder.tvTer.setText("Asignado a: " + asig.nombreTerapeuta);
            holder.btnQuitar.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                    .setTitle("Confirmar")
                    .setMessage("¿Desea quitar esta recompensa a " + asig.nombreTerapeuta + "?")
                    .setPositiveButton("Sí, quitar", (dialog, which) -> desasignarReward(asig.documentId))
                    .setNegativeButton("Cancelar", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() { return mData.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRew, tvTer;
            Button btnQuitar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRew = itemView.findViewById(R.id.tvRecompensaAsignada);
                tvTer = itemView.findViewById(R.id.tvTerapeutaAsignadoReward);
                btnQuitar = itemView.findViewById(R.id.btnDesasignarReward);
            }
        }
    }

    public static class AsignacionReward {
        public String documentId;
        public String idUsuario;
        public String nombreTerapeuta;
        public String rewardId;
        public String rewardName;
        public String tipoUsuario;
        public long fechaAsignacion;
        public AsignacionReward() {}
    }
}
