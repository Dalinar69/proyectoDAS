package com.example.proyectoindiv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class EstadisticasFragment extends Fragment {

    private TextView tvTotalJuegos;
    private TextView tvTiempoMedio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadisticas, container, false);
        tvTotalJuegos = view.findViewById(R.id.tvTotalJuegos);
        tvTiempoMedio = view.findViewById(R.id.tvTiempoMedio);
        return view;
    }

    public void actualizarDatos(List<JuegoMesa> listaJuegos) {
        if (tvTotalJuegos != null && tvTiempoMedio != null && getContext() != null) {

            int total = listaJuegos.size();
            int sumaTiempo = 0;

            // Recorremos los juegos para sumar el tiempo de todos
            for (JuegoMesa juego : listaJuegos) {
                sumaTiempo += juego.getDuracion();
            }

            // Calculamos la media
            int tiempoMedio = total > 0 ? (sumaTiempo / total) : 0;

            // Ponemos los textos traducidos desde el diccionario
            tvTotalJuegos.setText(getString(R.string.stat_total) + " " + total);
            tvTiempoMedio.setText(getString(R.string.stat_tiempo_medio) + " " + tiempoMedio + " " + getString(R.string.minutos));
        }
    }
}