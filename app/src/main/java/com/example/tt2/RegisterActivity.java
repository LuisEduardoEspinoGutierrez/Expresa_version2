package com.example.tt2;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

import android.widget.ArrayAdapter;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    FirebaseAuth auth;
    FirebaseFirestore db;

    Button btnCancelar, btnRegistrar;
    ImageView ivRegresar;

    EditText etNombre, etCorreo, etContrasena, etConfirmarContrasena;
    AutoCompleteTextView actTipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Conectar campos
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        actTipoUsuario = findViewById(R.id.actTipoUsuario);

        // Conectar botones
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Conectar imagen
        ivRegresar = findViewById(R.id.ivRegresar);

        // Botón registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        // Botón cancelar
        btnCancelar.setOnClickListener(v -> finish());

        // Imagen regresar
        ivRegresar.setOnClickListener(v -> finish());

        // Tipos de usuario para el dropdown
        String[] tiposUsuario = {"Terapeuta", "Paciente", "Administrador"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tiposUsuario
        );
        actTipoUsuario.setAdapter(adapter);
    }

    private void registrarUsuario(){
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String tipoUsuario = actTipoUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String confirmar = etConfirmarContrasena.getText().toString().trim();

        // Validaciones
        if(TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo) ||
                TextUtils.isEmpty(contrasena) || TextUtils.isEmpty(confirmar) ||
                TextUtils.isEmpty(tipoUsuario)){
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!contrasena.equals(confirmar)){
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false); // Deshabilitar para evitar múltiples envíos
        Log.d(TAG, "Iniciando registro para: " + correo);

        // 1. Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        Log.d(TAG, "Auth exitoso. UID: " + userId);
                        guardarEnFirestore(userId, nombre, correo, tipoUsuario);
                    } else {
                        btnRegistrar.setEnabled(true);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "Error en Auth: " + errorMsg);

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error Auth: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void guardarEnFirestore(String userId, String nombre, String correo, String tipoUsuario) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("correo", correo);
        usuario.put("tipoUsuario", tipoUsuario);
        usuario.put("fechaRegistro", System.currentTimeMillis());
        usuario.put("usuarioid", userId); // Agregado por consistencia con tu captura

        Log.d(TAG, "Guardando datos en Firestore...");

        db.collection("usuarios")
                .document(userId)
                .set(usuario)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Firestore: Datos guardados correctamente ✅");
                    Toast.makeText(this, "Registro exitoso y guardado en Firestore ✅", Toast.LENGTH_LONG).show();
                    finish(); // Cerrar actividad al terminar
                })
                .addOnFailureListener(e -> {
                    btnRegistrar.setEnabled(true);
                    Log.e(TAG, "Firestore: Error al guardar datos", e);
                    Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}