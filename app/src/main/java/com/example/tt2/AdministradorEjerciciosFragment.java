package com.example.tt2;

import android.app.AlertDialog;
import android.os.Bundle;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdministradorEjerciciosFragment extends Fragment {

    private FirebaseFirestore db;
    
    // Panel de Asignaciones Hechas (Panel superior para desasignar)
    private RecyclerView rvAsignaciones;
    private AsignacionesAdapter asignacionesAdapter;
    private List<AsignacionAdmin> listaAsignaciones;

    // Panel de Catálogo de Ejercicios para Asignar (Abajo - Las "Cajitas")
    private RecyclerView rvCatalogo;
    private CatalogoAdapter catalogoAdapter;
    private List<Ejercicio> listaCatalogo;

    public AdministradorEjerciciosFragment() {
        // Required empty public constructor
    }

    public static AdministradorEjerciciosFragment newInstance() {
        return new AdministradorEjerciciosFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ejercicios_administrador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Configurar RecyclerView de Asignaciones (Arriba)
        rvAsignaciones = view.findViewById(R.id.rvAsignacionesAdmin);
        if (rvAsignaciones != null) {
            rvAsignaciones.setLayoutManager(new LinearLayoutManager(getContext()));
            listaAsignaciones = new ArrayList<>();
            asignacionesAdapter = new AsignacionesAdapter(listaAsignaciones);
            rvAsignaciones.setAdapter(asignacionesAdapter);
            cargarAsignaciones();
        }

        // 2. Configurar RecyclerView de Catálogo (Abajo, las "cajitas" para asignar)
        rvCatalogo = view.findViewById(R.id.rvCatalogoEjercicios);
        if (rvCatalogo != null) {
            rvCatalogo.setLayoutManager(new LinearLayoutManager(getContext()));
            listaCatalogo = new ArrayList<>();
            catalogoAdapter = new CatalogoAdapter(listaCatalogo);
            rvCatalogo.setAdapter(catalogoAdapter);
            cargarCatalogo();
        }

        Button btnCargarFirestore = view.findViewById(R.id.btnCargarFirestore);
        if (btnCargarFirestore != null) {
            btnCargarFirestore.setOnClickListener(v -> sincronizarEjercicios());
        }
    }

    private void cargarAsignaciones() {
        db.collection("ejercicios_asignados")
                .orderBy("fechaAsignacion", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    listaAsignaciones.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            AsignacionAdmin asig = doc.toObject(AsignacionAdmin.class);
                            asig.documentId = doc.getId(); 
                            listaAsignaciones.add(asig);
                        }
                    }
                    asignacionesAdapter.notifyDataSetChanged();
                });
    }

    private void cargarCatalogo() {
        db.collection("ejercicios")
                .orderBy("numeroEjercicio")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    listaCatalogo.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Ejercicio eje = doc.toObject(Ejercicio.class);
                            listaCatalogo.add(eje);
                        }
                    }
                    catalogoAdapter.notifyDataSetChanged();
                });
    }

    private void mostrarDialogoTerapeutas(String logicalId, String nombreEje) {
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
                    builder.setTitle("Asignar a Terapeuta: " + nombreEje);

                    ArrayAdapter<String> adapterDialog = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nombresTerapeutas);
                    builder.setAdapter(adapterDialog, (dialog, which) -> {
                        String idTerapeuta = idsTerapeutas.get(which);
                        String nombreTerapeuta = nombresTerapeutas.get(which);
                        verificarYAsignarEjercicio(idTerapeuta, nombreTerapeuta, logicalId, nombreEje);
                    });

                    builder.show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar terapeutas", Toast.LENGTH_SHORT).show());
    }

    private void verificarYAsignarEjercicio(String idTerapeuta, String nombreTerapeuta, String logicalId, String nombreEje) {
        // VALIDACIÓN: Checar si ya existe la asignación exacta de este ejercicio a este terapeuta
        db.collection("ejercicios_asignados")
                .whereEqualTo("idTerapeuta", idTerapeuta)
                .whereEqualTo("logicalId", logicalId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "¡Aviso! " + nombreTerapeuta + " ya tiene asignado este ejercicio.", Toast.LENGTH_LONG).show();
                    } else {
                        realizarAsignacion(idTerapeuta, nombreTerapeuta, logicalId, nombreEje);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al verificar duplicados", Toast.LENGTH_SHORT).show());
    }

    private void realizarAsignacion(String idTerapeuta, String nombreTerapeuta, String logicalId, String nombreEje) {
        Map<String, Object> asignacion = new HashMap<>();
        asignacion.put("idTerapeuta", idTerapeuta);
        asignacion.put("nombreTerapeuta", nombreTerapeuta);
        asignacion.put("logicalId", logicalId);
        asignacion.put("nombreEjercicio", nombreEje);
        asignacion.put("fechaAsignacion", System.currentTimeMillis());

        db.collection("ejercicios_asignados")
                .add(asignacion)
                .addOnSuccessListener(documentReference -> 
                    Toast.makeText(getContext(), "Asignado correctamente ✓", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error al guardar asignación", Toast.LENGTH_SHORT).show());
    }

    private void desasignarEjercicio(String documentId) {
        db.collection("ejercicios_asignados").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Asignación eliminada ✓", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTADORES ---

    // Adaptador para el Catálogo (Las Cajitas de abajo)
    private class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.ViewHolder> {
        private List<Ejercicio> mData;
        public CatalogoAdapter(List<Ejercicio> data) { this.mData = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalogo_ejercicio, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ejercicio eje = mData.get(position);
            holder.tvNombre.setText(eje.getNombre());
            holder.tvNumero.setText("Número de ejercicio: " + eje.getNumeroEjercicio());
            holder.btnAsignar.setOnClickListener(v -> mostrarDialogoTerapeutas(eje.getNumeroEjercicio(), eje.getNombre()));
        }

        @Override
        public int getItemCount() { return mData.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvNumero;
            Button btnAsignar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreEjercicioCatalogo);
                tvNumero = itemView.findViewById(R.id.tvNumeroEjercicioCatalogo);
                btnAsignar = itemView.findViewById(R.id.btnAsignarEjercicioCatalogo);
            }
        }
    }

    // Adaptador para el Panel de Desasignación (Arriba)
    private class AsignacionesAdapter extends RecyclerView.Adapter<AsignacionesAdapter.ViewHolder> {
        private List<AsignacionAdmin> mData;
        public AsignacionesAdapter(List<AsignacionAdmin> data) { this.mData = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asignacion_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AsignacionAdmin asig = mData.get(position);
            holder.tvEje.setText(asig.nombreEjercicio);
            holder.tvTer.setText("Asignado a: " + asig.nombreTerapeuta);
            holder.btnQuitar.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                    .setTitle("Confirmar")
                    .setMessage("¿Desea quitar este ejercicio a " + asig.nombreTerapeuta + "?")
                    .setPositiveButton("Sí, quitar", (dialog, which) -> desasignarEjercicio(asig.documentId))
                    .setNegativeButton("Cancelar", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() { return mData.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEje, tvTer;
            Button btnQuitar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEje = itemView.findViewById(R.id.tvEjercicioAsignado);
                tvTer = itemView.findViewById(R.id.tvTerapeutaAsignado);
                btnQuitar = itemView.findViewById(R.id.btnDesasignar);
            }
        }
    }

    // Modelo de datos interno
    public static class AsignacionAdmin {
        public String documentId; 
        public String logicalId;
        public String nombreEjercicio;
        public String idTerapeuta;
        public String nombreTerapeuta;
        public long fechaAsignacion;
        public AsignacionAdmin() {}
    }

    private void sincronizarEjercicios() {
        List<Ejercicio> listaDefinida = new ArrayList<>();
        listaDefinida.add(new Ejercicio("1", "Pronunciación Inicial R", "Pronunciación", "Identifica y pronuncia imágenes que comienzan con R.", "Bajo", "Mejorar la articulación de la R al inicio de las palabras."));
        listaDefinida.add(new Ejercicio("2", "Arrastra la R", "Discriminación Visual", "Arrastra las imágenes que empiezan con R al centro.", "Bajo", "Identificar palabras que inician con el fonema R."));
        listaDefinida.add(new Ejercicio("3", "Lectura: El Tesoro de Teresa", "Lectura", "Lee en voz alta la historia de Teresa.", "Medio", "Fluidez lectora y pronunciación de la R suave."));
        listaDefinida.add(new Ejercicio("4", "Lectura: Raúl y el Ferrocarril", "Lectura", "Lee en voz alta la historia de Raúl.", "Medio", "Práctica de la R fuerte en lectura corrida."));
        listaDefinida.add(new Ejercicio("5", "Lectura: Curro y Tarro", "Lectura", "Lee en voz alta la historia de la carroza abandonada.", "Medio", "Mejorar la dicción de palabras con R y RR."));
        listaDefinida.add(new Ejercicio("6.1", "Trabalenguas: El Moro", "Trabalenguas", "Escucha y repite el trabalenguas del Moro.", "Medio", "Agilidad lingual con fonemas vibrantes."));
        listaDefinida.add(new Ejercicio("6.2", "Trabalenguas: El Amor", "Trabalenguas", "Escucha y repite el trabalenguas del Amor.", "Medio", "Mejorar la velocidad de articulación."));
        listaDefinida.add(new Ejercicio("6.3", "Trabalenguas: El Burro", "Trabalenguas", "Escucha y repite el trabalenguas del Burro y los berros.", "Medio", "Precisión articulatoria en fonemas similares."));
        listaDefinida.add(new Ejercicio("6.4", "Trabalenguas: Guitarra y Barril", "Trabalenguas", "Escucha y repite el trabalenguas de la guitarra.", "Alto", "Control de la vibración lingual múltiple."));
        listaDefinida.add(new Ejercicio("6.5", "Trabalenguas: Parra y Perra", "Trabalenguas", "Escucha y repite el trabalenguas de Parra.", "Alto", "Diferenciación fonética en contextos rápidos."));
        listaDefinida.add(new Ejercicio("7.1", "Trabalenguas: Ferrocarril", "Trabalenguas", "Escucha y repite el trabalenguas del ferrocarril.", "Alto", "Dominio de la RR múltiple."));
        listaDefinida.add(new Ejercicio("7.2", "Trabalenguas: La Araña", "Trabalenguas", "Escucha y repite el trabalenguas de la araña.", "Medio", "Fluidez en fonemas vibrantes simples."));
        listaDefinida.add(new Ejercicio("7.3", "Trabalenguas: El Tapón", "Trabalenguas", "Escucha y repite el trabalenguas del tapón.", "Medio", "Coordinación motora oral."));
        listaDefinida.add(new Ejercicio("7.4", "Trabalenguas: Rodolfo el Cerrajero", "Trabalenguas", "Escucha y repite el trabalenguas de Rodolfo.", "Alto", "Articulación de la R en diferentes positions."));
        listaDefinida.add(new Ejercicio("8", "Sopa de Letras: Imágenes R", "Sopa de Letras", "Busca en la sopa los nombres de los dibujos que ves.", "Bajo", "Vocabulario y conciencia fonológica de la R."));
        listaDefinida.add(new Ejercicio("9", "Sopa de Letras: Fonema D", "Sopa de Letras", "Busca palabras que contienen el fonema D.", "Bajo", "Diferenciación de fonemas dentales."));
        listaDefinida.add(new Ejercicio("10", "Sopa de Letras: Sinfón TR", "Sopa de Letras", "Encuentra palabras que contienen el grupo TR.", "Medio", "Reconocimiento de sinfones complejos."));
        listaDefinida.add(new Ejercicio("11", "Sopa de Letras: Sinfón FR", "Sopa de Letras", "Encuentra palabras que contienen el grupo FR.", "Medio", "Reconocimiento de sinfones complejos."));
        listaDefinida.add(new Ejercicio("12", "Relaciona: Letra R", "Relación", "Une las imágenes que contienen R con la letra central.", "Bajo", "Discriminación auditiva de la R."));
        listaDefinida.add(new Ejercicio("13", "Ruleta de Palabras R", "Juego", "Gira la ruleta y graba la palabra indicada.", "Medio", "Práctica lúdica de pronunciación."));
        listaDefinida.add(new Ejercicio("14", "Sílabas RA-RU", "Relación", "Coloca la imagen frente a su sílaba inicial.", "Bajo", "Asociación de sílabas con sonidos iniciales."));
        listaDefinida.add(new Ejercicio("15", "R Fuerte vs R Ligera", "Clasificación", "Clasifica imágenes según el sonido de su R.", "Alto", "Diferenciación entre R fuerte y R suave."));

        db.collection("ejercicios").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Set<String> existingLogicalNumbers = new HashSet<>();
                int currentMaxIntId = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Ejercicio e = document.toObject(Ejercicio.class);
                    if (e.getNumeroEjercicio() != null) existingLogicalNumbers.add(e.getNumeroEjercicio());
                    try {
                        int idVal = Integer.parseInt(document.getId());
                        if (idVal > currentMaxIntId) currentMaxIntId = idVal;
                    } catch (NumberFormatException ignored) {}
                }
                int nextId = currentMaxIntId;
                int addedCounter = 0;
                for (Ejercicio eje : listaDefinida) {
                    if (!existingLogicalNumbers.contains(eje.getNumeroEjercicio())) {
                        nextId++;
                        String autoId = String.valueOf(nextId);
                        eje.setIdEjercicio(autoId);
                        db.collection("ejercicios").document(autoId).set(eje);
                        addedCounter++;
                    }
                }
                if (addedCounter > 0) {
                    Toast.makeText(getContext(), "Se sincronizaron " + addedCounter + " ejercicios nuevos.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "El catálogo ya está actualizado.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al conectar con Firestore", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
