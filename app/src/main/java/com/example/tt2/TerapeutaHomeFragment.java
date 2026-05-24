package com.example.tt2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TerapeutaHomeFragment extends Fragment {

    private TextView tvHola, tvFecha;
    private RecyclerView rvPacientes;
    private ProgressBar pbLoading;
    private PacienteAdapter adapter;
    private List<PacienteHomeItem> listPacientes = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String terapeutaID;
    private Set<String> seenAudioPaths = new HashSet<>();

    public TerapeutaHomeFragment() {}

    public static TerapeutaHomeFragment newInstance() {
        return new TerapeutaHomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        terapeutaID = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
                : "anonimo";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_terapeuta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        tvHola = view.findViewById(R.id.tvHolaTerapeuta);
        tvFecha = view.findViewById(R.id.tvFechaHoy);
        rvPacientes = view.findViewById(R.id.rvPacientesHome);
        pbLoading = view.findViewById(R.id.pbHomeLoading);

        rvPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PacienteAdapter(listPacientes);
        rvPacientes.setAdapter(adapter);

        setFechaActual();
        cargarDatosTerapeuta();
        fetchData();
    }

    private void setFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
        String fecha = sdf.format(Calendar.getInstance().getTime());
        tvFecha.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));
    }

    private void cargarDatosTerapeuta() {
        if (terapeutaID.equals("anonimo")) return;
        db.collection("usuarios").document(terapeutaID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        tvHola.setText("Hola, " + (nombre != null ? nombre : "Terapeuta"));
                    }
                });
    }

    private void fetchData() {
        pbLoading.setVisibility(View.VISIBLE);
        
        // 1. Cargar audios vistos para saber cuáles son nuevos
        db.collection("audios_vistos")
                .whereEqualTo("vistoPor", terapeutaID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    seenAudioPaths.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String path = doc.getString("audioPath");
                        if (path != null) seenAudioPaths.add(path);
                    }
                    cargarPacientes();
                })
                .addOnFailureListener(e -> cargarPacientes());
    }

    private void cargarPacientes() {
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Paciente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listPacientes.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PacienteHomeItem p = new PacienteHomeItem();
                        p.uid = doc.getId();
                        p.nombre = doc.getString("nombre");
                        listPacientes.add(p);
                    }
                    actualizarStatsPacientes();
                })
                .addOnFailureListener(e -> pbLoading.setVisibility(View.GONE));
    }

    private void actualizarStatsPacientes() {
        if (listPacientes.isEmpty()) {
            pbLoading.setVisibility(View.GONE);
            return;
        }

        final int[] completedCount = {0};
        for (PacienteHomeItem p : listPacientes) {
            // 1. Obtener ejercicios asignados específicamente a este paciente
            db.collection("pacientes_ejercicios")
                    .whereEqualTo("idPaciente", p.uid)
                    .get()
                    .addOnSuccessListener(asignacionesSnapshot -> {
                        int totales = asignacionesSnapshot.size();
                        p.ejerciciosTotales = totales;
                        
                        Set<String> assignedIds = new HashSet<>();
                        for (QueryDocumentSnapshot doc : asignacionesSnapshot) {
                            String logId = doc.getString("logicalId");
                            if (logId != null) assignedIds.add(logId);
                        }

                        if (totales == 0) {
                            p.ejerciciosCompletados = 0;
                            processNextPatientStats(p, completedCount);
                        } else {
                            // 2. Obtener progreso y contar solo los que están asignados y terminados
                            db.collection("progreso_ejercicios")
                                    .whereEqualTo("idPaciente", p.uid)
                                    .get()
                                    .addOnSuccessListener(progresoSnapshot -> {
                                        int completados = 0;
                                        for (QueryDocumentSnapshot doc : progresoSnapshot) {
                                            String logId = doc.getString("logicalId");
                                            Long porcentaje = doc.getLong("porcentaje");
                                            if (assignedIds.contains(logId) && porcentaje != null && porcentaje >= 100) {
                                                completados++;
                                            }
                                        }
                                        p.ejerciciosCompletados = completados;
                                        processNextPatientStats(p, completedCount);
                                    })
                                    .addOnFailureListener(e -> processNextPatientStats(p, completedCount));
                        }
                    })
                    .addOnFailureListener(e -> processNextPatientStats(p, completedCount));
        }
    }

    private void processNextPatientStats(PacienteHomeItem p, int[] completedCount) {
        contarAudiosNuevos(p, () -> {
            completedCount[0]++;
            if (completedCount[0] == listPacientes.size()) {
                if (isAdded()) {
                    pbLoading.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void contarAudiosNuevos(PacienteHomeItem p, Runnable onFinish) {
        String[] folders = {"1", "2", "3", "4", "5", "6_1", "6_2", "6_3", "6_4", "6_5", "7_1", "7_2", "7_3", "7_4", "8", "9", "10", "11", "12", "13", "14", "15"};
        final int[] pendingRequests = {folders.length};
        final int[] nuevosCount = {0};

        for (String f : folders) {
            StorageReference ref = storage.getReference().child("audios/ejercicio" + f);
            ref.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    if (item.getName().startsWith(p.uid) && !seenAudioPaths.contains(item.getPath())) {
                        nuevosCount[0]++;
                    }
                }
                pendingRequests[0]--;
                if (pendingRequests[0] <= 0) {
                    p.audiosNuevos = nuevosCount[0];
                    onFinish.run();
                }
            }).addOnFailureListener(e -> {
                pendingRequests[0]--;
                if (pendingRequests[0] <= 0) {
                    p.audiosNuevos = nuevosCount[0];
                    onFinish.run();
                }
            });
        }
    }

    // Model class
    private static class PacienteHomeItem {
        String uid;
        String nombre;
        int audiosNuevos;
        int ejerciciosCompletados;
        int ejerciciosTotales;
    }

    // Adapter class
    private class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.ViewHolder> {
        private List<PacienteHomeItem> items;

        public PacienteAdapter(List<PacienteHomeItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paciente_home, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PacienteHomeItem item = items.get(position);
            holder.tvNombre.setText(item.nombre);
            holder.tvAudios.setText(item.audiosNuevos + (item.audiosNuevos == 1 ? " audio nuevo" : " audios nuevos"));
            holder.tvCompletados.setText(item.ejerciciosCompletados + "/" + item.ejerciciosTotales);
            
            int pendientes = item.ejerciciosTotales - item.ejerciciosCompletados;
            holder.tvPendientes.setText(String.valueOf(pendientes));
            
            int progress = (item.ejerciciosTotales > 0) ? (item.ejerciciosCompletados * 100 / item.ejerciciosTotales) : 0;
            holder.pbProgreso.setProgress(progress);

            holder.btnGenerarReporte.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Generando reporte para " + item.nombre, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvAudios, tvCompletados, tvPendientes;
            ProgressBar pbProgreso;
            Button btnGenerarReporte;

            ViewHolder(View v) {
                super(v);
                tvNombre = v.findViewById(R.id.tvPacienteNombreHome);
                tvAudios = v.findViewById(R.id.tvNuevosAudiosCount);
                tvCompletados = v.findViewById(R.id.tvEjerciciosCompletados);
                tvPendientes = v.findViewById(R.id.tvEjerciciosPendientes);
                pbProgreso = v.findViewById(R.id.pbPacienteProgreso);
                btnGenerarReporte = v.findViewById(R.id.btnGenerarReporte);
            }
        }
    }
}
