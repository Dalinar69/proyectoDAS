package com.example.proyectoindiv;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class DetalleActivity extends AppCompatActivity {

    private EditText etNombre, etJugadores, etDuracion, etFecha;
    private LinearLayout layoutFecha;
    private Switch switchJugado;
    private Button btnGuardar, btnBuscarWeb;
    private DatabaseHelper dbHelper;
    private int idJuegoActual;
    private android.widget.ImageView ivDetalleCaratula;
    private boolean esWishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);


        // 1. Enlaces
        etNombre = findViewById(R.id.etDetalleNombre);
        etJugadores = findViewById(R.id.etDetalleJugadores);
        etDuracion = findViewById(R.id.etDetalleDuracion);
        switchJugado = findViewById(R.id.switchJugado);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        ivDetalleCaratula = findViewById(R.id.ivDetalleCaratula);
        layoutFecha = findViewById(R.id.layoutFecha);
        etFecha = findViewById(R.id.etDetalleFecha);
        dbHelper = new DatabaseHelper(this);
        btnBuscarWeb = findViewById(R.id.btnBuscarWeb);

        // 2.1 Intent implicito
        btnBuscarWeb.setOnClickListener(v -> {
            // Cogemos el nombre del juego que estás viendo
            String nombreBuscado = etNombre.getText().toString();

            // Creamos la URL de búsqueda en Google
            String url = "https://www.google.com/search?q=juego+de+mesa+" + nombreBuscado;

            android.content.Intent intentWeb = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intentWeb.setData(android.net.Uri.parse(url));
            startActivity(intentWeb);
        });

        // 2. Datos del Intent
        idJuegoActual = getIntent().getIntExtra("ID_JUEGO", -1);
        String nombre = getIntent().getStringExtra("NOMBRE_JUEGO");
        String jugadores = getIntent().getStringExtra("JUGADORES_JUEGO");
        int duracion = getIntent().getIntExtra("DURACION_JUEGO", 0);
        int jugado = getIntent().getIntExtra("JUGADO_JUEGO", 0);
        esWishlist = getIntent().getBooleanExtra("ES_WISHLIST", false);
        String fechaGuardada = getIntent().getStringExtra("FECHA_JUEGO");

        int idNotiBorrar = getIntent().getIntExtra("ID_NOTI_BORRAR", -1);
        if (idNotiBorrar != -1) {
            androidx.core.app.NotificationManagerCompat.from(this).cancel(idNotiBorrar);
        }

        // 3. Rellenar campos
        etNombre.setText(nombre);
        etJugadores.setText(jugadores);
        etDuracion.setText(String.valueOf(duracion));
        switchJugado.setChecked(jugado == 1);

        if (fechaGuardada != null && !fechaGuardada.isEmpty()) {
            etFecha.setText(fechaGuardada);
        }

        // Lógica visual inicial
        if (jugado == 1) layoutFecha.setVisibility(View.VISIBLE);

        switchJugado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutFecha.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // --- SELECCIÓN DE FECHA CON CALENDARIO NATIVO ---
        // Hacemos que no se pueda escribir a mano con el teclado
        etFecha.setFocusable(false);
        etFecha.setClickable(true);

        etFecha.setOnClickListener(v -> {
            // Cogemos la fecha actual para que el calendario se abra en el día de hoy
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Creamos el diálogo del calendario
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    DetalleActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Cuando el usuario elige fecha, le damos el formato dd/mm/aaaa y lo ponemos en el cajetín
                        String fechaFormateada = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                        etFecha.setText(fechaFormateada);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // --- IMAGEN  ---
        String nombreImagen = nombre.toLowerCase().replaceAll("[^a-z0-9]", "");
        int imageResId = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
        ivDetalleCaratula.setImageResource(imageResId != 0 ? imageResId : R.drawable.pordefecto);

        // 4. GUARDAR CON VALIDACIÓN Y MOVIMIENTO AUTOMÁTICO

        btnGuardar.setOnClickListener(v -> {
            boolean loTengo = switchJugado.isChecked();
            String fecha = etFecha.getText().toString();

            // Lógica de borrado o movimiento
            if (!esWishlist && !loTengo) {
                // Estás en Ludoteca y dices que ya NO lo tienes -> BORRAR
                dbHelper.eliminarJuego(idJuegoActual);
                Toast.makeText(this, getString(R.string.toast_eliminado), Toast.LENGTH_SHORT).show();
            } else {
                int nuevaPropiedad = esWishlist && loTengo ? 1 : (esWishlist ? 0 : 1);

                if (esWishlist && loTengo) {
                    Toast.makeText(this, getString(R.string.toast_anadido), Toast.LENGTH_SHORT).show();
                }

                dbHelper.actualizarJuegoCompleto(
                        idJuegoActual,
                        etNombre.getText().toString(),
                        etJugadores.getText().toString(),
                        Integer.parseInt(etDuracion.getText().toString()),
                        loTengo ? 1 : 0,
                        nuevaPropiedad,
                        fecha
                );
            }
            finish();
        });

    }
}