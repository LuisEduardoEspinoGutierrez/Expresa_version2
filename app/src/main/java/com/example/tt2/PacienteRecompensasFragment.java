package com.example.tt2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class PacienteRecompensasFragment extends Fragment {

    public PacienteRecompensasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recompensas_paciente, container, false);

        // Botón regresar
        ImageView btnRegresar = view.findViewById(R.id.btnRegresar);

        btnRegresar.setOnClickListener(v -> {
            requireActivity().onBackPressed(); // simula el "back"
        });

        return view;
    }
}