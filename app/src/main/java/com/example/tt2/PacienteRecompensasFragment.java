package com.example.tt2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Locale;

public class PacienteRecompensasFragment extends Fragment {

    private static final String TAG = "PacienteRecompensas";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView rvRecompensas;
    private RecompensasAdapter adapter;
    private List<AsignacionReward> listaRecompensas;

    public PacienteRecompensasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recompensas_paciente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRecompensas = view.findViewById(R.id.rvRecompensasPaciente);
        rvRecompensas.setLayoutManager(new LinearLayoutManager(getContext()));
        
        listaRecompensas = new ArrayList<>();
        adapter = new RecompensasAdapter(listaRecompensas);
        rvRecompensas.setAdapter(adapter);

        cargarMisRecompensas();
    }

    private void cargarMisRecompensas() {
        if (auth.getCurrentUser() == null) return;
        
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("recompensas_asignadas")
                .whereEqualTo("idUsuario", currentUserId)
                .whereEqualTo("tipoUsuario", "Paciente")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando recompensas del paciente", error);
                        return;
                    }

                    listaRecompensas.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionReward reward = doc.toObject(AsignacionReward.class);
                            listaRecompensas.add(reward);
                        }
                        // Ordenar por fecha reciente
                        Collections.sort(listaRecompensas, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // --- ADAPTADOR ---

    private class RecompensasAdapter extends RecyclerView.Adapter<RecompensasAdapter.ViewHolder> {
        private List<AsignacionReward> mData;
        private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public RecompensasAdapter(List<AsignacionReward> data) {
            this.mData = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recompensa_terapeuta, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionReward reward = mData.get(position);
            
            holder.tvNombre.setText(reward.rewardName != null ? reward.rewardName : reward.rewardId);
            holder.tvFecha.setText("Desbloqueada el: " + sdf.format(new Date(reward.fechaAsignacion)));

            // Configurar icono según el ID
            if ("Reward1".equals(reward.rewardId)) {
                holder.ivIcono.setImageResource(R.drawable.user);
            }

            holder.btnAbrir.setOnClickListener(v -> {
                if ("Reward1".equals(reward.rewardId)) {
                    Intent intent = new Intent(getContext(), Reward1.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Esta recompensa no está disponible", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcono;
            TextView tvNombre, tvFecha;
            MaterialButton btnAbrir;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcono = itemView.findViewById(R.id.ivIconoReward);
                tvNombre = itemView.findViewById(R.id.tvNombreReward);
                tvFecha = itemView.findViewById(R.id.tvFechaReward);
                btnAbrir = itemView.findViewById(R.id.btnVerReward);
            }
        }
    }

    public static class AsignacionReward {
        public String idUsuario;
        public String rewardId;
        public String rewardName;
        public String tipoUsuario;
        public long fechaAsignacion;
        public AsignacionReward() {}
    }
}
