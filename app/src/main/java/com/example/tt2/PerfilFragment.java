package com.example.tt2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private TextView tvNombre, tvCorreo, tvTipoUsuario, tvFecha;
    private Button btnCerrarSesion, btnEditar;
    private ImageView imgPerfil;
    private FloatingActionButton fabEditPhoto;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public PerfilFragment() {
    }

    public static PerfilFragment newInstance() {
        return new PerfilFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar el selector de imágenes
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            subirFotoAFirebase(imageUri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Conectar vistas
        tvNombre = view.findViewById(R.id.tvNombre);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvTipoUsuario = view.findViewById(R.id.tvTipoUsuario);
        tvFecha = view.findViewById(R.id.tvFecha);
        imgPerfil = view.findViewById(R.id.imgPerfil);
        fabEditPhoto = view.findViewById(R.id.fabEditPhoto);

        btnCerrarSesion = view.findViewById(R.id.btnLogout);
        btnEditar = view.findViewById(R.id.btnEditar);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Seleccionar foto de perfil
        fabEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Editar perfil
        btnEditar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función de edición próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatosUsuario() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String correo = documentSnapshot.getString("correo");
                        String tipo = documentSnapshot.getString("tipoUsuario");
                        Long fecha = documentSnapshot.getLong("fechaRegistro");
                        String fotoUrl = documentSnapshot.getString("fotoUrl");

                        tvNombre.setText(nombre != null ? nombre : "Usuario");
                        tvCorreo.setText(correo != null ? correo : auth.getCurrentUser().getEmail());
                        tvTipoUsuario.setText(tipo != null ? tipo : "No definido");

                        // Cargar imagen con Glide
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.user)
                                    .into(imgPerfil);
                        }

                        if (fecha != null) {
                            Date date = new Date(fecha);
                            SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("es", "ES"));
                            tvFecha.setText(sdf.format(date));
                        } else {
                            tvFecha.setText("N/A");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subirFotoAFirebase(Uri uri) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        StorageReference fileRef = storage.getReference().child("perfiles/" + userId + ".jpg");

        Toast.makeText(getContext(), "Subiendo foto...", Toast.LENGTH_SHORT).show();

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        actualizarUrlFotoEnFirestore(downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir foto", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarUrlFotoEnFirestore(String url) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("fotoUrl", url);

        db.collection("usuarios").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                        Glide.with(this).load(url).into(imgPerfil);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
