package com.example.tt2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    TextView tvNombre, tvCorreo, tvTipoUsuario, tvFecha;
    Button btnCerrarSesion, btnEditar;

    public PerfilFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Conectar vistas
        tvNombre = view.findViewById(R.id.tvNombre);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvTipoUsuario = view.findViewById(R.id.tvTipoUsuario);
        tvFecha = view.findViewById(R.id.tvFecha);

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

        // Editar perfil (placeholder)
        btnEditar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Editar perfil (próximamente)", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    // 🔥 MÉTODO PARA CARGAR DATOS
    private void cargarDatosUsuario(){

        if(auth.getCurrentUser() == null){
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if(documentSnapshot.exists()){

                        String nombre = documentSnapshot.getString("nombre");
                        String correo = documentSnapshot.getString("correo");
                        String tipo = documentSnapshot.getString("tipoUsuario");
                        Long fecha = documentSnapshot.getLong("fechaRegistro");

                        tvNombre.setText(nombre != null ? nombre : "Sin nombre");
                        tvCorreo.setText(correo != null ? correo : "Sin correo");
                        tvTipoUsuario.setText(tipo != null ? tipo : "Sin tipo");

                        // Formatear fecha
                        if(fecha != null){
                            Date date = new Date(fecha);
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            tvFecha.setText("Miembro desde: " + sdf.format(date));
                        }else{
                            tvFecha.setText("Sin fecha");
                        }

                    }else{
                        Toast.makeText(getContext(),"No se encontraron datos",Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                });
    }
}