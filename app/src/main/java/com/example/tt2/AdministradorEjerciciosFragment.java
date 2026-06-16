package com.example.tt2;

import android.app.AlertDialog;
import android.os.Bundle;
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

import com.example.tt2.ejercicios.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdministradorEjerciciosFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView rvAsignaciones;
    private AsignacionesAdapter asignacionesAdapter;
    private List<AsignacionAdmin> listaAsignaciones;
    private RecyclerView rvCatalogo;
    private CatalogoAdapter catalogoAdapter;
    private List<Ejercicio> listaCatalogo;

    public AdministradorEjerciciosFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ejercicios_administrador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAsignaciones = view.findViewById(R.id.rvAsignacionesAdmin);
        rvAsignaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        listaAsignaciones = new ArrayList<>();
        asignacionesAdapter = new AsignacionesAdapter(listaAsignaciones);
        rvAsignaciones.setAdapter(asignacionesAdapter);

        rvCatalogo = view.findViewById(R.id.rvCatalogoEjercicios);
        rvCatalogo.setLayoutManager(new LinearLayoutManager(getContext()));
        listaCatalogo = new ArrayList<>();
        catalogoAdapter = new CatalogoAdapter(listaCatalogo);
        rvCatalogo.setAdapter(catalogoAdapter);

        view.findViewById(R.id.btnCargarFirestore).setOnClickListener(v -> sincronizarEjercicios());

        cargarAsignaciones();
        cargarCatalogo();
    }

    private void cargarAsignaciones() {
        db.collection("ejercicios_asignados").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            listaAsignaciones.clear();
            for (QueryDocumentSnapshot doc : value) {
                AsignacionAdmin asig = doc.toObject(AsignacionAdmin.class);
                asig.documentId = doc.getId();
                listaAsignaciones.add(asig);
            }
            Collections.sort(listaAsignaciones, (a, b) -> Long.compare(b.fechaAsignacion, a.fechaAsignacion));
            asignacionesAdapter.notifyDataSetChanged();
        });
    }

    private void cargarCatalogo() {
        db.collection("ejercicios").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            listaCatalogo.clear();
            for (QueryDocumentSnapshot doc : value) {
                Ejercicio eje = doc.toObject(Ejercicio.class);
                if (eje.getNombre() != null && eje.getNumeroEjercicio() != null) {
                    listaCatalogo.add(eje);
                }
            }
            Collections.sort(listaCatalogo, (e1, e2) -> compareEjercicioNumbers(e1.getNumeroEjercicio(), e2.getNumeroEjercicio()));
            catalogoAdapter.notifyDataSetChanged();
        });
    }

    private int compareEjercicioNumbers(String n1, String n2) {
        try {
            String[] p1 = n1.split("\\.");
            String[] p2 = n2.split("\\.");
            int max = Math.max(p1.length, p2.length);
            for (int i = 0; i < max; i++) {
                int v1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
                int v2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;
                if (v1 != v2) return Integer.compare(v1, v2);
            }
        } catch (Exception e) { return n1.compareTo(n2); }
        return 0;
    }

    private void sincronizarEjercicios() {
        List<Ejercicio> listaDefinida = new ArrayList<>();
        listaDefinida.add(new Ejercicio("1", "Pronunciación Inicial R", "Pronunciación", "Identifica imágenes que comienzan con R.", "Bajo", "Articulación inicial."));
        listaDefinida.add(new Ejercicio("2", "Arrastra la R", "Visual", "Arrastra imágenes con R al centro.", "Bajo", "Conciencia fonológica."));
        listaDefinida.add(new Ejercicio("3", "Lectura: El Tesoro de Teresa", "Lectura", "Lee en voz alta sobre Teresa.", "Medio", "Fluidez R suave."));
        listaDefinida.add(new Ejercicio("4", "Lectura: Raúl y el Ferrocarril", "Lectura", "Lee en voz alta sobre Raúl.", "Medio", "Práctica R fuerte."));
        listaDefinida.add(new Ejercicio("5", "Lectura: Curro y Tarro", "Lectura", "Lee en voz alta sobre la carroza.", "Medio", "Dicción R y RR."));
        listaDefinida.add(new Ejercicio("6.1", "Trabalenguas: El Moro", "Trabalenguas", "Repite el trabalenguas del Moro.", "Medio", "Agilidad lingual."));
        listaDefinida.add(new Ejercicio("6.2", "Trabalenguas: El Amor", "Trabalenguas", "Repite el trabalenguas del Amor.", "Medio", "Velocidad."));
        listaDefinida.add(new Ejercicio("6.3", "Trabalenguas: El Burro", "Trabalenguas", "Repite el trabalenguas del Burro.", "Medio", "Precisión."));
        listaDefinida.add(new Ejercicio("6.4", "Trabalenguas: Guitarra", "Trabalenguas", "Repite el trabalenguas de la guitarra.", "Alto", "Vibración lingual."));
        listaDefinida.add(new Ejercicio("6.5", "Trabalenguas: Parra", "Trabalenguas", "Repite el trabalenguas de Parra.", "Alto", "Diferenciación."));
        listaDefinida.add(new Ejercicio("7.1", "Trabalenguas: Ferrocarril", "Trabalenguas", "Repite el trabalenguas del ferrocarril.", "Alto", "Dominio RR."));
        listaDefinida.add(new Ejercicio("7.2", "Trabalenguas: La Araña", "Trabalenguas", "Repite el trabalenguas de la araña.", "Medio", "R simple."));
        listaDefinida.add(new Ejercicio("7.3", "Trabalenguas: El Tapón", "Trabalenguas", "Repite el trabalenguas del tapón.", "Medio", "Coordinación."));
        listaDefinida.add(new Ejercicio("7.4", "Trabalenguas: Rodolfo", "Trabalenguas", "Repite el trabalenguas de Rodolfo.", "Alto", "Diferentes posiciones R."));
        listaDefinida.add(new Ejercicio("8", "Sopa de Letras: Imágenes R", "Sopa", "Busca nombres de dibujos.", "Bajo", "Vocabulario R."));
        listaDefinida.add(new Ejercicio("9", "Sopa de Letras: Fonema D", "Sopa", "Busca palabras con D.", "Bajo", "Diferenciación dental."));
        listaDefinida.add(new Ejercicio("10", "Sopa de Letras: Sinfón TR", "Sopa", "Busca palabras con TR.", "Medio", "Sinfón complejo."));
        listaDefinida.add(new Ejercicio("11", "Sopa de Letras: Sinfón FR", "Sopa", "Busca palabras con FR.", "Medio", "Sinfón complejo."));
        listaDefinida.add(new Ejercicio("12", "Relaciona: Letra R", "Relación", "Une imágenes con R.", "Bajo", "Discriminación R."));
        listaDefinida.add(new Ejercicio("13", "Ruleta de Palabras R", "Juego", "Gira y graba la palabra.", "Medio", "Práctica lúdica."));
        listaDefinida.add(new Ejercicio("14", "Sílabas RA-RU", "Relación", "Une imagen con su sílaba.", "Bajo", "Asociación sílaba-sonido."));
        listaDefinida.add(new Ejercicio("15", "R Fuerte vs R Ligera", "Clasificación", "Clasifica por tipo de R.", "Alto", "Contraste fonético."));
        listaDefinida.add(new Ejercicio("16", "Video Fonema R", "Video", "Observa el video guía original.", "Bajo", "Reconocimiento auditivo."));
        listaDefinida.add(new Ejercicio("17.1", "Video Fonema R (Variante 1)", "Video", "Observa el video variante 1.", "Bajo", "Articulación avanzada."));
        listaDefinida.add(new Ejercicio("17.2", "Video Fonema R (erre)", "Video", "Observa el video con erre.mov.", "Bajo", "Práctica fonema erre."));
        listaDefinida.add(new Ejercicio("17.3", "Video Fonema R (irri)", "Video", "Observa el video con irri.mov.", "Bajo", "Práctica fonema irri."));
        listaDefinida.add(new Ejercicio("17.4", "Video Fonema R (orro)", "Video", "Observa el video con orro.mov.", "Bajo", "Práctica fonema orro."));

        WriteBatch batch = db.batch();
        for (Ejercicio eje : listaDefinida) {
            batch.set(db.collection("ejercicios").document(eje.getNumeroEjercicio()), eje);
        }
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "¡Catálogo Completo Sincronizado! ✓", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoTerapeutas(String logId, String nomEje) {
        db.collection("usuarios").whereEqualTo("tipoUsuario", "Terapeuta").get().addOnSuccessListener(snap -> {
            List<String> nombres = new ArrayList<>(); List<String> ids = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                String n = doc.getString("nombre");
                nombres.add(n != null ? n : "Sin nombre");
                ids.add(doc.getId());
            }
            if (nombres.isEmpty()) { Toast.makeText(getContext(), "No hay terapeutas", Toast.LENGTH_SHORT).show(); return; }
            new AlertDialog.Builder(getContext()).setTitle("Asignar: " + nomEje).setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nombres), (d, w) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idTerapeuta", ids.get(w)); m.put("nombreTerapeuta", nombres.get(w)); m.put("logicalId", logId); m.put("nombreEjercicio", nomEje); m.put("fechaAsignacion", System.currentTimeMillis());
                db.collection("ejercicios_asignados").add(m).addOnSuccessListener(u -> Toast.makeText(getContext(), "Asignado correctamente ✓", Toast.LENGTH_SHORT).show());
            }).show();
        });
    }

    private class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.ViewHolder> {
        private List<Ejercicio> mData;
        public CatalogoAdapter(List<Ejercicio> d) { this.mData = d; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_catalogo_ejercicio, p, false)); }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            Ejercicio e = mData.get(p);
            h.tvNom.setText(e.getNombre()); h.tvNum.setText("Número de ejercicio: " + e.getNumeroEjercicio());
            h.btnAsig.setOnClickListener(v -> mostrarDialogoTerapeutas(e.getNumeroEjercicio(), e.getNombre()));
        }
        @Override public int getItemCount() { return mData.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNom, tvNum; Button btnAsig;
            public ViewHolder(@NonNull View v) { super(v); tvNom = v.findViewById(R.id.tvNombreEjercicioCatalogo); tvNum = v.findViewById(R.id.tvNumeroEjercicioCatalogo); btnAsig = v.findViewById(R.id.btnAsignarEjercicioCatalogo); }
        }
    }

    private class AsignacionesAdapter extends RecyclerView.Adapter<AsignacionesAdapter.ViewHolder> {
        private List<AsignacionAdmin> mData;
        public AsignacionesAdapter(List<AsignacionAdmin> d) { this.mData = d; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_asignacion_admin, p, false)); }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            AsignacionAdmin a = mData.get(p); h.tvEje.setText(a.nombreEjercicio); h.tvTer.setText("Asignado a: " + a.nombreTerapeuta);
            h.btnQuit.setOnClickListener(v -> db.collection("ejercicios_asignados").document(a.documentId).delete());
        }
        @Override public int getItemCount() { return mData.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEje, tvTer; Button btnQuit;
            public ViewHolder(@NonNull View v) { super(v); tvEje = v.findViewById(R.id.tvEjercicioAsignado); tvTer = v.findViewById(R.id.tvTerapeutaAsignado); btnQuit = v.findViewById(R.id.btnDesasignar); }
        }
    }

    public static class AsignacionAdmin { public String documentId, logicalId, nombreEjercicio, idTerapeuta, nombreTerapeuta; public long fechaAsignacion; public AsignacionAdmin() {} }
}
