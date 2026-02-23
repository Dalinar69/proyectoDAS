package com.example.proyectoindiv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

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
    }
}