# 🔔 Configuración de Firebase Cloud Messaging

Esta guía te explica paso a paso cómo configurar Firebase Cloud Messaging (FCM) para el proyecto KameHouse Laguna.

## 📋 Requisitos Previos

- Proyecto Firebase creado
- Firebase Authentication configurado
- App Android registrada en Firebase
- Dispositivo Android físico o emulador

## 🚀 Paso 1: Habilitar Cloud Messaging

### 1.1 Acceder a Cloud Messaging
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `kamehouse-laguna`
3. En el menú lateral, haz clic en **"Cloud Messaging"**

### 1.2 Verificar Configuración
1. La página de Cloud Messaging se abrirá automáticamente
2. No requiere configuración adicional para el funcionamiento básico
3. Verifica que aparezca "Cloud Messaging está habilitado"

## 📱 Paso 2: Configurar en la App

### 2.1 Verificar Dependencias
Asegúrate de que `app/build.gradle.kts` incluya:
```kotlin
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // ... otras dependencias
}
```

### 2.2 Verificar AndroidManifest.xml
Asegúrate de que incluya:
```xml
<!-- Permisos necesarios -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Servicio de FCM -->
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Receptor de notificaciones -->
<receiver
    android:name="com.google.firebase.iid.FirebaseInstanceIdReceiver"
    android:exported="true"
    android:permission="com.google.android.c2dm.permission.SEND">
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
    </intent-filter>
</receiver>

<!-- Meta-data para notificaciones -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_launcher_foreground" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@android:color/holo_blue_bright" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="figuras_channel" />
```

## 🔧 Paso 3: Implementar Servicio de Mensajería

### 3.1 Crear Canal de Notificaciones
En tu `MyFirebaseMessagingService.java`:
```java
private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
```

### 3.2 Manejar Mensajes Recibidos
```java
@Override
public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    
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
        
        // Mostrar notificación
        showNotification(notification.getTitle(), notification.getBody());
    }
}
```

### 3.3 Suscribirse a Tópicos
En tu `MainActivity.java`:
```java
// Suscribirse al tópico de todos los usuarios
FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
    .addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "Suscrito exitosamente al tópico: allUsers");
            } else {
                Log.e(TAG, "Error al suscribirse al tópico", task.getException());
            }
        }
    });
```

## 🧪 Paso 4: Probar Notificaciones

### 4.1 Obtener Token FCM
1. Ejecuta la app en un dispositivo físico
2. Abre los logs de Android Studio
3. Busca el token FCM en los logs:
   ```
   Token FCM: [TOKEN_LARGO_AQUÍ]
   ```

### 4.2 Enviar Notificación de Prueba
1. Ve a **"Cloud Messaging"** en Firebase Console
2. Haz clic en **"Enviar tu primer mensaje"**
3. **Título**: "Bienvenido a KameHouse Laguna"
4. **Texto**: "Descubre nuestra colección de figuras de acción"
5. **Destinatario**: Selecciona tu app
6. Haz clic en **"Revisar"** y luego **"Publicar"**

### 4.3 Verificar Recepción
1. Verifica que la notificación aparezca en el dispositivo
2. Revisa los logs para confirmar que se recibió
3. Toca la notificación para verificar que abra la app

## 📊 Paso 5: Configurar Tópicos

### 5.1 Crear Tópicos
En tu app, suscribe a los usuarios a tópicos relevantes:
```java
// Suscribirse a tópicos específicos
FirebaseMessaging.getInstance().subscribeToTopic("nuevas_figuras");
FirebaseMessaging.getInstance().subscribeToTopic("ofertas_especiales");
FirebaseMessaging.getInstance().subscribeToTopic("noticias");
```

### 5.2 Enviar a Tópicos
Desde Firebase Console:
1. Ve a **"Cloud Messaging"**
2. Haz clic en **"Nueva campaña"**
3. Selecciona **"Notificación"**
4. **Destinatario**: Selecciona **"Tópico"**
5. Ingresa el nombre del tópico (ej: `nuevas_figuras`)
6. Configura el mensaje y envía

## 🔧 Paso 6: Configurar Servidor Backend (Opcional)

### 6.1 Usar Firebase Admin SDK
Para enviar notificaciones desde un servidor:

```javascript
// Node.js con Firebase Admin SDK
const admin = require('firebase-admin');

// Inicializar Admin SDK
admin.initializeApp({
    credential: admin.credential.applicationDefault(),
});

// Enviar notificación
const message = {
    notification: {
        title: 'Nueva Figura Disponible',
        body: 'Se ha agregado Batman al catálogo'
    },
    topic: 'allUsers'
};

admin.messaging().send(message)
    .then((response) => {
        console.log('Notificación enviada:', response);
    })
    .catch((error) => {
        console.log('Error al enviar:', error);
    });
```

### 6.2 Usar HTTP API
```bash
# Enviar notificación usando cURL
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "/topics/allUsers",
    "notification": {
      "title": "Nueva Figura Disponible",
      "body": "Se ha agregado Batman al catálogo"
    }
  }'
```

## 🚨 Solución de Problemas

### Error: "Token not found"
- **Causa**: Token FCM no generado
- **Solución**: Verifica que Google Play Services esté instalado

### Error: "Invalid registration token"
- **Causa**: Token FCM inválido o expirado
- **Solución**: Obtén un nuevo token FCM

### Error: "Notificaciones no aparecen"
- **Causa**: Canal de notificación no configurado
- **Solución**: Crea el canal de notificación para Android 8.0+

### Error: "App no se abre al tocar notificación"
- **Causa**: Intent mal configurado
- **Solución**: Verifica la configuración del PendingIntent

### Error: "Notificaciones no llegan en segundo plano"
- **Causa**: App optimizada por el sistema
- **Solución**: Desactiva la optimización de batería para tu app

## 📊 Monitoreo y Analytics

### 1. Métricas de FCM
1. Ve a **"Cloud Messaging"** en Firebase Console
2. Revisa las métricas:
   - **Enviados**: Número de mensajes enviados
   - **Entregados**: Número de mensajes entregados
   - **Aperturas**: Número de notificaciones abiertas
   - **Conversiones**: Acciones realizadas

### 2. Configurar Eventos Personalizados
```java
// En tu FirebaseMessagingService
@Override
public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    // Enviar evento a Analytics
    Bundle bundle = new Bundle();
    bundle.putString("notification_title", remoteMessage.getNotification().getTitle());
    FirebaseAnalytics.getInstance(this).logEvent("notification_received", bundle);
}
```

## 🔒 Mejores Prácticas

### Seguridad
1. **Validar tokens**: Verifica tokens FCM en el servidor
2. **Limitar tópicos**: No suscribir a tópicos sensibles
3. **Auditar mensajes**: Revisa logs de mensajes enviados
4. **Rate limiting**: Implementa límites de envío

### UX/UI
1. **Timing apropiado**: Envía notificaciones en horarios adecuados
2. **Contenido relevante**: Mensajes útiles para el usuario
3. **Frecuencia moderada**: No saturar con notificaciones
4. **Personalización**: Permite al usuario configurar preferencias

### Rendimiento
1. **Batch sending**: Agrupa mensajes cuando sea posible
2. **Cache tokens**: Almacena tokens localmente
3. **Retry logic**: Implementa reintentos para fallos
4. **Monitoring**: Supervisa tasas de entrega

## 📱 Configuración Avanzada

### 1. Notificaciones con Datos
```java
// Enviar notificación con datos personalizados
Map<String, String> data = new HashMap<>();
data.put("figura_id", "figura_001");
data.put("action", "view_figure");

RemoteMessage.Builder builder = new RemoteMessage.Builder("to");
builder.setData(data);
```

### 2. Notificaciones Programadas
```java
// Programar notificación local
AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
Intent intent = new Intent(this, NotificationReceiver.class);
PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

// Programar para 1 hora
alarmManager.set(AlarmManager.RTC_WAKEUP, 
    System.currentTimeMillis() + 3600000, pendingIntent);
```

### 3. Notificaciones con Imágenes
```java
// Notificación con imagen grande
NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("Nueva Figura")
    .setContentText("Batman está disponible")
    .setStyle(new NotificationCompat.BigPictureStyle()
        .bigPicture(bitmap))
    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
```

## 💰 Consideraciones de Costos

### Facturación
- **FCM es gratuito** para la mayoría de casos de uso
- **Límites**: 1 millón de mensajes/mes gratis
- **Exceso**: $0.40 por 1,000 mensajes adicionales

### Optimización de Costos
1. **Segmentación**: Envía solo a usuarios relevantes
2. **Frecuencia**: Limita número de mensajes por usuario
3. **Tópicos**: Usa tópicos en lugar de tokens individuales
4. **Analytics**: Monitorea efectividad de mensajes

---

## ✅ Checklist de Configuración

- [ ] Cloud Messaging habilitado
- [ ] Dependencias agregadas
- [ ] AndroidManifest.xml configurado
- [ ] Servicio de mensajería implementado
- [ ] Canal de notificaciones creado
- [ ] Suscripción a tópicos implementada
- [ ] Notificación de prueba enviada
- [ ] Recepción de notificaciones verificada
- [ ] Manejo de errores implementado
- [ ] Monitoreo configurado
- [ ] Backend configurado (opcional)

**¡Firebase Cloud Messaging está listo para usar! 🎉**
