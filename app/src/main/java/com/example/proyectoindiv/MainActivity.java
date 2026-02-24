package com.example.proyectoindiv;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * Actividad principal que muestra el listado de juegos de mesa de la ludoteca.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewJuegos;
    private JuegoAdapter adaptadorJuegos;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAnadirJuego;
    private List<JuegoMesa> miLudoteca;
    private int modoLista = 1; // Por defecto Ludoteca

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enlazamos las variables con los IDs del layout XML
        recyclerViewJuegos = findViewById(R.id.recyclerViewJuegos);
        fabAnadirJuego = findViewById(R.id.fabAnadirJuego);

        // Enlazamos el fondo principal para poder cambiarlo
        android.widget.RelativeLayout layoutPrincipal = findViewById(R.id.layoutPrincipal);

        // 2. Configuramos el layout del RecyclerView (lista vertical estándar)
        recyclerViewJuegos.setLayoutManager(new LinearLayoutManager(this));

        // 3. Inicializamos la base de datos y obtenemos la lista de juegos
        dbHelper = new DatabaseHelper(this);

        // Leemos qué botón pulsaste en la pantalla de inicio
        modoLista = getIntent().getIntExtra("MODO_LISTA", 1);

        // Creamos la variable booleana para pasársela al Adapter luego
        boolean esWishlist = (modoLista == 0);

        // Arreglo rápido para que no quede soso arriba y CAMBIO DE FONDO
        if (modoLista == 1) {
            setTitle("🎲 Mi Ludoteca");
            layoutPrincipal.setBackgroundResource(R.drawable.ludoteca); // Fondo Ludoteca
        } else {
            setTitle("🛒 Mi Wishlist");
            layoutPrincipal.setBackgroundResource(R.drawable.fondo_wishlist); // Fondo Wishlist
        }

        // Le pedimos a la BBDD solo los juegos de esa lista
        miLudoteca = dbHelper.obtenerJuegosFiltrados(modoLista);

        // 4. Configuramos el adaptador pasándole también si es wishlist o no (esWishlist)
        adaptadorJuegos = new JuegoAdapter(miLudoteca, esWishlist);
        recyclerViewJuegos.setAdapter(adaptadorJuegos);

        // Configuración del botón flotante para añadir juegos mediante un Diálogo
        fabAnadirJuego.setOnClickListener(v -> {
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_anadir_juego, null);
            android.widget.EditText etNombre = dialogView.findViewById(R.id.etNombreJuego);
            android.widget.EditText etJugadores = dialogView.findViewById(R.id.etJugadores);
            android.widget.EditText etDuracion = dialogView.findViewById(R.id.etDuracion);

            // Creamos el diálogo pero NO lo mostramos todavía con .show()
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .setTitle("Añadir nuevo juego")
                    .setPositiveButton("Guardar", null) // Ponemos null para controlar el click después
                    .setNegativeButton("Cancelar", null)
                    .create();

            dialog.show();

            // Ahora capturamos el botón para que no se cierre si hay errores
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String nombre = etNombre.getText().toString().trim();
                String jugadores = etJugadores.getText().toString().trim();
                String duracionStr = etDuracion.getText().toString().trim();

                // VALIDACIÓN: Si está vacío, marcamos el error en rojo en el propio campo
                if (nombre.isEmpty()) {
                    etNombre.setError("El nombre es obligatorio");
                    return;
                }
                if (jugadores.isEmpty()){
                    etJugadores.setError("Pon un numero de jugadores valido");
                    return;
                }
                if (duracionStr.isEmpty()) {
                    etDuracion.setError("Pon una duración");
                    return;
                }

                // Si llega aquí, es que todo está OK
                int duracion = Integer.parseInt(duracionStr);
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(DatabaseHelper.COLUMN_NOMBRE, nombre);
                values.put(DatabaseHelper.COLUMN_JUGADORES, jugadores);
                values.put(DatabaseHelper.COLUMN_DURACION, duracion);

                // Lo que hablamos: Si es Ludoteca (1), nace con el switch encendido (1)
                values.put(DatabaseHelper.COLUMN_JUGADO, modoLista == 1 ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_PROPIEDAD, modoLista);
                values.put(DatabaseHelper.COLUMN_FECHA, "");

                android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.insert(DatabaseHelper.TABLE_JUEGOS, null, values);
                db.close();

                // Actualizamos la lista y cerramos el diálogo manualmente
                miLudoteca.clear();
                miLudoteca.addAll(dbHelper.obtenerJuegosFiltrados(modoLista));
                adaptadorJuegos.notifyDataSetChanged();
                dialog.dismiss();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && miLudoteca != null && adaptadorJuegos != null) {
            miLudoteca.clear();
            miLudoteca.addAll(dbHelper.obtenerJuegosFiltrados(modoLista));
            adaptadorJuegos.notifyDataSetChanged();
        }
    }
}