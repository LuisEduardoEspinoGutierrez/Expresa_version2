package com.example.tt2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class AudioFragment extends Fragment {

    Button btnGrabar, btnDetener, btnSubir;
    MediaRecorder recorder;
    String filePath;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    public AudioFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 Nuevo sistema de permisos
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Toast.makeText(getContext(), "Permiso concedido", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        btnGrabar = view.findViewById(R.id.btnGrabar);
        btnDetener = view.findViewById(R.id.btnDetener);
        btnSubir = view.findViewById(R.id.btnSubir);

        // Botón grabar
        btnGrabar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {

                startRecording();

            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnDetener.setOnClickListener(v -> stopRecording());
        btnSubir.setOnClickListener(v -> uploadAudio());

        return view;
    }

    // 🎤 INICIAR GRABACIÓN
    private void startRecording() {
        try {

            File file = new File(getContext().getExternalFilesDir(null), "audio.3gp");
            filePath = file.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();

            Toast.makeText(getContext(), "🎤 Grabando...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al grabar", Toast.LENGTH_SHORT).show();
        }
    }

    // ⏹️ DETENER GRABACIÓN
    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;

                Toast.makeText(getContext(), "Grabación detenida", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al detener", Toast.LENGTH_SHORT).show();
        }
    }

    // ☁️ SUBIR AUDIO
    private void uploadAudio() {

        if (filePath == null) {
            Toast.makeText(getContext(), "Primero graba un audio", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileObj = new File(filePath);

        if (!fileObj.exists()) {
            Toast.makeText(getContext(), "El archivo no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri file = Uri.fromFile(fileObj);

        StorageReference ref = storageRef.child("audios/audio_" + System.currentTimeMillis() + ".3gp");

        ref.putFile(file)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String url = uri.toString();

                        Toast.makeText(getContext(), "Audio subido!", Toast.LENGTH_SHORT).show();
                        Log.d("AUDIO_URL", url);
                    });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.e("ERROR_FIREBASE", e.getMessage());
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}