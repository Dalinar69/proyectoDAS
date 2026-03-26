package com.example.proyectoindiv;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerfilActivity extends AppCompatActivity {

    private ImageView ivFotoPerfil;
    private TextView tvEmailPerfil, tvEliminarFoto;
    private EditText etNombrePerfil;
    private Button btnTomarFoto, btnGuardarPerfil;

    private String emailUsuario = "";
    private String fotoBase64 = ""; // Aquí guardaremos la foto en formato texto o "BORRAR"

    private final String URL_ACTUALIZAR = "http://34.175.86.107:81/actualizar_perfil.php";

    private final ActivityResultLauncher<Intent> launcherCamara = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivFotoPerfil.setImageBitmap(imageBitmap);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, getString(R.string.error_permiso_camara), Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_perfil);

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        tvEmailPerfil = findViewById(R.id.tvEmailPerfil);
        etNombrePerfil = findViewById(R.id.etNombrePerfil);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);
        tvEliminarFoto = findViewById(R.id.tvEliminarFoto);

        // Hacer que el texto parezca un enlace subrayado
        tvEliminarFoto.setPaintFlags(tvEliminarFoto.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String nombreGuardado = prefs.getString("user_name", "");
        emailUsuario = prefs.getString("user_email", "usuario@prueba.com");

        tvEmailPerfil.setText(emailUsuario);
        etNombrePerfil.setText(nombreGuardado);

        btnTomarFoto.setOnClickListener(v -> comprobarPermisosCamara());

        // Acción de ELIMINAR FOTO
        tvEliminarFoto.setOnClickListener(v -> {
            ivFotoPerfil.setImageResource(android.R.drawable.ic_menu_camera);
            fotoBase64 = "BORRAR"; // Le mandamos esta palabra clave al servidor
        });

        btnGuardarPerfil.setOnClickListener(v -> {
            String nuevoNombre = etNombrePerfil.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                etNombrePerfil.setError(getString(R.string.error_obligatorio));
                return;
            }
            guardarPerfilEnServidor(nuevoNombre, fotoBase64);
        });
    }

    private void comprobarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            launcherCamara.launch(takePictureIntent);
        }
    }

    private void guardarPerfilEnServidor(String nuevoNombre, String fotoCodificada) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String resultado = "";
            try {
                URL url = new URL(URL_ACTUALIZAR);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String parametros = "email=" + URLEncoder.encode(emailUsuario, "UTF-8") +
                        "&nombre=" + URLEncoder.encode(nuevoNombre, "UTF-8") +
                        "&foto=" + URLEncoder.encode(fotoCodificada, "UTF-8");

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
            handler.post(() -> procesarRespuesta(respuestaFinal, nuevoNombre));
        });
    }

    private void procesarRespuesta(String json, String nuevoNombre) {
        if (json.equals("error_conexion")) {
            Toast.makeText(this, getString(R.string.toast_error_servidor), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            String status = jsonObject.getString("status");

            if (status.equals("success")) {
                Toast.makeText(this, getString(R.string.toast_perfil_actualizado), Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                prefs.edit().putString("user_name", nuevoNombre).apply();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.toast_error_bd), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.toast_error_procesando), Toast.LENGTH_SHORT).show();
        }
    }
}