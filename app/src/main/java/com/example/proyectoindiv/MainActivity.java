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

        // Configura el icono del menú lateral (Navigation Drawer) en la Toolbar.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.abrir_menu, R.string.cerrar_menu);
        toggle.getDrawerArrowDrawable().setColor(android.graphics.Color.WHITE);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gestión de eventos de navegación del menú lateral
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_ludoteca) {
                cambiarModo(1);
            } else if (item.getItemId() == R.id.nav_wishlist) {
                cambiarModo(0);
            } else if (item.getItemId() == R.id.nav_exportar) {
                exportarColeccionTXT();
            } else if (item.getItemId() == R.id.nav_es) {
                cambiarIdioma("es");
            } else if (item.getItemId() == R.id.nav_en) {
                cambiarIdioma("en");
            } else if (item.getItemId() == R.id.nav_eu) {
                cambiarIdioma("eu");
            } else if (item.getItemId() == R.id.nav_acerca_de) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.acerca_de_titulo))
                        .setMessage(getString(R.string.acerca_de_mensaje))
                        .setPositiveButton(getString(R.string.btn_cerrar), null)
                        .show();
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Cierra el menú al elegir
            return true;
        });

        // 2. Configuramos el layout del RecyclerView (lista vertical estándar)
        recyclerViewJuegos.setLayoutManager(new LinearLayoutManager(this));

        // 3. Inicializamos la base de datos y obtenemos la lista de juegos
        dbHelper = new DatabaseHelper(this);

        // Recuperación del modo de vista desde el Intent
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
                    .setTitle(getString(R.string.dialog_add_title)) // Título traducido
                    .setPositiveButton(getString(R.string.btn_guardar), null) // Botón traducido
                    .setNegativeButton(getString(R.string.btn_cancelar), null) // Botón traducido
                    .create();


            dialog.show();

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String nombre = etNombre.getText().toString().trim();
                String jugadores = etJugadores.getText().toString().trim();
                String duracionStr = etDuracion.getText().toString().trim();

                // Errores traducidos
                if (nombre.isEmpty()) {
                    etNombre.setError(getString(R.string.error_obligatorio));
                    return;
                }
                if (jugadores.isEmpty()){
                    etJugadores.setError(getString(R.string.error_obligatorio));
                    return;
                }
                if (duracionStr.isEmpty()) {
                    etDuracion.setError(getString(R.string.error_obligatorio));
                    return;
                }
                boolean existe = false;
                for (JuegoMesa j : dbHelper.obtenerTodosLosJuegos()) {
                    // equalsIgnoreCase ignora si lo pones en mayúsculas o minúsculas
                    if (j.getNombre().equalsIgnoreCase(nombre)) {
                        existe = true;
                        break; // Si ya hemos encontrado uno, dejamos de buscar
                    }
                }

                if (existe) {
                    etNombre.setError(getString(R.string.error_duplicado));
                    return; // Cortamos aquí para que no se guarde
                }
                // Inserción del nuevo registro si supera las validaciones
                // Inserción del nuevo registro si supera las validaciones
                int duracion = Integer.parseInt(duracionStr);
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(DatabaseHelper.COLUMN_NOMBRE, nombre);
                values.put(DatabaseHelper.COLUMN_JUGADORES, jugadores);
                values.put(DatabaseHelper.COLUMN_DURACION, duracion);

                // Asignar estado de 'jugado' por defecto según el modo de lista.
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


        // ---  MOSTRAR NOMBRE EN EL MENÚ LATERAL ---
        android.view.View header = navView.getHeaderView(0);
        android.widget.TextView tvNombreNav = header.findViewById(R.id.tvNombreUsuarioNav);

        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String nombre = prefs.getString("user_name", "Jugador");
        // Miramos qué idioma está puesto ahora mismo
        String lang = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags();

        // Aplicamos la lógica del orden según el idioma
        if (lang.contains("eu")) {
            // En Euskera: Nombre + ren + Ludoteka (ej: Ikerren Ludoteka)
            tvNombreNav.setText(nombre + "ren " + getString(R.string.menu_ludoteca2));
        } else if (lang.contains("es") || lang.isEmpty()) {
            // Español o por defecto (ej: Ludoteca de Iker)
            tvNombreNav.setText(getString(R.string.menu_ludoteca2) + " " + nombre);
        } else {
            // Ingles (ej: Iker's boardgames)
            tvNombreNav.setText(nombre + "'s " + getString(R.string.menu_ludoteca2));
        }
        // Si clica en el nombre, llamamos al método para modificarlo
        tvNombreNav.setOnClickListener(v -> mostrarDialogoNombre(tvNombreNav, prefs));
    }
    private void mostrarDialogoNombre(android.widget.TextView tvNombreNav, android.content.SharedPreferences prefs) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_nombre, null);
        android.widget.EditText etNombre = view.findViewById(R.id.etNuevoNombre);
        etNombre.setText(prefs.getString("user_name", ""));

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("OK", null)
                .setNegativeButton(getString(R.string.btn_cancelar), null)
                .create();

        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevo = etNombre.getText().toString().trim();
            if (!nuevo.isEmpty()) {
                prefs.edit().putString("user_name", nuevo).apply();
                String lang = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags();
                if (lang.contains("eu")) {
                    tvNombreNav.setText(nuevo + "ren " + getString(R.string.menu_ludoteca2));
                } else if (lang.contains("es") || lang.isEmpty()) {
                    tvNombreNav.setText(getString(R.string.menu_ludoteca2) + " " + nuevo);
                } else {
                    tvNombreNav.setText(nuevo + "'s " + getString(R.string.menu_ludoteca2));
                }
                dialog.dismiss();
            } else {
                etNombre.setError(getString(R.string.error_obligatorio));
            }
        });
    }

    private void cambiarModo(int nuevoModo) {
        modoLista = nuevoModo;
        boolean esWishlist = (modoLista == 0);

        if (modoLista == 1) {
            getSupportActionBar().setTitle(getString(R.string.menu_ludoteca));
            layoutPrincipal.setBackgroundResource(R.drawable.ludoteca);
        } else {
            getSupportActionBar().setTitle(getString(R.string.menu_wishlist));
            layoutPrincipal.setBackgroundResource(R.drawable.fondo_wishlist);
        }

        miLudoteca = dbHelper.obtenerJuegosFiltrados(modoLista);
        adaptadorJuegos = new JuegoAdapter(miLudoteca, esWishlist);
        recyclerViewJuegos.setAdapter(adaptadorJuegos);

        // Comprobamos si el hueco del fragment existe (es decir, si estamos en horizontal)
        android.view.View fragmentContainer = findViewById(R.id.fragmentContainer);
        if (fragmentContainer != null) {
            EstadisticasFragment fragment = new EstadisticasFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();

            // Usamos un pequeño retraso para asegurar que el fragment se ha dibujado antes de pasarle el dato
            fragmentContainer.post(() -> fragment.actualizarDatos(miLudoteca));
        }
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

    // Método para cambiar el idioma de la aplicación en tiempo de ejecución.
    private void cambiarIdioma(String codigoIdioma) {
        // Miramos qué idioma está puesto ahora mismo
        String actual = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags();

        // Si ya estamos en ese idioma (o si está vacío y elegimos el español, que es la base), no hacemos nada
        if (actual.equals(codigoIdioma) || (actual.isEmpty() && codigoIdioma.equals("es"))) {
            android.widget.Toast.makeText(this, getString(R.string.idioma_seleccionado), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es distinto, le decimos a Android que recargue la app en el nuevo idioma
        androidx.core.os.LocaleListCompat appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(codigoIdioma);
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale);
    }

    // Método para exportar la base de datos a un fichero de texto plano
    private void exportarColeccionTXT() {
        try {
            // Abrimos/creamos un archivo llamado "mi_coleccion.txt" en la memoria interna de la app
            java.io.OutputStreamWriter fout = new java.io.OutputStreamWriter(
                    openFileOutput("mi_coleccion.txt", MODE_PRIVATE));

            fout.write("--- MI COLECCIÓN TABLER ---\n\n");

            // Traemos TODOS los juegos (Ludoteca y Wishlist)
            List<JuegoMesa> todosLosJuegos = dbHelper.obtenerTodosLosJuegos();

            for (JuegoMesa juego : todosLosJuegos) {
                String ubicacion = juego.getPropiedad() == 1 ? "LUDOTECA" : "WISHLIST";
                String linea = "- " + juego.getNombre() + " | Jugadores: " + juego.getJugadores() +
                        " | " + juego.getDuracion() + " min. [" + ubicacion + "]\n";
                fout.write(linea); // Escribimos línea a línea en el archivo
            }

            fout.close(); // Cerramos el archivo para que se guarde bien

            // Avisamos al usuario de que ha ido perfecto
            android.widget.Toast.makeText(this, getString(R.string.toast_exportado), android.widget.Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            // Si algo falla (falta de memoria, etc), avisamos del error
            android.widget.Toast.makeText(this, getString(R.string.toast_error_exportar), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

}
