package com.example.proyectoindiv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class InicioActivity extends AppCompatActivity {

    // Necesitamos llamar a la base de datos desde el inicio
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        dbHelper = new DatabaseHelper(this);

        // --- INICIALIZAR SISTEMA DE NOTIFICACIONES AL ABRIR LA APP ---
        pedirPermisosNotificaciones();
        crearCanalNotificaciones();
        comprobarJuegosOlvidados();
        // -------------------------------------------------------------

        Button btnLudoteca = findViewById(R.id.btnIrLudoteca);
        Button btnWishlist = findViewById(R.id.btnIrWishlist);

        // Al pulsar "Mi Ludoteca"
        btnLudoteca.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            intent.putExtra("MODO_LISTA", 1); // 1 = Ludoteca
            startActivity(intent);
        });

        // Al pulsar "Wishlist"
        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            intent.putExtra("MODO_LISTA", 0); // 0 = Wishlist
            startActivity(intent);
        });

        android.widget.ImageView btnIdioma = findViewById(R.id.btnIdioma);
        btnIdioma.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(InicioActivity.this, btnIdioma);
            popup.getMenu().add(0, 1, 0, getString(R.string.idioma_es));
            popup.getMenu().add(0, 2, 0, getString(R.string.idioma_en));
            popup.getMenu().add(0, 3, 0, getString(R.string.idioma_eu));

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: cambiarIdioma("es"); break; // Español
                    case 2: cambiarIdioma("en"); break; // Inglés
                    case 3: cambiarIdioma("eu"); break; // Euskera
                }
                return true;
            });
            popup.show();
        });
        // --- GESTIÓN DE NOMBRE DE USUARIO (SharedPreferences) ---
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String nombreGuardado = prefs.getString("user_name", "");

        // Si es la primera vez (nombre vacío), lo pedimos
        if (nombreGuardado.isEmpty()) {
            pedirNombreUsuario(prefs);
        }
    }

    // --- MÉTODOS DE IDIOMA ---
    private void cambiarIdioma(String codigoIdioma) {
        String actual = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags();

        if (actual.equals(codigoIdioma) || (actual.isEmpty() && codigoIdioma.equals("es"))) {
            android.widget.Toast.makeText(this, getString(R.string.idioma_seleccionado), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.core.os.LocaleListCompat appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(codigoIdioma);
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale);
    }

    // --- MÉTODOS DE NOTIFICACIONES ---
    private void pedirPermisosNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void crearCanalNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Avisos Ludoteca";
            String description = "Canal para avisar de juegos no jugados";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel("CANAL_LUDOTECA", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void comprobarJuegosOlvidados() {
        java.util.List<JuegoMesa> misJuegos = dbHelper.obtenerJuegosFiltrados(1);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.util.Calendar hoy = java.util.Calendar.getInstance();

        for (JuegoMesa juego : misJuegos) {
            String fechaStr = juego.getFecha();

            if (fechaStr != null && !fechaStr.trim().isEmpty()) {
                try {
                    java.util.Date fechaJugado = sdf.parse(fechaStr);
                    java.util.Calendar limite = java.util.Calendar.getInstance();
                    limite.setTime(fechaJugado);
                    limite.add(java.util.Calendar.MONTH, 3); // +3 meses

                    if (hoy.after(limite)) {

                        // ---  ACCIÓN 1: BOTÓN "A JUGAR" ---
                        android.content.Intent intentJugar = new android.content.Intent(this, DetalleActivity.class);
                        // Pasamos los datos a la actividad mediante extras.
                        intentJugar.putExtra("ID_JUEGO", juego.getId());
                        intentJugar.putExtra("NOMBRE_JUEGO", juego.getNombre());
                        intentJugar.putExtra("JUGADORES_JUEGO", juego.getJugadores());
                        intentJugar.putExtra("DURACION_JUEGO", juego.getDuracion());
                        intentJugar.putExtra("JUGADO_JUEGO", juego.getJugado());
                        intentJugar.putExtra("ES_WISHLIST", false);
                        intentJugar.putExtra("FECHA_JUEGO", juego.getFecha());
                        intentJugar.putExtra("ID_NOTI_BORRAR", juego.getId());

                        android.app.PendingIntent pendingIntentJugar = android.app.PendingIntent.getActivity(
                                this,
                                juego.getId(), // Un ID único para que no se mezclen si hay varias notificaciones
                                intentJugar,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                        );

                        // ---  ACCIÓN 2: BOTÓN "DESCARTAR" ---
                        android.content.Intent intentDescartar = new android.content.Intent(this, CancelarNotificacionReceiver.class);
                        intentDescartar.putExtra("ID_NOTIFICACION", juego.getId());

                        android.app.PendingIntent pendingIntentDescartar = android.app.PendingIntent.getBroadcast(
                                this,
                                juego.getId() + 10000, // ID diferente para evitar conflictos
                                intentDescartar,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                        );

                        // --- CONSTRUIR NOTIFICACIÓN CON BOTONES ---
                        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, "CANAL_LUDOTECA")
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setContentTitle(getString(R.string.noti_titulo))
                                .setContentText(getString(R.string.noti_texto) + " " + juego.getNombre())
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true) // Borra la noti si tocas el texto normal
                                .addAction(android.R.drawable.ic_media_play, getString(R.string.btn_noti_jugar), pendingIntentJugar)
                                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.btn_noti_descartar), pendingIntentDescartar);

                        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(this);

                        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            notificationManager.notify(juego.getId(), builder.build());
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores de fecha
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                comprobarJuegosOlvidados(); // Salta al instante si acabas de darle a Permitir
            }
        }
    }
    // Pedir nombre de usuario la primera vez que entras
    private void pedirNombreUsuario(android.content.SharedPreferences prefs) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_nombre, null);
        android.widget.EditText etNombre = view.findViewById(R.id.etNuevoNombre);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .create();

        dialog.show();

        // Controlamos el clic para que no se cierre si está vacío
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                prefs.edit().putString("user_name", nombre).apply(); // Guardado en SharedPreferences
                dialog.dismiss();
            } else {
                etNombre.setError(getString(R.string.error_obligatorio));
            }
        });
    }
}