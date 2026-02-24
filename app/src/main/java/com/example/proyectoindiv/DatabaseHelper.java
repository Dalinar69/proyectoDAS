package com.example.proyectoindiv;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 1. Definimos el nombre y la versión de la base de datos
    private static final String DATABASE_NAME = "LudotecaDB";
    private static final int DATABASE_VERSION = 3;

    // 2. Definimos el nombre de la tabla y sus columnas
    public static final String TABLE_JUEGOS = "juegos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_JUGADORES = "jugadores"; // Ej: "2-4"
    public static final String COLUMN_DURACION = "duracion"; // En minutos
    public static final String COLUMN_JUGADO = "jugado"; // 0 = No, 1 = Sí
    public static final String COLUMN_PROPIEDAD = "propiedad"; // 0 = Wishlist, 1 = Colección
    public static final String COLUMN_FECHA = "fecha";

    // 3. Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 4. onCreate se ejecuta la PRIMERA vez que se abre la app
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creamos la tabla con la nueva columna
        String createTable = "CREATE TABLE " + TABLE_JUEGOS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOMBRE + " TEXT, " +
                COLUMN_JUGADORES + " TEXT, " +
                COLUMN_DURACION + " INTEGER, " +
                COLUMN_JUGADO + " INTEGER, " +
                COLUMN_PROPIEDAD + " INTEGER," +
                COLUMN_FECHA + " TEXT)";

        db.execSQL(createTable);

        // PRECARGAMOS ALGUNOS JUEGOS
        // JUEGOS INSERTADOS EN LA LUDOTECA
        insertarJuegoInicial(db, "Catan", "3-4", 90, 1, 1, "24/02/2026");
        insertarJuegoInicial(db, "Pandemic", "2-4", 60, 1, 1, "03/12/2025");
        insertarJuegoInicial(db, "Seven Wonders Duel", "2", 30, 1, 1, "04/08/2024");
        insertarJuegoInicial(db, "Carcassonne", "2-4", 45, 1, 1, "16/09/2025");
        insertarJuegoInicial(db,"Dune Imperium", "2-4", 90, 1, 1,"24/02/2025");
        insertarJuegoInicial(db, "Skull King", "2-4", 60, 1, 1, "12/07/2024");
        insertarJuegoInicial(db, "Splendor", "2-4", 75, 1, 1, "24/02/2025");
        insertarJuegoInicial(db, "Jaipur", "2", 30, 1, 1, "04/08/2024");
        insertarJuegoInicial(db, "Exploding Kittens", "2-4", 60, 1, 1, "16/09/2025");
        insertarJuegoInicial(db, "Monopoly", "2-4", 45, 1, 1, "24/02/2025");
        insertarJuegoInicial(db, "Ticket to Ride", "2-4", 60, 1, 1, "12/07/2024");
        insertarJuegoInicial(db, "Scout", "2-4", 75, 1, 1, "24/02/2025");
        insertarJuegoInicial(db, "The Crew", "2-4", 20, 1, 1, "04/08/2024");
        insertarJuegoInicial(db, "Risk", "2-4", 45, 1, 1, "27/01/2026");
        //JUEGOS INSERTADOS EN LA WISHLIST
        insertarJuegoInicial(db, "Gloomhaven", "1-4", 120, 0, 0, "");
        insertarJuegoInicial(db, "Arnak", "1-4", 60, 0, 0, "");
        insertarJuegoInicial(db, "Wingspan", "2-4", 60, 0, 0, "");
        insertarJuegoInicial(db, "Living Forest", "2-4", 90, 0, 0, "");
        insertarJuegoInicial(db, "Seven Wonders", "2-4", 90, 0, 0, "");
        insertarJuegoInicial(db, "Brass Birmingham", "2-4", 60, 0, 0, "");
        insertarJuegoInicial(db, "Terraforming Mars", "2-4", 120, 0, 0, "");
        insertarJuegoInicial(db, "Dead Cells", "2-4", 60, 0, 0, "");
        insertarJuegoInicial(db, "Ark Nova", "2-4", 130, 0, 0, "");
    }

    public void eliminarJuego(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JUEGOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JUEGOS);
        onCreate(db);
    }

    // Método auxiliar actualizado
    private void insertarJuegoInicial(SQLiteDatabase db, String nombre, String jugadores, int duracion, int jugado, int propiedad, String fecha) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_JUGADORES, jugadores);
        values.put(COLUMN_DURACION, duracion);
        values.put(COLUMN_JUGADO, jugado);
        values.put(COLUMN_PROPIEDAD, propiedad);
        values.put(COLUMN_FECHA, fecha);
        db.insert(TABLE_JUEGOS, null, values);
    }

    /**
     * Recupera todos los juegos almacenados en la base de datos local.
     * @return Una lista de objetos JuegoMesa.
     */
    public java.util.List<JuegoMesa> obtenerTodosLosJuegos() {
        java.util.List<JuegoMesa> listaJuegos = new java.util.ArrayList<>();
        // Abrimos la base de datos en modo lectura
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();

        // Ejecutamos la consulta SQL para obtener todo el contenido de la tabla
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_JUEGOS, null);

        // Recorremos los resultados y vamos creando los objetos JuegoMesa
        if (cursor.moveToFirst()) {
            do {
                JuegoMesa juego = new JuegoMesa(
                        cursor.getInt(0), // id
                        cursor.getString(1), // nombre
                        cursor.getString(2), // jugadores
                        cursor.getInt(3), // duracion
                        cursor.getInt(4), // jugado
                        cursor.getInt(5), // propiedad
                        cursor.getString(6) //fecha
                );
                listaJuegos.add(juego);
            } while (cursor.moveToNext());
        }

        // Cerramos el cursor y la base de datos para liberar memoria
        cursor.close();
        db.close();

        return listaJuegos;
    }

    // MÉTODO PARA FILTRAR
    public List<JuegoMesa> obtenerJuegosFiltrados(int propiedad) {
        List<JuegoMesa> lista = new java.util.ArrayList<>();
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();

        // Solo traemos los juegos que coincidan con la propiedad (0 o 1)
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_JUEGOS + " WHERE " + COLUMN_PROPIEDAD + " = " + propiedad, null);

        if (cursor.moveToFirst()) {
            do {
                JuegoMesa juego = new JuegoMesa(
                        cursor.getInt(0), // ID
                        cursor.getString(1), // Nombre
                        cursor.getString(2), // Jugadores
                        cursor.getInt(3), // Duración
                        cursor.getInt(4), // Jugado
                        cursor.getInt(5),  // PROPIEDAD
                        cursor.getString(6) //fecha
                );
                // Si tu JuegoMesa tiene más campos en el constructor, ponlos aquí igual que en obtenerTodosLosJuegos()
                lista.add(juego);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    /**
     * Actualiza los datos de un juego existente en la base de datos.
     */
    public void actualizarJuegoCompleto(int id, String nombre, String jugadores, int duracion, int jugado, int propiedad, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_JUGADORES, jugadores);
        values.put(COLUMN_DURACION, duracion);
        values.put(COLUMN_JUGADO, jugado);
        values.put(COLUMN_PROPIEDAD, propiedad);
        values.put(COLUMN_FECHA, fecha);

        db.update(TABLE_JUEGOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}