package com.example.proyectoindiv;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleActivity extends AppCompatActivity {

    private EditText etNombre, etJugadores, etDuracion;
    private Switch switchJugado;
    private Button btnGuardar;
    private DatabaseHelper dbHelper;
    private int idJuegoActual; // Para saber qué juego estamos editando
    private android.widget.ImageView ivDetalleCaratula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // 1. Enlazamos con el XML
        etNombre = findViewById(R.id.etDetalleNombre);
        etJugadores = findViewById(R.id.etDetalleJugadores);
        etDuracion = findViewById(R.id.etDetalleDuracion);
        switchJugado = findViewById(R.id.switchJugado);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        dbHelper = new DatabaseHelper(this);
        ivDetalleCaratula = findViewById(R.id.ivDetalleCaratula);

        // 2. Recogemos los datos que nos envía el Adapter en el Intent
        idJuegoActual = getIntent().getIntExtra("ID_JUEGO", -1);
        String nombre = getIntent().getStringExtra("NOMBRE_JUEGO");
        String jugadores = getIntent().getStringExtra("JUGADORES_JUEGO");
        int duracion = getIntent().getIntExtra("DURACION_JUEGO", 0);
        int jugado = getIntent().getIntExtra("JUGADO_JUEGO", 0);

        // 3. Rellenamos la pantalla con esos datos
        etNombre.setText(nombre);
        etJugadores.setText(jugadores);
        etDuracion.setText(String.valueOf(duracion));
        switchJugado.setChecked(jugado == 1); // Si es 1, el interruptor se enciende

        // --- IMAGEN EN GRANDE ---
        String nombreImagen = nombre.toLowerCase().replaceAll("[^a-z0-9]", "");
        int imageResId = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());

        if (imageResId != 0) {
            ivDetalleCaratula.setImageResource(imageResId);
        } else {
            ivDetalleCaratula.setImageResource(R.drawable.ic_launcher_background);
        }
        // --------------------------------------

        // 4. Qué pasa al pulsar "Guardar Cambios"
        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString();
            String nuevosJugadores = etJugadores.getText().toString();
            int nuevaDuracion = Integer.parseInt(etDuracion.getText().toString());
            int nuevoJugado = switchJugado.isChecked() ? 1 : 0;

            // Guardamos en la BBDD
            dbHelper.actualizarJuego(idJuegoActual, nuevoNombre, nuevosJugadores, nuevaDuracion, nuevoJugado);

            Toast.makeText(this, "Juego actualizado", Toast.LENGTH_SHORT).show();

            // Cerramos esta pantalla para volver a la principal (Control de pila de actividades)
            finish();
        });
    }
}