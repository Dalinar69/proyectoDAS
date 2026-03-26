package com.example.proyectoindiv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InicioActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // --- VARIABLES PARA LOGIN/REGISTRO ---
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegistro;

    // IP Estática de Google Cloud configurada
    private final String URL_LOGIN = "http://34.175.86.107:81/login.php";
    private final String URL_REGISTRO = "http://34.175.86.107:81/registro.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_inicio);

        dbHelper = new DatabaseHelper(this);

        // --- INICIALIZAR SISTEMA DE NOTIFICACIONES ---
        pedirPermisosNotificaciones();
        crearCanalNotificaciones();
        comprobarJuegosOlvidados();
        // ---------------------------------------------

        // --- GESTIÓN DE IDIOMA ---
        android.widget.ImageView btnIdioma = findViewById(R.id.btnIdioma);
        if (btnIdioma != null) {
            btnIdioma.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(InicioActivity.this, btnIdioma);
                popup.getMenu().add(0, 1, 0, getString(R.string.idioma_es));
                popup.getMenu().add(0, 2, 0, getString(R.string.idioma_en));
                popup.getMenu().add(0, 3, 0, getString(R.string.idioma_eu));

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 1: cambiarIdioma("es"); break;
                        case 2: cambiarIdioma("en"); break;
                        case 3: cambiarIdioma("eu"); break;
                    }
                    return true;
                });
                popup.show();
            });
        }

        // --- CÓDIGO DE LOGIN Y REGISTRO ---
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegistro = findViewById(R.id.btn_registro);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!email.isEmpty() && !password.isEmpty()) {
                    hacerPeticionRed(URL_LOGIN, email, password, false);
                } else {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnRegistro != null) {
            btnRegistro.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!email.isEmpty() && !password.isEmpty()) {
                    hacerPeticionRed(URL_REGISTRO, email, password, true);
                } else {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // --- MÉTODOS DE CONEXIÓN A BD REMOTA (Temas 10 y 14) ---
    private void hacerPeticionRed(String urlDestino, String email, String password, boolean esRegistro) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String resultado = "";
            try {
                URL url = new URL(urlDestino);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String parametros = "email=" + email + "&password=" + password;
                if (esRegistro) {
                    parametros += "&nombre=" + email.split("@")[0]; // Nombre por defecto
                }

                OutputStream os = conn.getOutputStream();
                os.write(parametros.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    resultado = response.toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
                resultado = "error_conexion";
            }

            final String respuestaFinal = resultado;
            handler.post(() -> {
                procesarRespuestaServidor(respuestaFinal, esRegistro);
            });
        });
    }

    private void procesarRespuestaServidor(String json, boolean esRegistro) {
        if (json.equals("error_conexion")) {
            Toast.makeText(this, "Error conectando al servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.getString("status");
            String message = jsonObject.getString("message");

            if (status.equals("success")) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (!esRegistro) {
                    // Login correcto: Guardar datos y pasar a MainActivity
                    android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                    prefs.edit().putString("user_name", jsonObject.optString("nombre", "Jugador")).apply();

                    prefs.edit().putString("user_email", etEmail.getText().toString().trim()).apply();
                    // Aquí pasamos por defecto a la Ludoteca al hacer login
                    Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                    intent.putExtra("MODO_LISTA", 1);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error procesando datos", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MÉTODOS DE IDIOMA ---
    private void cambiarIdioma(String codigoIdioma) {
        String actual = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (actual.equals(codigoIdioma) || (actual.isEmpty() && codigoIdioma.equals("es"))) {
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
                    limite.add(java.util.Calendar.MONTH, 3);

                    if (hoy.after(limite)) {
                        android.content.Intent intentJugar = new android.content.Intent(this, DetalleActivity.class);
                        intentJugar.putExtra("ID_JUEGO", juego.getId());
                        intentJugar.putExtra("NOMBRE_JUEGO", juego.getNombre());
                        intentJugar.putExtra("JUGADORES_JUEGO", juego.getJugadores());
                        intentJugar.putExtra("DURACION_JUEGO", juego.getDuracion());
                        intentJugar.putExtra("JUGADO_JUEGO", juego.getJugado());
                        intentJugar.putExtra("ES_WISHLIST", false);
                        intentJugar.putExtra("FECHA_JUEGO", juego.getFecha());

                        android.app.PendingIntent pendingIntentJugar = android.app.PendingIntent.getActivity(
                                this, juego.getId(), intentJugar,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                        );

                        android.content.Intent intentDescartar = new android.content.Intent(this, CancelarNotificacionReceiver.class);
                        intentDescartar.putExtra("ID_NOTIFICACION", juego.getId());

                        android.app.PendingIntent pendingIntentDescartar = android.app.PendingIntent.getBroadcast(
                                this, juego.getId() + 10000, intentDescartar,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                        );

                        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, "CANAL_LUDOTECA")
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setContentTitle(getString(R.string.noti_titulo))
                                .setContentText(getString(R.string.noti_texto) + " " + juego.getNombre())
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .addAction(android.R.drawable.ic_media_play, getString(R.string.btn_noti_jugar), pendingIntentJugar)
                                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.btn_noti_descartar), pendingIntentDescartar);

                        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(this);
                        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            notificationManager.notify(juego.getId(), builder.build());
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                comprobarJuegosOlvidados();
            }
        }
    }
}