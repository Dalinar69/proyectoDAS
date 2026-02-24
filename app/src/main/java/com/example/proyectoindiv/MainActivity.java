package com.example.proyectoindiv;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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

    // Menu y fondo
    private DrawerLayout drawerLayout;
    private android.widget.RelativeLayout layoutPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enlazamos las variables con los IDs del layout XML
        recyclerViewJuegos = findViewById(R.id.recyclerViewJuegos);
        fabAnadirJuego = findViewById(R.id.fabAnadirJuego);

        // Enlazamos el fondo principal para poder cambiarlo
        layoutPrincipal = findViewById(R.id.layoutPrincipal);

        //Menu lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        // Añade el botón de la hamburguesa a la barra
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.abrir_menu, R.string.cerrar_menu);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Qué pasa al tocar una opción del menú
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_ludoteca) {
                cambiarModo(1);
            } else if (item.getItemId() == R.id.nav_wishlist) {
                cambiarModo(0);
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el menú al elegir
            return true;
        });

        // 2. Configuramos el layout del RecyclerView (lista vertical estándar)
        recyclerViewJuegos.setLayoutManager(new LinearLayoutManager(this));

        // 3. Inicializamos la base de datos y obtenemos la lista de juegos
        dbHelper = new DatabaseHelper(this);

        // Leemos qué botón pulsaste en la pantalla de inicio
        modoLista = getIntent().getIntExtra("MODO_LISTA", 1);
        cambiarModo(modoLista);

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

    private void cambiarModo(int nuevoModo) {
        modoLista = nuevoModo;
        boolean esWishlist = (modoLista == 0);

        if (modoLista == 1) {
            getSupportActionBar().setTitle("🎲 Mi Ludoteca");
            layoutPrincipal.setBackgroundResource(R.drawable.ludoteca);
        } else {
            getSupportActionBar().setTitle("🛒 Mi Wishlist");
            layoutPrincipal.setBackgroundResource(R.drawable.fondo_wishlist);
        }

        miLudoteca = dbHelper.obtenerJuegosFiltrados(modoLista);
        adaptadorJuegos = new JuegoAdapter(miLudoteca, esWishlist);
        recyclerViewJuegos.setAdapter(adaptadorJuegos);
    }

    private void recargarLista() {
        if (dbHelper != null && miLudoteca != null && adaptadorJuegos != null) {
            miLudoteca.clear();
            miLudoteca.addAll(dbHelper.obtenerJuegosFiltrados(modoLista));
            adaptadorJuegos.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarLista();
        }
    }
