package com.example.tt2;

import android.app.AlertDialog;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TerapeutaCloudFragment extends Fragment {

    private static final String TAG = "TerapeutaCloud";
    private ExpandableListView elvMedia;
    private ProgressBar progressBar;
    private MediaExpandableAdapter adapter;

    private List<String> listPacientesNombres;
    private Map<String, List<MediaItem>> mapMediaPorPaciente;
    private Map<String, List<Object>> mapItemsPorPaciente;
    private Map<String, String> uidToNameMap;
    private Map<String, String> numEjeToNombreMap;
    private Set<String> seenMediaPaths;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private MediaPlayer mediaPlayer;
    
    private MediaItem currentlyPlayingItem = null;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private String usuarioID;

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
        mapMediaPorPaciente = new HashMap<>();
        mapItemsPorPaciente = new HashMap<>();
        seenMediaPaths = new HashSet<>();

        usuarioID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_terapeuta, container, false);
        elvMedia = view.findViewById(R.id.elvAudios);
        progressBar = view.findViewById(R.id.pbLoadingCloud);
        
        TextView tvTitle = view.findViewById(R.id.tvCloudTitle);
        if (tvTitle != null) {
            tvTitle.setText("Nube de Mis Pacientes (Audios y Videos)");
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new MediaExpandableAdapter();
        elvMedia.setAdapter(adapter);
        fetchInitialData();
    }

    private void fetchInitialData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        db.collection("ejercicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String num = doc.getString("numeroEjercicio");
                        String nombre = doc.getString("nombre");
                        if (num != null && nombre != null) {
                            numEjeToNombreMap.put(num, nombre);
                            numEjeToNombreMap.put(num.replace(".", "_"), nombre);
                        }
                    }
                    cargarUsuarios();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar ejercicios", e);
                    cargarUsuarios();
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
                    cargarMediaVistos();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuarios", e);
                    cargarMediaVistos();
                });
    }

    private void cargarMediaVistos() {
        db.collection("audios_vistos")
                .whereEqualTo("vistoPor", usuarioID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    seenMediaPaths.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String path = doc.getString("audioPath");
                        if (path != null) seenMediaPaths.add(path);
                    }
                    buscarMediaEnStorage();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando media vistos", e);
                    buscarMediaEnStorage();
                });
    }

    private void buscarMediaEnStorage() {
        // Se agregaron las carpetas hasta el ejercicio 20 para incluir el ejercicio 16 y futuros
        // Se añade "17.2" para incluir los videos del ejercicio 17.2
        String[] folders = {"1", "2", "3", "4", "5", "6_1", "6_2", "6_3", "6_4", "6_5", "7_1", "7_2", "7_3", "7_4", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17.1", "17.2","7.3", "7.4", "7.5", "18.1", "18.2", "18.3", "18.4", "18.5"};
        String[] types = {"audios", "videos"};
        
        final int totalRequests = folders.length * types.length;
        final int[] pendingRequests = {totalRequests};
        
        for (String type : types) {
            boolean isAudio = type.equals("audios");
            for (String f : folders) {
                String folderPath = type + "/ejercicio" + f;
                StorageReference ref = storage.getReference().child(folderPath);
                ref.listAll().addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        procesarReferenciaMedia(item, f, isAudio);
                    }
                    checkProgress(pendingRequests);
                }).addOnFailureListener(e -> {
                    checkProgress(pendingRequests);
                });
            }
        }
    }

    private void procesarReferenciaMedia(StorageReference item, String numEje, boolean isAudio) {
        String fileName = item.getName();
        String[] parts = fileName.split("_");
        
        // Se cambió el check a >= 2 para ser más flexible con diferentes formatos de nombre (UID_..._FECHA.ext)
        if (parts.length >= 2) {
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
                
                MediaItem media = new MediaItem();
                String nombreEjeBase = numEje.replace("_", ".");
                String nombreCompleto = numEjeToNombreMap.get(numEje);
                if (nombreCompleto == null) nombreCompleto = numEjeToNombreMap.get(nombreEjeBase);
                
                media.nombreEjercicio = (nombreCompleto != null) 
                        ? "Ejercicio " + nombreEjeBase + ": " + nombreCompleto 
                        : "Ejercicio " + nombreEjeBase;

                media.rawNumEje = numEje;
                media.fecha = timestamp;
                media.ref = item;
                media.visto = seenMediaPaths.contains(item.getPath());
                media.isAudio = isAudio;

                synchronized (this) {
                    List<MediaItem> items = mapMediaPorPaciente.get(pacienteNombre);
                    if (items == null) {
                        items = new ArrayList<>();
                        mapMediaPorPaciente.put(pacienteNombre, items);
                        listPacientesNombres.add(pacienteNombre);
                    }
                    items.add(media);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Nombre de archivo con formato de fecha no numérico: " + fileName);
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
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            Collections.sort(listPacientesNombres);
            for (List<MediaItem> items : mapMediaPorPaciente.values()) {
                Collections.sort(items, (a, b) -> {
                    int comp = compareEjercicios(a.rawNumEje, b.rawNumEje);
                    if (comp == 0) return Long.compare(b.fecha, a.fecha);
                    return comp;
                });
            }

            organizarSecciones();

            if (progressBar != null) progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            if (listPacientesNombres.isEmpty()) {
                Toast.makeText(getContext(), "No se encontraron archivos", Toast.LENGTH_SHORT).show();
            } else {
                if (elvMedia.getExpandableListAdapter() != null && elvMedia.getExpandableListAdapter().getGroupCount() > 0) {
                    elvMedia.expandGroup(0);
                }
            }
        });
    }

    private void organizarSecciones() {
        mapItemsPorPaciente.clear();
        for (String paciente : listPacientesNombres) {
            List<MediaItem> allItems = mapMediaPorPaciente.get(paciente);
            if (allItems == null) continue;

            List<MediaItem> recentAudio = new ArrayList<>();
            List<MediaItem> recentVideo = new ArrayList<>();
            List<MediaItem> historyAudio = new ArrayList<>();
            List<MediaItem> historyVideo = new ArrayList<>();

            for (MediaItem item : allItems) {
                if (item.visto) {
                    if (item.isAudio) historyAudio.add(item);
                    else historyVideo.add(item);
                } else {
                    if (item.isAudio) recentAudio.add(item);
                    else recentVideo.add(item);
                }
            }

            List<Object> items = new ArrayList<>();
            if (!recentAudio.isEmpty()) { items.add("Audios recientes"); items.addAll(recentAudio); }
            if (!recentVideo.isEmpty()) { items.add("Videos recientes"); items.addAll(recentVideo); }
            if (!historyAudio.isEmpty()) { items.add("Historial de audios"); items.addAll(historyAudio); }
            if (!historyVideo.isEmpty()) { items.add("Historial de videos"); items.addAll(historyVideo); }
            mapItemsPorPaciente.put(paciente, items);
        }
    }

    private int compareEjercicios(String e1, String e2) {
        try {
            if (e1 == null || e2 == null) return 0;
            
            // Primero intentamos comparar como números decimales si contienen puntos
            if (e1.contains(".") || e2.contains(".")) {
                double d1 = Double.parseDouble(e1.replace("_", "."));
                double d2 = Double.parseDouble(e2.replace("_", "."));
                return Double.compare(d1, d2);
            }

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

    private void marcarComoVisto(MediaItem item) {
        if (item.visto || usuarioID.equals("anonimo")) return;
        
        item.visto = true;
        seenMediaPaths.add(item.ref.getPath());
        
        Map<String, Object> data = new HashMap<>();
        data.put("audioPath", item.ref.getPath());
        data.put("vistoPor", usuarioID);
        data.put("fechaVisto", System.currentTimeMillis());

        db.collection("audios_vistos")
                .add(data)
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar visto", e));
        
        organizarSecciones();
        adapter.notifyDataSetChanged();
    }

    private void reproducirMedia(MediaItem item) {
        if (!item.isAudio) {
            mostrarVideo(item);
            return;
        }

        if (currentlyPlayingItem == item && mediaPlayer != null) {
            mediaPlayer.start();
            item.isPlaying = true;
            adapter.notifyDataSetChanged();
            startSeekBarUpdate();
            return;
        }

        detenerMediaActual();

        item.ref.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
                mediaPlayer.setDataSource(requireContext(), uri);
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
                    marcarComoVisto(item);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al reproducir audio", e);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener archivo", Toast.LENGTH_SHORT).show());
    }

    private void mostrarVideo(MediaItem item) {
        detenerMediaActual();
        item.ref.getDownloadUrl().addOnSuccessListener(uri -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_video_player, null);
            VideoView videoView = dialogView.findViewById(R.id.vvCloudPlayer);
            ProgressBar pbVideo = dialogView.findViewById(R.id.pbVideoLoading);
            
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            MediaController mediaController = new MediaController(requireContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            
            videoView.setOnPreparedListener(mp -> {
                pbVideo.setVisibility(View.GONE);
                videoView.start();
            });

            videoView.setOnCompletionListener(mp -> marcarComoVisto(item));

            dialog.show();
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar video", Toast.LENGTH_SHORT).show());
    }

    private void pausarAudio(MediaItem item) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            item.isPlaying = false;
            stopSeekBarUpdate();
            adapter.notifyDataSetChanged();
        }
    }

    private void detenerMediaActual() {
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
                if (mediaPlayer != null && currentlyPlayingItem != null && currentlyPlayingItem.isPlaying) {
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
        detenerMediaActual();
        super.onDestroy();
    }

    private class MediaExpandableAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() { return listPacientesNombres.size(); }
        @Override
        public int getChildrenCount(int groupPosition) {
            List<Object> items = mapItemsPorPaciente.get(listPacientesNombres.get(groupPosition));
            return items != null ? items.size() : 0;
        }
        @Override
        public Object getGroup(int groupPosition) { return listPacientesNombres.get(groupPosition); }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            List<Object> items = mapItemsPorPaciente.get(listPacientesNombres.get(groupPosition));
            return items != null ? items.get(childPosition) : null;
        }
        @Override
        public long getGroupId(int groupPosition) { return groupPosition; }
        @Override
        public long getChildId(int groupPosition, int childPosition) { return childPosition; }
        @Override
        public boolean hasStableIds() { return false; }
        @Override
        public int getChildTypeCount() { return 2; }
        @Override
        public int getChildType(int groupPosition, int childPosition) {
            return (getChild(groupPosition, childPosition) instanceof String) ? 0 : 1;
        }

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
            int type = getChildType(groupPosition, childPosition);

            if (type == 0) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_audio_subheader, parent, false);
                }
                ((TextView) convertView.findViewById(R.id.tvSubheader)).setText((String) getChild(groupPosition, childPosition));
                return convertView;
            }

            if (convertView == null || convertView.findViewById(R.id.tvAudioEjercicio) == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_audio_child, parent, false);
            }
            
            MediaItem item = (MediaItem) getChild(groupPosition, childPosition);
            TextView tvEje = convertView.findViewById(R.id.tvAudioEjercicio);
            TextView tvFecha = convertView.findViewById(R.id.tvAudioFecha);
            ImageButton btnPlay = convertView.findViewById(R.id.btnPlayAudio);
            ImageButton btnPause = convertView.findViewById(R.id.btnPauseAudio);
            SeekBar sbProgress = convertView.findViewById(R.id.sbAudioProgress);
            ImageView ivType = convertView.findViewById(R.id.ivMediaType);

            tvEje.setText(item.nombreEjercicio);
            tvFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(item.fecha)));
            
            if (ivType != null) {
                ivType.setImageResource(item.isAudio ? android.R.drawable.ic_btn_speak_now : android.R.drawable.presence_video_online);
            }

            if (currentlyPlayingItem == item && item.isAudio) {
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

            btnPlay.setOnClickListener(v -> reproducirMedia(item));
            btnPause.setOnClickListener(v -> pausarAudio(item));
            
            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null && currentlyPlayingItem == item) mediaPlayer.seekTo(progress);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            return convertView;
        }

        @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return getChildType(groupPosition, childPosition) == 1; }
    }

    private static class MediaItem {
        String nombreEjercicio;
        String rawNumEje;
        long fecha;
        StorageReference ref;
        boolean isPlaying = false;
        boolean visto = false;
        boolean isAudio = true;
    }
}
