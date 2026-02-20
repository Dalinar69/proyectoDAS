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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enlazamos las variables con los IDs del layout XML
        recyclerViewJuegos = findViewById(R.id.recyclerViewJuegos);
        fabAnadirJuego = findViewById(R.id.fabAnadirJuego);

        // 2. Configuramos el layout del RecyclerView (lista vertical estándar)
        recyclerViewJuegos.setLayoutManager(new LinearLayoutManager(this));

        // 3. Inicializamos la base de datos y obtenemos la lista de juegos
        dbHelper = new DatabaseHelper(this);
        miLudoteca = dbHelper.obtenerTodosLosJuegos();

        // 4. Configuramos el adaptador con la lista y se lo asignamos al RecyclerView
        adaptadorJuegos = new JuegoAdapter(miLudoteca);
        recyclerViewJuegos.setAdapter(adaptadorJuegos);

        // Configuración del botón flotante para añadir juegos mediante un Diálogo
        fabAnadirJuego.setOnClickListener(v -> {
            // 1. Inflamos (cargamos) el diseño visual del diálogo que creamos en XML
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_anadir_juego, null);

            // 2. Enlazamos los campos de texto del diálogo
            android.widget.EditText etNombre = dialogView.findViewById(R.id.etNombreJuego);
            android.widget.EditText etJugadores = dialogView.findViewById(R.id.etJugadores);
            android.widget.EditText etDuracion = dialogView.findViewById(R.id.etDuracion);

            // 3. Construimos la ventana emergente
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        // Obtenemos el texto que ha escrito el usuario
                        String nombre = etNombre.getText().toString();
                        String jugadores = etJugadores.getText().toString();
                        String duracionStr = etDuracion.getText().toString();

                        // Verificamos que no haya dejado campos vacíos básicos
                        if (!nombre.isEmpty() && !duracionStr.isEmpty()) {
                            int duracion = Integer.parseInt(duracionStr);

                            // Guardamos en SQLite (por defecto lo ponemos como 'no jugado' y 'en propiedad')
                            android.content.ContentValues values = new android.content.ContentValues();
                            values.put(DatabaseHelper.COLUMN_NOMBRE, nombre);
                            values.put(DatabaseHelper.COLUMN_JUGADORES, jugadores);
                            values.put(DatabaseHelper.COLUMN_DURACION, duracion);
                            values.put(DatabaseHelper.COLUMN_JUGADO, 0);
                            values.put(DatabaseHelper.COLUMN_PROPIEDAD, 1);

                            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.insert(DatabaseHelper.TABLE_JUEGOS, null, values);
                            db.close();

                            // Recargamos la lista para que aparezca el nuevo juego al instante
                            miLudoteca.clear();
                            miLudoteca.addAll(dbHelper.obtenerTodosLosJuegos());
                            adaptadorJuegos.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Si la base de datos y la lista ya están listas...
        if (dbHelper != null && miLudoteca != null && adaptadorJuegos != null) {
            // 1. Vaciamos la lista antigua
            miLudoteca.clear();
            // 2. Volvemos a pedirle todos los datos frescos a SQLite
            miLudoteca.addAll(dbHelper.obtenerTodosLosJuegos());
            // 3. Le chivamos al adaptador que repinte la pantalla
            adaptadorJuegos.notifyDataSetChanged();
        }
    }
}