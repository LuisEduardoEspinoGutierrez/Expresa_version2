package com.example.tt2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tt2.ejercicios.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerapeutaEjerciciosFragment extends Fragment {

    private FirebaseFirestore db;
    private String currentUserId;

    private RecyclerView rvAsignacionesPacientes;
    private AsignacionesPacienteAdapter asignacionesAdapter;
    private List<AsignacionPaciente> listaAsignacionesPacientes;

    private RecyclerView rvEjerciciosDisponibles;
    private MisEjerciciosDisponiblesAdapter catalogAdapter;
    private List<AsignacionAdmin> listaDisponibles;

    public TerapeutaEjerciciosFragment() {
    }

    public static TerapeutaEjerciciosFragment newInstance() {
        return new TerapeutaEjerciciosFragment();
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
        return inflater.inflate(R.layout.fragment_ejercicios_terapeuta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAsignacionesPacientes = view.findViewById(R.id.rvAsignacionesPacientes);
        if (rvAsignacionesPacientes != null) {
            rvAsignacionesPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
            listaAsignacionesPacientes = new ArrayList<>();
            asignacionesAdapter = new AsignacionesPacienteAdapter(listaAsignacionesPacientes);
            rvAsignacionesPacientes.setAdapter(asignacionesAdapter);
            cargarEjerciciosAsignadosANinos();
        }

        rvEjerciciosDisponibles = view.findViewById(R.id.rvEjerciciosParaAsignar);
        if (rvEjerciciosDisponibles != null) {
            rvEjerciciosDisponibles.setLayoutManager(new LinearLayoutManager(getContext()));
            listaDisponibles = new ArrayList<>();
            catalogAdapter = new MisEjerciciosDisponiblesAdapter(listaDisponibles);
            rvEjerciciosDisponibles.setAdapter(catalogAdapter);
            cargarMisEjerciciosDisponibles();
        }
    }

    private void cargarEjerciciosAsignadosANinos() {
        // Quitamos orderBy para evitar error de índices
        db.collection("pacientes_ejercicios")
                .whereEqualTo("idTerapeuta", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE_ERROR", "Error en pacientes_ejercicios: " + error.getMessage());
                        return;
                    }
                    listaAsignacionesPacientes.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionPaciente asig = doc.toObject(AsignacionPaciente.class);
                            asig.documentId = doc.getId();
                            listaAsignacionesPacientes.add(asig);
                        }
                        // Ordenar localmente
                        Collections.sort(listaAsignacionesPacientes, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
                    }
                    asignacionesAdapter.notifyDataSetChanged();
                });
    }

    private void cargarMisEjerciciosDisponibles() {
        // Quitamos orderBy para evitar error de índices
        db.collection("ejercicios_asignados")
                .whereEqualTo("idTerapeuta", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE_ERROR", "Error en ejercicios_asignados: " + error.getMessage());
                        return;
                    }
                    listaDisponibles.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionAdmin eje = doc.toObject(AsignacionAdmin.class);
                            listaDisponibles.add(eje);
                        }
                        // Ordenar localmente
                        Collections.sort(listaDisponibles, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
                    }
                    catalogAdapter.notifyDataSetChanged();
                });
    }

    private void mostrarDialogoPacientes(String logicalId, String nombreEje) {
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Paciente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombres = new ArrayList<>();
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        nombres.add(nombre != null ? nombre : "Sin nombre");
                        ids.add(doc.getId());
                    }

                    if (nombres.isEmpty()) {
                        Toast.makeText(getContext(), "No hay niños registrados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Asignar a Niño: " + nombreEje);
                    builder.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nombres),
                            (dialog, which) -> {
                                verificarYAsignarAPaciente(ids.get(which), nombres.get(which), logicalId, nombreEje);
                            });
                    builder.show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar niños", Toast.LENGTH_SHORT).show());
    }

    private void verificarYAsignarAPaciente(String idPac, String nomPac, String logId, String nomEje) {
        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", idPac)
                .whereEqualTo("logicalId", logId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        Toast.makeText(getContext(), "El niño " + nomPac + " ya tiene este ejercicio.", Toast.LENGTH_LONG).show();
                    } else {
                        realizarAsignacionAPaciente(idPac, nomPac, logId, nomEje);
                    }
                });
    }

    private void realizarAsignacionAPaciente(String idPac, String nomPac, String logId, String nomEje) {
        Map<String, Object> map = new HashMap<>();
        map.put("idTerapeuta", currentUserId);
        map.put("idPaciente", idPac);
        map.put("nombrePaciente", nomPac);
        map.put("logicalId", logId);
        map.put("nombreEjercicio", nomEje);
        map.put("fechaAsignacion", System.currentTimeMillis());

        db.collection("pacientes_ejercicios")
                .add(map)
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Asignado correctamente ✓", Toast.LENGTH_SHORT).show());
    }

    private void desasignarEjercicioPaciente(String docId, String nomPac) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar")
                .setMessage("¿Quitar este ejercicio a " + nomPac + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("pacientes_ejercicios").document(docId).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Asignación eliminada ✓", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private class MisEjerciciosDisponiblesAdapter extends RecyclerView.Adapter<MisEjerciciosDisponiblesAdapter.ViewHolder> {
        private List<AsignacionAdmin> mData;
        public MisEjerciciosDisponiblesAdapter(List<AsignacionAdmin> data) { this.mData = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_catalogo_ejercicio, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            AsignacionAdmin eje = mData.get(pos);
            h.tvNom.setText(eje.nombreEjercicio);
            h.tvNum.setText("Ejercicio " + eje.logicalId);
            h.btnAsig.setText("Asignar a Niño");
            h.btnAsig.setOnClickListener(v -> mostrarDialogoPacientes(eje.logicalId, eje.nombreEjercicio));
        }

        @Override
        public int getItemCount() { return mData.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNom, tvNum; Button btnAsig;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNom = itemView.findViewById(R.id.tvNombreEjercicioCatalogo);
                tvNum = itemView.findViewById(R.id.tvNumeroEjercicioCatalogo);
                btnAsig = itemView.findViewById(R.id.btnAsignarEjercicioCatalogo);
            }
        }
    }

    private class AsignacionesPacienteAdapter extends RecyclerView.Adapter<AsignacionesPacienteAdapter.ViewHolder> {
        private List<AsignacionPaciente> mData;
        public AsignacionesPacienteAdapter(List<AsignacionPaciente> data) { this.mData = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_asignacion_paciente, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            AsignacionPaciente asig = mData.get(pos);
            h.tvEje.setText(asig.nombreEjercicio);
            h.tvPac.setText("Niño: " + asig.nombrePaciente);
            h.btnQuit.setOnClickListener(v -> desasignarEjercicioPaciente(asig.documentId, asig.nombrePaciente));
        }

        @Override
        public int getItemCount() { return mData.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEje, tvPac; Button btnQuit;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEje = itemView.findViewById(R.id.tvEjercicioParaPaciente);
                tvPac = itemView.findViewById(R.id.tvPacienteAsignado);
                btnQuit = itemView.findViewById(R.id.btnDesasignarPaciente);
            }
        }
    }

    public static class AsignacionPaciente {
        public String documentId, logicalId, nombreEjercicio, idPaciente, nombrePaciente, idTerapeuta;
        public long fechaAsignacion;
        public AsignacionPaciente() {}
    }

    public static class AsignacionAdmin {
        public String logicalId, nombreEjercicio, idTerapeuta;
        public long fechaAsignacion;
        public AsignacionAdmin() {}
    }
}
