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
    private Button btnGuardar;
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

        // 2. Datos del Intent
        idJuegoActual = getIntent().getIntExtra("ID_JUEGO", -1);
        String nombre = getIntent().getStringExtra("NOMBRE_JUEGO");
        String jugadores = getIntent().getStringExtra("JUGADORES_JUEGO");
        int duracion = getIntent().getIntExtra("DURACION_JUEGO", 0);
        int jugado = getIntent().getIntExtra("JUGADO_JUEGO", 0);
        esWishlist = getIntent().getBooleanExtra("ES_WISHLIST", false);

        // 3. Rellenar campos
        etNombre.setText(nombre);
        etJugadores.setText(jugadores);
        etDuracion.setText(String.valueOf(duracion));
        switchJugado.setChecked(jugado == 1);

        // Lógica visual inicial
        if (jugado == 1) layoutFecha.setVisibility(View.VISIBLE);

        switchJugado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutFecha.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // --- FORMATEO DE FECHA AUTOMÁTICO (dd/mm/aaaa) ---
        etFecha.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i <= 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    etFecha.setText(current);
                    etFecha.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
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
                Toast.makeText(this, "Juego eliminado de la colección", Toast.LENGTH_SHORT).show();
            } else {
                int nuevaPropiedad = esWishlist && loTengo ? 1 : (esWishlist ? 0 : 1);

                if (esWishlist && loTengo) {
                    Toast.makeText(this, "¡Añadido a la colección!", Toast.LENGTH_SHORT).show();
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