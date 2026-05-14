package com.example.tt2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // 🔥 RECIBIR ROL
        rol = getIntent().getStringExtra("ROL");

        if (rol == null) {
            rol = "Paciente"; // fallback
        }

        // 🔥 FRAGMENT INICIAL
        if (savedInstanceState == null) {
            cargarInicio();
        }

        // 🔥 NAVEGACIÓN
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            int id = item.getItemId();

            switch (rol) {

                // 👑 ADMIN
                case "Administrador":

                    if (id == R.id.nav_home) {
                        fragment = new AdministradorHomeFragment();

                    } else if (id == R.id.nav_eje) {
                        fragment = new AdministradorEjerciciosFragment();

                    } else if (id == R.id.nav_rewards) {
                        fragment = new AdministradorRecompensasFragment();

                    } else if (id == R.id.nav_cloud) {
                        fragment = new AdministradorCloudFragment();

                    } else if (id == R.id.nav_profile) {
                        fragment = new PerfilFragment();
                    }
                    break;

                // 👨‍⚕️ TERAPEUTA
                case "Terapeuta":

                    if (id == R.id.nav_home) {
                        fragment = new TerapeutaHomeFragment();

                    } else if (id == R.id.nav_eje) {
                        fragment = new TerapeutaEjerciciosFragment();

                    } else if (id == R.id.nav_rewards) {
                        fragment = new TerapeutaRecompensasFragment();

                    } else if (id == R.id.nav_cloud) {
                        fragment = new TerapeutaCloudFragment();

                    } else if (id == R.id.nav_profile) {
                        fragment = new PerfilFragment();
                    }
                    break;

                // PACIENTE
                default:

                    if (id == R.id.nav_home) {
                        fragment = new PacienteHomeFragment();

                    } else if (id == R.id.nav_eje) {
                        fragment = new PacienteEjerciciosFragment(); // o PacienteEjerciciosFragment

                    } else if (id == R.id.nav_rewards) {
                        fragment = new PacienteRecompensasFragment();

                    } else if (id == R.id.nav_cloud) {
                        fragment = new PacienteCloudFragment();

                    } else if (id == R.id.nav_profile) {
                        fragment = new PerfilFragment();
                    }
                    break;
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

            return true;
        });
    }

    //FRAGMENT INICIAL SEGÚN ROL
    private void cargarInicio() {

        Fragment fragment;

        switch (rol) {

            case "Administrador":
                fragment = new AdministradorHomeFragment();
                break;

            case "Terapeuta":
                fragment = new TerapeutaHomeFragment();
                break;

            default:
                fragment = new PacienteHomeFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}