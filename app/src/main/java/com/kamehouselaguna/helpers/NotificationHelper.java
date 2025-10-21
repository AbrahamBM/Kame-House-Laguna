package com.kamehouselaguna.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.kamehouselaguna.MainActivity;
import com.kamehouselaguna.R;

/**
 * Clase helper para manejo de notificaciones push
 * Maneja la suscripción a tópicos y el envío de notificaciones
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "figuras_channel";
    private static final String CHANNEL_NAME = "Notificaciones de Figuras";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones sobre nuevas figuras y actualizaciones";
    private static final String TOPIC_ALL_USERS = "allUsers";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Suscribe al usuario al tópico de todos los usuarios
     */
    public void subscribeToAllUsersTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ALL_USERS)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Suscrito exitosamente al tópico: " + TOPIC_ALL_USERS);
                        } else {
                            Log.e(TAG, "Error al suscribirse al tópico", task.getException());
                        }
                    }
                });
    }

    /**
     * Desuscribe al usuario del tópico de todos los usuarios
     */
    public void unsubscribeFromAllUsersTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_ALL_USERS)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Desuscrito exitosamente del tópico: " + TOPIC_ALL_USERS);
                        } else {
                            Log.e(TAG, "Error al desuscribirse del tópico", task.getException());
                        }
                    }
                });
    }

    /**
     * Muestra una notificación local
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param notificationId ID único para la notificación
     */
    public void showLocalNotification(String title, String message, int notificationId) {
        // Crear intent para abrir la app cuando se toque la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar la notificación
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notificación local mostrada: " + title);
    }

    /**
     * Procesa un mensaje remoto de Firebase
     * @param remoteMessage Mensaje recibido
     */
    public void handleRemoteMessage(RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Verificar si el mensaje tiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Datos del mensaje: " + remoteMessage.getData());
        }

        // Verificar si el mensaje tiene notificación
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.d(TAG, "Título: " + notification.getTitle());
            Log.d(TAG, "Cuerpo: " + notification.getBody());

            // Mostrar notificación local si la app está en primer plano
            showLocalNotification(
                    notification.getTitle(),
                    notification.getBody(),
                    (int) System.currentTimeMillis()
            );
        }
    }

    /**
     * Obtiene el token de FCM del dispositivo
     * @param listener Callback para manejar el resultado
     */
    public void getFCMToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error al obtener token FCM", task.getException());
                            listener.onError("Error al obtener token FCM");
                            return;
                        }

                        // Obtener el token
                        String token = task.getResult();
                        Log.d(TAG, "Token FCM: " + token);
                        listener.onTokenReceived(token);
                    }
                });
    }

    // ========== INTERFACES DE CALLBACK ==========

    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
        void onError(String error);
    }
}
