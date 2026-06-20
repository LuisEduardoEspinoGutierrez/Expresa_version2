package com.example.tt2.ejercicios;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tt2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio08 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Ejercicio08";
    private int wordsFoundCount = 0;
    private SopaDeLetrasViewDalia sopa;
    private ImageView eje08_img1, eje08_img2, eje08_img3, eje08_img4, eje08_img5, eje08_img6, eje08_img7, eje08_img8, eje08_img9, eje08_img10, eje08_img11, eje08_img12, eje08_img13, eje08_img14, eje08_img15, eje08_img16, eje08_img17, eje08_img18, eje08_img19, eje08_img20, eje08_img21, eje08_img22, eje08_img23, eje08_img24, eje08_img25, eje08_img26, eje08_img27, eje08_img28, eje08_img29, eje08_img30;
    private ImageView ivRegresarEje08;
    private Button btnAudioInstruccionesEje08, btnFinalizarEje08;

    private MediaPlayer mp;
    private MediaPlayer mediaPlayerInstrucciones;

    private String usuarioID;
    private final String numeroEjercicio = "8";
    private FirebaseFirestore db;
    private List<String> palabrasEncontradas = new ArrayList<>();

    private String idAsignacionActual = "";
    private boolean isDataLoaded = false;

    @Override
    protected void onDestroy() {
        if(mp != null){
            mp.release();
            mp = null;
        }
        if (mediaPlayerInstrucciones != null) {
            mediaPlayerInstrucciones.release();
            mediaPlayerInstrucciones = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ejercicio08);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            usuarioID = "anonimo";
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivRegresarEje08 = findViewById(R.id.ivRegresarEje08);
        btnAudioInstruccionesEje08 = findViewById(R.id.btnAudioInstruccionesEje08);
        btnFinalizarEje08 = findViewById(R.id.btnFinalizarEje08);

        initImageViews();

        sopa = findViewById(R.id.sopaDeLetrasViewEje08);
        sopa.setGridSize(17, 17);
        setupSopa(sopa);

        sopa.setOnWordFoundListener(word -> {
            if (!palabrasEncontradas.contains(word.toUpperCase())) {
                palabrasEncontradas.add(word.toUpperCase());
                cambiarImagen(word);
                wordsFoundCount = palabrasEncontradas.size();
                playWordAudio(word);
                guardarProgreso();

                if (wordsFoundCount == 30) {
                    Toast.makeText(this, "¡Felicidades, has encontrado todas las palabras!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mediaPlayerInstrucciones = MediaPlayer.create(this, R.raw.instrucciones_eje8);

        setClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarConfirmacionSalida();
            }
        });

        cargarAsignacionYProgreso();
    }

    private void initImageViews() {
        eje08_img1 = findViewById(R.id.eje08_img1);
        eje08_img2 = findViewById(R.id.eje08_img2);
        eje08_img3 = findViewById(R.id.eje08_img3);
        eje08_img4 = findViewById(R.id.eje08_img4);
        eje08_img5 = findViewById(R.id.eje08_img5);
        eje08_img6 = findViewById(R.id.eje08_img6);
        eje08_img7 = findViewById(R.id.eje08_img7);
        eje08_img8 = findViewById(R.id.eje08_img8);
        eje08_img9 = findViewById(R.id.eje08_img9);
        eje08_img10 = findViewById(R.id.eje08_img10);
        eje08_img11 = findViewById(R.id.eje08_img11);
        eje08_img12 = findViewById(R.id.eje08_img12);
        eje08_img13 = findViewById(R.id.eje08_img13);
        eje08_img14 = findViewById(R.id.eje08_img14);
        eje08_img15 = findViewById(R.id.eje08_img15);
        eje08_img16 = findViewById(R.id.eje08_img16);
        eje08_img17 = findViewById(R.id.eje08_img17);
        eje08_img18 = findViewById(R.id.eje08_img18);
        eje08_img19 = findViewById(R.id.eje08_img19);
        eje08_img20 = findViewById(R.id.eje08_img20);
        eje08_img21 = findViewById(R.id.eje08_img21);
        eje08_img22 = findViewById(R.id.eje08_img22);
        eje08_img23 = findViewById(R.id.eje08_img23);
        eje08_img24 = findViewById(R.id.eje08_img24);
        eje08_img25 = findViewById(R.id.eje08_img25);
        eje08_img26 = findViewById(R.id.eje08_img26);
        eje08_img27 = findViewById(R.id.eje08_img27);
        eje08_img28 = findViewById(R.id.eje08_img28);
        eje08_img29 = findViewById(R.id.eje08_img29);
        eje08_img30 = findViewById(R.id.eje08_img30);
    }

    private void setClickListeners() {
        ivRegresarEje08.setOnClickListener(this);
        btnAudioInstruccionesEje08.setOnClickListener(this);
        btnFinalizarEje08.setOnClickListener(this);
        eje08_img1.setOnClickListener(this);
        eje08_img2.setOnClickListener(this);
        eje08_img3.setOnClickListener(this);
        eje08_img4.setOnClickListener(this);
        eje08_img5.setOnClickListener(this);
        eje08_img6.setOnClickListener(this);
        eje08_img7.setOnClickListener(this);
        eje08_img8.setOnClickListener(this);
        eje08_img9.setOnClickListener(this);
        eje08_img10.setOnClickListener(this);
        eje08_img11.setOnClickListener(this);
        eje08_img12.setOnClickListener(this);
        eje08_img13.setOnClickListener(this);
        eje08_img14.setOnClickListener(this);
        eje08_img15.setOnClickListener(this);
        eje08_img16.setOnClickListener(this);
        eje08_img17.setOnClickListener(this);
        eje08_img18.setOnClickListener(this);
        eje08_img19.setOnClickListener(this);
        eje08_img20.setOnClickListener(this);
        eje08_img21.setOnClickListener(this);
        eje08_img22.setOnClickListener(this);
        eje08_img23.setOnClickListener(this);
        eje08_img24.setOnClickListener(this);
        eje08_img25.setOnClickListener(this);
        eje08_img26.setOnClickListener(this);
        eje08_img27.setOnClickListener(this);
        eje08_img28.setOnClickListener(this);
        eje08_img29.setOnClickListener(this);
        eje08_img30.setOnClickListener(this);
    }

    private void cargarAsignacionYProgreso() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }

        db.collection("pacientes_ejercicios")
                .whereEqualTo("idPaciente", usuarioID)
                .whereEqualTo("logicalId", numeroEjercicio)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        idAsignacionActual = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "Asignación actual: " + idAsignacionActual);
                    }
                    cargarProgreso();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo asignación", e);
                    cargarProgreso();
                });
    }

    private void cargarProgreso() {
        if (usuarioID.equals("anonimo")) {
            isDataLoaded = true;
            return;
        }
        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> palabras = (List<String>) documentSnapshot.get("palabrasEncontradas");
                        if (palabras != null) {
                            palabrasEncontradas = palabras;
                            wordsFoundCount = palabrasEncontradas.size();
                            for (String word : palabrasEncontradas) {
                                cambiarImagen(word);
                                sopa.marcarPalabraComoEncontrada(word);
                            }
                        }
                    }
                    isDataLoaded = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando progreso", e);
                    isDataLoaded = true;
                });
    }

    private void guardarProgreso() {
        if (usuarioID.equals("anonimo")) return;
        int porcentaje = (wordsFoundCount * 100) / 30;
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("idPaciente", usuarioID);
        progreso.put("logicalId", numeroEjercicio);
        progreso.put("porcentaje", porcentaje);
        progreso.put("palabrasEncontradas", palabrasEncontradas);
        progreso.put("completado", wordsFoundCount == 30);
        progreso.put("ultimaModificacion", System.currentTimeMillis());

        db.collection("progreso_ejercicios")
                .document(usuarioID + "_" + numeroEjercicio)
                .set(progreso, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progreso guardado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar progreso", e));
    }

    private void procesarFinalizacionConRecompensa() {
        if (usuarioID.equals("anonimo") || !isDataLoaded || idAsignacionActual.isEmpty()) {
            guardarProgreso();
            finish();
            return;
        }

        db.runTransaction(transaction -> {
            DocumentReference asigRef = db.collection("pacientes_ejercicios").document(idAsignacionActual);
            DocumentReference userRef = db.collection("usuarios").document(usuarioID);

            DocumentSnapshot asigSnap = transaction.get(asigRef);
            Boolean entregada = asigSnap.getBoolean("recompensaEntregada");

            if (entregada == null || !entregada) {
                transaction.update(asigRef, "recompensaEntregada", true);
                transaction.update(userRef, "puntos", FieldValue.increment(5));
                return true;
            }
            return false;
        }).addOnSuccessListener(recompensaOtorgada -> {
            guardarProgreso();
            if (recompensaOtorgada) {
                mostrarToastConPuntos("¡Felicidades! Has ganado 5 puntos. Ahora tienes ");
            } else {
                mostrarToastConPuntos("¡Excelente trabajo! Recuerda que ya tienes ");
            }
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error en transacción de recompensa", e);
            guardarProgreso();
            finish();
        });
    }

    private void mostrarToastConPuntos(String mensajeBase) {
        db.collection("usuarios").document(usuarioID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long puntos = 0L;
                    if (documentSnapshot.exists()) {
                        puntos = documentSnapshot.getLong("puntos");
                        if (puntos == null) puntos = 0L;
                    }
                    Toast.makeText(Ejercicio08.this, mensajeBase + puntos + " puntos en recompensas.", Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarConfirmacionSalida() {
        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir?")
                .setMessage("Tu progreso se guardará automáticamente.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    guardarProgreso();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void reproducirAudios(int... audios){
        if(mp != null){
            try { mp.release(); } catch (Exception e){ e.printStackTrace(); }
            mp = null;
        }
        if(audios.length == 0) return;
        reproducirSecuencia(audios, 0);
    }

    private void reproducirSecuencia(int[] audios, int index){
        mp = MediaPlayer.create(this, audios[index]);
        if(mp == null) return;
        mp.start();
        mp.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.release();
            int siguiente = index + 1;
            if(siguiente < audios.length){
                reproducirSecuencia(audios, siguiente);
            }
        });
    }

    private void cambiarImagen(String word) {
        switch (word.toUpperCase()) {
            case "CALAMAR": eje08_img1.setImageResource(R.drawable.eje08_img1_cc); break;
            case "CARACOL": eje08_img2.setImageResource(R.drawable.eje08_img2_cc); break;
            case "CARAMELO": eje08_img3.setImageResource(R.drawable.eje08_img3_cc); break;
            case "CARTA": eje08_img4.setImageResource(R.drawable.eje08_img4_cc); break;
            case "CORONA": eje08_img5.setImageResource(R.drawable.eje08_img5_cc); break;
            case "DINERO": eje08_img6.setImageResource(R.drawable.eje08_img6_cc); break;
            case "FAROLA": eje08_img7.setImageResource(R.drawable.eje08_img7_cc); break;
            case "HOGUERA": eje08_img8.setImageResource(R.drawable.eje08_img8_cc); break;
            case "HORMIGA": eje08_img9.setImageResource(R.drawable.eje08_img9_cc); break;
            case "TORTUGA": eje08_img10.setImageResource(R.drawable.eje08_img10_cc); break;
            case "TORO": eje08_img11.setImageResource(R.drawable.eje08_img11_cc); break;
            case "TESORO": eje08_img12.setImageResource(R.drawable.eje08_img12_cc); break;
            case "SIRENA": eje08_img13.setImageResource(R.drawable.eje08_img13_cc); break;
            case "RUEDA": eje08_img14.setImageResource(R.drawable.eje08_img14_cc); break;
            case "ROSA": eje08_img15.setImageResource(R.drawable.eje08_img15_cc); break;
            case "REMO": eje08_img16.setImageResource(R.drawable.eje08_img16_cc); break;
            case "RATÓN": eje08_img17.setImageResource(R.drawable.eje08_img17_cc); break;
            case "RAQUETA": eje08_img18.setImageResource(R.drawable.eje08_img18_cc); break;
            case "RANA": eje08_img19.setImageResource(R.drawable.eje08_img19_cc); break;
            case "RAMO": eje08_img20.setImageResource(R.drawable.eje08_img20_cc); break;
            case "PIRATA": eje08_img21.setImageResource(R.drawable.eje08_img21_cc); break;
            case "PERFUME": eje08_img22.setImageResource(R.drawable.eje08_img22_cc); break;
            case "PERCHA": eje08_img23.setImageResource(R.drawable.eje08_img23_cc); break;
            case "PERA": eje08_img24.setImageResource(R.drawable.eje08_img24_cc); break;
            case "PARAGUAS": eje08_img25.setImageResource(R.drawable.eje08_img25_cc); break;
            case "MARIPOSA": eje08_img26.setImageResource(R.drawable.eje08_img26_cc); break;
            case "MARIQUITA": eje08_img27.setImageResource(R.drawable.eje08_img27_cc); break;
            case "LORO": eje08_img28.setImageResource(R.drawable.eje08_img28_cc); break;
            case "PAPELERA": eje08_img29.setImageResource(R.drawable.eje08_img29_cc); break;
            case "PIRULETA": eje08_img30.setImageResource(R.drawable.eje08_img30_cc); break;
        }
    }

    private void setupSopa(SopaDeLetrasViewDalia sopa) {
        String[] template = {
                "CORONARAMOPERAXXX",
                "XXPIRATACARAMELOR",
                "REMOXXXXXXROSAXXU",
                "XXXCARACOLXXXFTXE",
                "XXXXMARIPOSAHAOXD",
                "XXXXXXXXXXXXORRXA",
                "XXXXCALAMARXROTHX",
                "XXXXXXXXXXXXMLUOX",
                "XPERCHACARTAIAGGX",
                "LOROXXXXTOROGXAUT",
                "XSIRENAXXXXXAPXEE",
                "XXXXMARIQUITAEXRS",
                "PIRULETAXXXXXRXAO",
                "RAQUETAXXXXXXFXXR",
                "PAPELERAXXXXXUXXO",
                "DINERORATÓNXXMXXX",
                "PARAGUASRANAXEXXX"
        };

        List<SopaDeLetrasViewDalia.Word> words = new ArrayList<>();
        words.add(new SopaDeLetrasViewDalia.Word("CORONA", 0, 0, 0, 5));
        words.add(new SopaDeLetrasViewDalia.Word("RAMO", 0, 6, 0, 9));
        words.add(new SopaDeLetrasViewDalia.Word("PERA", 0, 10, 0, 13));
        words.add(new SopaDeLetrasViewDalia.Word("PIRATA", 1, 2, 1, 7));
        words.add(new SopaDeLetrasViewDalia.Word("CARAMELO", 1, 8, 1, 15));
        words.add(new SopaDeLetrasViewDalia.Word("REMO", 2, 0, 2, 3));
        words.add(new SopaDeLetrasViewDalia.Word("ROSA", 2, 10, 2, 13));
        words.add(new SopaDeLetrasViewDalia.Word("CARACOL", 3, 3, 3, 9));
        words.add(new SopaDeLetrasViewDalia.Word("MARIPOSA", 4, 4, 4, 11));
        words.add(new SopaDeLetrasViewDalia.Word("CALAMAR", 6, 4, 6, 10));
        words.add(new SopaDeLetrasViewDalia.Word("PERCHA", 8, 1, 8, 6));
        words.add(new SopaDeLetrasViewDalia.Word("CARTA", 8, 7, 8, 11));
        words.add(new SopaDeLetrasViewDalia.Word("LORO", 9, 0, 9, 3));
        words.add(new SopaDeLetrasViewDalia.Word("TORO", 9, 8, 9, 11));
        words.add(new SopaDeLetrasViewDalia.Word("SIRENA", 10, 1, 10, 6));
        words.add(new SopaDeLetrasViewDalia.Word("MARIQUITA", 11, 4, 11, 12));
        words.add(new SopaDeLetrasViewDalia.Word("PIRULETA", 12, 0, 12, 7));
        words.add(new SopaDeLetrasViewDalia.Word("RAQUETA", 13, 0, 13, 6));
        words.add(new SopaDeLetrasViewDalia.Word("PAPELERA", 14, 0, 14, 7));
        words.add(new SopaDeLetrasViewDalia.Word("DINERO", 15, 0, 15,5));
        words.add(new SopaDeLetrasViewDalia.Word("RATÓN", 15, 6, 15, 10));
        words.add(new SopaDeLetrasViewDalia.Word("PARAGUAS", 16, 0, 16, 7));
        words.add(new SopaDeLetrasViewDalia.Word("RANA", 16, 8, 16, 11));
        words.add(new SopaDeLetrasViewDalia.Word("RUEDA", 1, 16, 5, 16));
        words.add(new SopaDeLetrasViewDalia.Word("HORMIGA", 4, 12, 10, 12));
        words.add(new SopaDeLetrasViewDalia.Word("FAROLA", 3, 13, 8, 13));
        words.add(new SopaDeLetrasViewDalia.Word("TORTUGA", 3, 14, 9, 14));
        words.add(new SopaDeLetrasViewDalia.Word("HOGUERA", 6, 15, 12, 15));
        words.add(new SopaDeLetrasViewDalia.Word("PERFUME", 10, 13, 16, 13));
        words.add(new SopaDeLetrasViewDalia.Word("TESORO", 9, 16, 14, 16));

        sopa.setBoard(template, words);
    }

    private void playWordAudio(String word) {
        String resourceName = "tr_" + word.toLowerCase();
        int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());
        if (resId != 0) { playAudio(resId); } else { playAudio(R.raw.muy_bien); }
    }

    private void playAudio(int resId) {
        if (mp != null) { mp.release(); }
        mp = MediaPlayer.create(this, resId);
        if (mp != null) { mp.start(); }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRegresarEje08) { mostrarConfirmacionSalida(); }
        else if (id == R.id.btnAudioInstruccionesEje08) {
            if (mediaPlayerInstrucciones != null) {
                if (mediaPlayerInstrucciones.isPlaying()) { mediaPlayerInstrucciones.seekTo(0); }
                else { mediaPlayerInstrucciones.start(); }
            }
        } else if (id == R.id.btnFinalizarEje08) {
            if (wordsFoundCount == 30) {
                procesarFinalizacionConRecompensa();
            } else {
                Toast.makeText(this, "Aún faltan " + (30 - wordsFoundCount) + " palabra(s) por encontrar!", Toast.LENGTH_SHORT).show();
                reproducirAudios(R.raw.no_has_terminado);
            }
        } else if (id == R.id.eje08_img1) { reproducirAudios(R.raw.audio_img1_eje8); }
        else if (id == R.id.eje08_img2) { reproducirAudios(R.raw.audio_img2_eje8); }
        else if (id == R.id.eje08_img3) { reproducirAudios(R.raw.audio_img3_eje8); }
        else if (id == R.id.eje08_img4) { reproducirAudios(R.raw.audio_img4_eje8); }
        else if (id == R.id.eje08_img5) { reproducirAudios(R.raw.audio_img5_eje8); }
        else if (id == R.id.eje08_img6) { reproducirAudios(R.raw.audio_img6_eje8); }
        else if (id == R.id.eje08_img7) { reproducirAudios(R.raw.audio_img7_eje8); }
        else if (id == R.id.eje08_img8) { reproducirAudios(R.raw.audio_img8_eje8); }
        else if (id == R.id.eje08_img9) { reproducirAudios(R.raw.audio_img9_eje8); }
        else if (id == R.id.eje08_img10) { reproducirAudios(R.raw.audio_img10_eje8); }
        else if (id == R.id.eje08_img11) { reproducirAudios(R.raw.audio_img11_eje8); }
        else if (id == R.id.eje08_img12) { reproducirAudios(R.raw.audio_img12_eje8); }
        else if (id == R.id.eje08_img13) { reproducirAudios(R.raw.audio_img13_eje8); }
        else if (id == R.id.eje08_img14) { reproducirAudios(R.raw.audio_img14_eje8); }
        else if (id == R.id.eje08_img15) { reproducirAudios(R.raw.audio_img15_eje8); }
        else if (id == R.id.eje08_img16) { reproducirAudios(R.raw.audio_img16_eje8); }
        else if (id == R.id.eje08_img17) { reproducirAudios(R.raw.audio_img17_eje8); }
        else if (id == R.id.eje08_img18) { reproducirAudios(R.raw.audio_img18_eje8); }
        else if (id == R.id.eje08_img19) { reproducirAudios(R.raw.audio_img19_eje8); }
        else if (id == R.id.eje08_img20) { reproducirAudios(R.raw.audio_img20_eje8); }
        else if (id == R.id.eje08_img21) { reproducirAudios(R.raw.audio_img21_eje8); }
        else if (id == R.id.eje08_img22) { reproducirAudios(R.raw.audio_img22_eje8); }
        else if (id == R.id.eje08_img23) { reproducirAudios(R.raw.audio_img23_eje8); }
        else if (id == R.id.eje08_img24) { reproducirAudios(R.raw.audio_img24_eje8); }
        else if (id == R.id.eje08_img25) { reproducirAudios(R.raw.audio_img25_eje8); }
        else if (id == R.id.eje08_img26) { reproducirAudios(R.raw.audio_img26_eje8); }
        else if (id == R.id.eje08_img27) { reproducirAudios(R.raw.audio_img27_eje8); }
        else if (id == R.id.eje08_img28) { reproducirAudios(R.raw.audio_img28_eje8); }
        else if (id == R.id.eje08_img29) { reproducirAudios(R.raw.audio_img29_eje8); }
        else if (id == R.id.eje08_img30) { reproducirAudios(R.raw.audio_img30_eje8); }
    }
}
