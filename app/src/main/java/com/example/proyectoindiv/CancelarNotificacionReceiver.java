package com.example.proyectoindiv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

public class CancelarNotificacionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Pillamos el ID de la notificación que queremos borrar
        int notificationId = intent.getIntExtra("ID_NOTIFICACION", -1);

        if (notificationId != -1) {
            // Le decimos al sistema que la borre silenciosamente
            NotificationManagerCompat.from(context).cancel(notificationId);
        }
    }
}