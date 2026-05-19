package com.example.tt2;

import android.app.AlertDialog;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
    private Map<String, String> numEjeToNombreMap;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private MediaPlayer mediaPlayer;
    
    private AudioItem currentlyPlayingItem = null;
    private Handler progressHandler = new Handler();
    private Runnable updateSeekBarRunnable;

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
        numEjeToNombreMap = new HashMap<>();
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
        
        Log.d(TAG, "Cargando nombres de ejercicios y usuarios...");
        
        // Primero cargamos los nombres de los ejercicios de Firestore
        db.collection("ejercicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String num = doc.getString("numeroEjercicio");
                        String nombre = doc.getString("nombre");
                        if (num != null && nombre != null) {
                            // Guardamos tanto con punto como con guion para asegurar match
                            numEjeToNombreMap.put(num, nombre);
                            numEjeToNombreMap.put(num.replace(".", "_"), nombre);
                        }
                    }
                    cargarUsuarios();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar ejercicios", e);
                    cargarUsuarios(); // Intentar cargar usuarios aunque fallen los nombres de ejercicios
                });
    }

    private void cargarUsuarios() {
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Paciente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        uidToNameMap.put(doc.getId(), nombre != null ? nombre : "Sin nombre");
                    }
                    buscarAudiosEnStorage();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuarios", e);
                    buscarAudiosEnStorage();
                });
    }

    private void buscarAudiosEnStorage() {
        String[] folders = {"1", "2", "3", "4", "5", "6_1", "6_2", "6_3", "6_4", "6_5", "7_1", "7_2", "7_3", "7_4", "8", "9", "10", "11", "12", "13", "14", "15"};
        
        final int[] pendingRequests = {folders.length};
        for (String f : folders) {
            String folderPath = "audios/ejercicio" + f;
            StorageReference ref = storage.getReference().child(folderPath);
            ref.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    procesarReferenciaAudio(item, f);
                }
                checkProgress(pendingRequests);
            }).addOnFailureListener(e -> {
                checkProgress(pendingRequests);
            });
        }
    }

    private void procesarReferenciaAudio(StorageReference item, String numEje) {
        String fileName = item.getName();
        String[] parts = fileName.split("_");
        
        if (parts.length >= 4) {
            String uid = parts[0];
            String lastPartWithExt = parts[parts.length - 1];
            
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
                String nombreEjeBase = numEje.replace("_", ".");
                String nombreCompleto = numEjeToNombreMap.get(numEje);
                if (nombreCompleto == null) nombreCompleto = numEjeToNombreMap.get(nombreEjeBase);
                
                if (nombreCompleto != null) {
                    audio.nombreEjercicio = "Ejercicio " + nombreEjeBase + ": " + nombreCompleto;
                } else {
                    audio.nombreEjercicio = "Ejercicio " + nombreEjeBase;
                }

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
                    if (comp == 0) return Long.compare(b.fecha, a.fecha);
                    return comp;
                });
            }

            if (progressBar != null) progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            if (listPacientesNombres.isEmpty()) {
                Toast.makeText(getContext(), "No se encontraron grabaciones", Toast.LENGTH_SHORT).show();
            } else {
                elvAudios.expandGroup(0);
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
            if (n1 != n2) return Integer.compare(n1, n2);
            int sub1 = (p1.length > 1) ? Integer.parseInt(p1[1]) : 0;
            int sub2 = (p2.length > 1) ? Integer.parseInt(p2[1]) : 0;
            return Integer.compare(sub1, sub2);
        } catch (Exception e) {
            return e1.compareTo(e2);
        }
    }

    private void reproducirAudio(AudioItem item) {
        if (currentlyPlayingItem != null && currentlyPlayingItem == item && mediaPlayer != null) {
            mediaPlayer.start();
            item.isPlaying = true;
            adapter.notifyDataSetChanged();
            startSeekBarUpdate();
            return;
        }

        detenerAudioActual();

        item.ref.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
                mediaPlayer.setDataSource(getContext(), uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    item.isPlaying = true;
                    currentlyPlayingItem = item;
                    adapter.notifyDataSetChanged();
                    startSeekBarUpdate();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    item.isPlaying = false;
                    currentlyPlayingItem = null;
                    stopSeekBarUpdate();
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al reproducir audio: " + e.getMessage());
                Toast.makeText(getContext(), "Error al reproducir", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "No se pudo obtener el archivo", Toast.LENGTH_SHORT).show();
        });
    }

    private void pausarAudio(AudioItem item) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            item.isPlaying = false;
            stopSeekBarUpdate();
            adapter.notifyDataSetChanged();
        }
    }

    private void detenerAudioActual() {
        stopSeekBarUpdate();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (currentlyPlayingItem != null) {
            currentlyPlayingItem.isPlaying = false;
            currentlyPlayingItem = null;
        }
    }

    private void startSeekBarUpdate() {
        stopSeekBarUpdate();
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && currentlyPlayingItem != null) {
                    adapter.notifyDataSetChanged();
                    progressHandler.postDelayed(this, 500);
                }
            }
        };
        progressHandler.post(updateSeekBarRunnable);
    }

    private void stopSeekBarUpdate() {
        if (updateSeekBarRunnable != null) {
            progressHandler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    @Override
    public void onDestroy() {
        detenerAudioActual();
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
            ImageView ivIndicator = convertView.findViewById(R.id.ivGroupIndicator);
            
            tv.setText((String) getGroup(groupPosition));
            ivIndicator.setRotation(isExpanded ? 90 : 0);
            
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
            ImageButton btnPause = convertView.findViewById(R.id.btnPauseAudio);
            SeekBar sbProgress = convertView.findViewById(R.id.sbAudioProgress);

            tvEje.setText(item.nombreEjercicio);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvFecha.setText(sdf.format(new Date(item.fecha)));

            if (currentlyPlayingItem == item) {
                btnPause.setVisibility(item.isPlaying ? View.VISIBLE : View.GONE);
                btnPlay.setVisibility(item.isPlaying ? View.GONE : View.VISIBLE);
                sbProgress.setVisibility(View.VISIBLE);
                
                if (mediaPlayer != null) {
                    sbProgress.setMax(mediaPlayer.getDuration());
                    sbProgress.setProgress(mediaPlayer.getCurrentPosition());
                }
            } else {
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
                sbProgress.setVisibility(View.GONE);
            }

            btnPlay.setOnClickListener(v -> reproducirAudio(item));
            btnPause.setOnClickListener(v -> pausarAudio(item));
            
            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null && currentlyPlayingItem == item) {
                        mediaPlayer.seekTo(progress);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

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
        boolean isPlaying = false;
    }
}
