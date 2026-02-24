package com.example.proyectoindiv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adaptador para gestionar el listado de juegos de mesa en el RecyclerView.
 */
public class JuegoAdapter extends RecyclerView.Adapter<JuegoAdapter.JuegoViewHolder> {

    private List<JuegoMesa> listaJuegos;
    private boolean esWishlist;

    public JuegoAdapter(List<JuegoMesa> listaJuegos, boolean esWishlist) {

        this.listaJuegos = listaJuegos;
        this.esWishlist = esWishlist;
    }

    @NonNull
    @Override
    public JuegoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_juego, parent, false);
        return new JuegoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JuegoViewHolder holder, int position) {
        JuegoMesa juegoActual = listaJuegos.get(position);

        holder.tvNombreJuego.setText(juegoActual.getNombre());
        holder.tvJugadores.setText("👥 Jugadores: " + juegoActual.getJugadores());
        holder.tvDuracion.setText("⏱️ Duración: " + juegoActual.getDuracion() + " min");

        // --- IMAGEN ---
        // 1. Limpiamos el nombre: lo pasamos a minúsculas y quitamos espacios
        String nombreImagen = juegoActual.getNombre().toLowerCase().replaceAll("[^a-z0-9]", "");

        // 2. Buscamos si existe un archivo con ese nombre en la carpeta drawable
        int imageResId = holder.itemView.getContext().getResources().getIdentifier(
                nombreImagen, "drawable", holder.itemView.getContext().getPackageName());

        // 3. Si existe (imageResId no es 0), la ponemos. Si no, dejamos una por defecto
        if (imageResId != 0) {
            holder.ivCaratula.setImageResource(imageResId);
        } else {
            holder.ivCaratula.setImageResource(R.drawable.pordefecto);
            holder.ivCaratula.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER); // Para que no se corte el dibujoh
        }
        // -----------------------------

        // Hacemos que toda la tarjeta sea clicable
        holder.itemView.setOnClickListener(v -> {
            // Creamos un Intent para viajar a la DetalleActivity
            android.content.Intent intent = new android.content.Intent(v.getContext(), DetalleActivity.class);

            // Le pasamos los datos del juego en la "mochila" del Intent
            intent.putExtra("ID_JUEGO", juegoActual.getId());
            intent.putExtra("NOMBRE_JUEGO", juegoActual.getNombre());
            intent.putExtra("JUGADORES_JUEGO", juegoActual.getJugadores());
            intent.putExtra("DURACION_JUEGO", juegoActual.getDuracion());
            intent.putExtra("JUGADO_JUEGO", juegoActual.getJugado());
            intent.putExtra("ES_WISHLIST", this.esWishlist);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaJuegos.size();
    }

    public static class JuegoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreJuego;
        TextView tvJugadores;
        TextView tvDuracion;
        android.widget.ImageView ivCaratula; // Declaramos la imagen

        public JuegoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreJuego = itemView.findViewById(R.id.tvNombreJuego);
            tvJugadores = itemView.findViewById(R.id.tvJugadores);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            ivCaratula = itemView.findViewById(R.id.ivCaratula); // La enlazamos con el XML
        }
    }
}