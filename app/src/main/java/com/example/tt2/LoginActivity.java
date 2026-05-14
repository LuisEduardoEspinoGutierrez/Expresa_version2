package com.example.tt2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    Button btnIngresar, btnRegistrar;
    TextInputEditText etCorreo, etContrasena;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Campos
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);

        // Botones
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Ir a registro
        btnRegistrar.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Login
        btnIngresar.setOnClickListener(v -> loginUsuario());
    }

    private void loginUsuario(){

        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if(TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)){
            Toast.makeText(this,"Completa todos los campos",Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        String userId = auth.getCurrentUser().getUid();

                        // 🔥 Obtener rol desde Firestore
                        db.collection("usuarios")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {

                                    if(documentSnapshot.exists()){

                                        String tipo = documentSnapshot.getString("tipoUsuario");

                                        Toast.makeText(this,"Bienvenido",Toast.LENGTH_SHORT).show();

                                        // 🔥 Enviar rol a MainActivity
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.putExtra("ROL", tipo);
                                        startActivity(intent);
                                        finish();

                                    }else{
                                        Toast.makeText(this,"No hay datos del usuario",Toast.LENGTH_SHORT).show();
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,"Error Firestore: "+e.getMessage(),Toast.LENGTH_LONG).show();
                                });

                    }else{
                        Toast.makeText(this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }

                });
    }
}