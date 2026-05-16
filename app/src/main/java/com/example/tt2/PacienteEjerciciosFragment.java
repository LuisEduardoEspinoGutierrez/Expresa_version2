package com.example.tt2;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tt2.ejercicios.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragmento que muestra la lista de ejercicios asignados al Paciente (Niño)
 * por su terapeuta para que pueda realizarlos.
 */
public class PacienteEjerciciosFragment extends Fragment {

    private RecyclerView rvEjercicios;
    private EjerciciosPacienteAdapter adapter;
    private List<AsignacionPaciente> listaEjercicios;
    private FirebaseFirestore db;
    private String currentUserId;

    public PacienteEjerciciosFragment() {
    }

    public static PacienteEjerciciosFragment newInstance() {
        return new PacienteEjerciciosFragment();
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
        return inflater.inflate(R.layout.fragment_ejercicios_paciente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvEjercicios = view.findViewById(R.id.rvEjerciciosPaciente);
        if (rvEjercicios != null) {
            rvEjercicios.setLayoutManager(new LinearLayoutManager(getContext()));
            listaEjercicios = new ArrayList<>();
            adapter = new EjerciciosPacienteAdapter(listaEjercicios);
            rvEjercicios.setAdapter(adapter);
            cargarEjerciciosAsignados();
        }
    }

    private void cargarEjerciciosAsignados() {
        if (currentUserId.equals("anonimo")) return;

        // Quitamos el orderBy de la consulta de Firestore para evitar errores de índices.
        // El ordenamiento lo hacemos localmente abajo.
        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE_PACIENTE", "Error al cargar ejercicios: " + error.getMessage());
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    listaEjercicios.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionPaciente asig = doc.toObject(AsignacionPaciente.class);
                            listaEjercicios.add(asig);
                        }
                        
                        // Ordenar localmente por fechaAsignacion descendente (más recientes primero)
                        Collections.sort(listaEjercicios, (o1, o2) -> Long.compare(o2.fechaAsignacion, o1.fechaAsignacion));
                    }
                    
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Adapter para la lista de ejercicios del niño
    private class EjerciciosPacienteAdapter extends RecyclerView.Adapter<EjerciciosPacienteAdapter.ViewHolder> {
        private List<AsignacionPaciente> mData;

        public EjerciciosPacienteAdapter(List<AsignacionPaciente> data) {
            this.mData = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio_asignado, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionPaciente asig = mData.get(position);
            holder.tvNombre.setText(asig.nombreEjercicio);
            holder.tvId.setText("Código: " + asig.logicalId);
            holder.btnRealizar.setText("Comenzar");
            holder.btnRealizar.setOnClickListener(v -> abrirEjercicio(asig.logicalId));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvId;
            Button btnRealizar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreEjercicio);
                tvId = itemView.findViewById(R.id.tvIdEjercicio);
                btnRealizar = itemView.findViewById(R.id.btnAbrirEjercicio);
            }
        }
    }

    private void abrirEjercicio(String logicalId) {
        Class<?> activityClass = null;
        switch (logicalId) {
            case "1": activityClass = Ejercicio01.class; break;
            case "2": activityClass = Ejercicio2Activity.class; break;
            case "3": activityClass = Ejercicio3Activity.class; break;
            case "4": activityClass = Ejercicio4Activity.class; break;
            case "5": activityClass = Ejercicio05.class; break;
            case "6.1": activityClass = Ejercicio06_1.class; break;
            case "6.2": activityClass = Ejercicio06_2.class; break;
            case "6.3": activityClass = Ejercicio06_3.class; break;
            case "6.4": activityClass = Ejercicio06_4.class; break;
            case "6.5": activityClass = Ejercicio06_5.class; break;
            case "7.1": activityClass = Ejercicio7_1Activity.class; break;
            case "7.2": activityClass = Ejercicio7_2Activity.class; break;
            case "7.3": activityClass = Ejercicio7_3Activity.class; break;
            case "7.4": activityClass = Ejercicio7_4Activity.class; break;
            case "8": activityClass = Ejercicio08.class; break;
            case "9": activityClass = Ejercicio09.class; break;
            case "10": activityClass = Ejercicio10Activity.class; break;
            case "11": activityClass = Ejercicio11Activity.class; break;
            case "12": activityClass = Ejercicio12.class; break;
            case "13": activityClass = Ejercicio13.class; break;
            case "14": activityClass = Ejercicio14.class; break;
            case "15": activityClass = Ejercicio15Activity.class; break;
        }

        if (activityClass != null) {
            Intent intent = new Intent(getActivity(), activityClass);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Ejercicio no configurado", Toast.LENGTH_SHORT).show();
        }
    }

    // Modelo de datos para las asignaciones
    public static class AsignacionPaciente {
        public String logicalId;
        public String nombreEjercicio;
        public String idPaciente;
        public String idTerapeuta;
        public long fechaAsignacion;

        public AsignacionPaciente() {} // Requerido por Firestore
    }
}
