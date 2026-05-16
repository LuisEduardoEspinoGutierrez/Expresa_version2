package com.example.tt2;

import android.app.AlertDialog;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TerapeutaCloudFragment extends Fragment {

    private static final String TAG = "TerapeutaCloud";
    private ExpandableListView elvAudios;
    private ProgressBar progressBar;
    private AudioExpandableAdapter adapter;

    private List<String> listPacientesNombres;
    private Map<String, List<AudioItem>> mapAudiosPorPaciente;
    private Map<String, String> uidToNameMap;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private MediaPlayer mediaPlayer;

    public TerapeutaCloudFragment() {
    }

    public static TerapeutaCloudFragment newInstance() {
        return new TerapeutaCloudFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        uidToNameMap = new HashMap<>();
        listPacientesNombres = new ArrayList<>();
        mapAudiosPorPaciente = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_terapeuta, container, false);
        elvAudios = view.findViewById(R.id.elvAudios);
        progressBar = view.findViewById(R.id.pbLoadingCloud);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new AudioExpandableAdapter();
        elvAudios.setAdapter(adapter);
        fetchInitialData();
    }

    private void fetchInitialData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "Cargando usuarios tipo Paciente...");
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Paciente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Usuarios encontrados en Firestore: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        uidToNameMap.put(doc.getId(), nombre != null ? nombre : "Sin nombre");
                    }
                    buscarAudiosEnStorage();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuarios de Firestore", e);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar base de datos", Toast.LENGTH_SHORT).show();
                    // Intentar buscar audios de todos modos por si ya conocemos IDs
                    buscarAudiosEnStorage();
                });
    }

    private void buscarAudiosEnStorage() {
        String[] folders = {"1", "2", "3", "4", "5", "6_1", "6_2", "6_3", "6_4", "6_5", "7_1", "7_2", "7_3", "7_4", "8", "9", "10", "11", "12", "13", "14", "15"};
        
        final int[] pendingRequests = {folders.length};
        Log.d(TAG, "Iniciando escaneo de " + folders.length + " carpetas en Storage...");

        for (String f : folders) {
            String folderPath = "audios/ejercicio" + f;
            StorageReference ref = storage.getReference().child(folderPath);
            ref.listAll().addOnSuccessListener(listResult -> {
                Log.d(TAG, "Carpeta: " + folderPath + " - Archivos encontrados: " + listResult.getItems().size());
                for (StorageReference item : listResult.getItems()) {
                    procesarReferenciaAudio(item, f);
                }
                checkProgress(pendingRequests);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al listar carpeta: " + folderPath + " - " + e.getMessage());
                checkProgress(pendingRequests);
            });
        }
    }

    private void procesarReferenciaAudio(StorageReference item, String numEje) {
        String fileName = item.getName();
        // Estructura: UID_ejeX_tipo_timestamp.ext
        String[] parts = fileName.split("_");
        
        if (parts.length >= 4) {
            String uid = parts[0];
            String lastPartWithExt = parts[parts.length - 1];
            
            // Quitar extensión (.mp4, .3gp, etc)
            String timestampStr = lastPartWithExt;
            int lastDot = lastPartWithExt.lastIndexOf('.');
            if (lastDot != -1) {
                timestampStr = lastPartWithExt.substring(0, lastDot);
            }

            try {
                long timestamp = Long.parseLong(timestampStr);
                String pacienteNombre = uidToNameMap.get(uid);
                if (pacienteNombre == null) {
                    pacienteNombre = "Niño (" + (uid.length() > 5 ? uid.substring(0, 5) : uid) + ")";
                }
                
                AudioItem audio = new AudioItem();
                audio.nombreEjercicio = "Ejercicio " + numEje.replace("_", ".");
                audio.rawNumEje = numEje;
                audio.fecha = timestamp;
                audio.ref = item;

                synchronized (this) {
                    if (!mapAudiosPorPaciente.containsKey(pacienteNombre)) {
                        mapAudiosPorPaciente.put(pacienteNombre, new ArrayList<>());
                        listPacientesNombres.add(pacienteNombre);
                    }
                    mapAudiosPorPaciente.get(pacienteNombre).add(audio);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Nombre de archivo con formato de fecha inválido: " + fileName);
            }
        } else {
            Log.w(TAG, "Nombre de archivo no cumple el formato esperado: " + fileName);
        }
    }

    private void checkProgress(int[] pending) {
        pending[0]--;
        if (pending[0] <= 0) {
            finalizarCarga();
        }
    }

    private void finalizarCarga() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            Collections.sort(listPacientesNombres);
            
            for (List<AudioItem> audios : mapAudiosPorPaciente.values()) {
                Collections.sort(audios, (a, b) -> {
                    int comp = compareEjercicios(a.rawNumEje, b.rawNumEje);
                    if (comp == 0) {
                        // Si es el mismo ejercicio, ordenar por fecha (más reciente primero)
                        return Long.compare(b.fecha, a.fecha);
                    }
                    return comp;
                });
            }

            if (progressBar != null) progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();

            if (listPacientesNombres.isEmpty()) {
                Log.d(TAG, "Escaneo terminado: No se encontró ningún audio.");
                Toast.makeText(getContext(), "No se encontraron grabaciones", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Escaneo terminado: Se encontraron audios de " + listPacientesNombres.size() + " pacientes.");
                elvAudios.expandGroup(0); // Expandir el primero automáticamente
            }
        });
    }

    private int compareEjercicios(String e1, String e2) {
        try {
            if (e1 == null || e2 == null) return 0;
            
            String[] p1 = e1.split("_");
            String[] p2 = e2.split("_");
            
            int n1 = Integer.parseInt(p1[0]);
            int n2 = Integer.parseInt(p2[0]);
            
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
            
            // Si tienen sub-ejercicio (e.g. 6_1 vs 6_2)
            int sub1 = (p1.length > 1) ? Integer.parseInt(p1[1]) : 0;
            int sub2 = (p2.length > 1) ? Integer.parseInt(p2[1]) : 0;
            
            return Integer.compare(sub1, sub2);
        } catch (Exception e) {
            return e1.compareTo(e2);
        }
    }

    private void reproducirAudio(StorageReference ref) {
        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
                mediaPlayer.setDataSource(getContext(), uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                Toast.makeText(getContext(), "Reproduciendo audio...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error al reproducir audio: " + e.getMessage());
                Toast.makeText(getContext(), "Error al reproducir", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "No se pudo obtener el archivo de Storage", e);
            Toast.makeText(getContext(), "No se pudo obtener el archivo", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    private class AudioExpandableAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() { return listPacientesNombres.size(); }
        @Override
        public int getChildrenCount(int groupPosition) {
            List<AudioItem> audios = mapAudiosPorPaciente.get(listPacientesNombres.get(groupPosition));
            return audios != null ? audios.size() : 0;
        }
        @Override
        public Object getGroup(int groupPosition) { return listPacientesNombres.get(groupPosition); }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mapAudiosPorPaciente.get(listPacientesNombres.get(groupPosition)).get(childPosition);
        }
        @Override
        public long getGroupId(int groupPosition) { return groupPosition; }
        @Override
        public long getChildId(int groupPosition, int childPosition) { return childPosition; }
        @Override
        public boolean hasStableIds() { return true; }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_audio_group, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.tvGroupName);
            tv.setText((String) getGroup(groupPosition));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_audio_child, parent, false);
            }
            AudioItem item = (AudioItem) getChild(groupPosition, childPosition);
            TextView tvEje = convertView.findViewById(R.id.tvAudioEjercicio);
            TextView tvFecha = convertView.findViewById(R.id.tvAudioFecha);
            ImageButton btnPlay = convertView.findViewById(R.id.btnPlayAudio);

            tvEje.setText(item.nombreEjercicio);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvFecha.setText(sdf.format(new Date(item.fecha)));

            btnPlay.setOnClickListener(v -> reproducirAudio(item.ref));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
    }

    private static class AudioItem {
        String nombreEjercicio;
        String rawNumEje;
        long fecha;
        StorageReference ref;
    }
}
