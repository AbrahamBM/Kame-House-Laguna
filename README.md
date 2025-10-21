# KameHouse Laguna - Catálogo de Figuras de Acción

Una aplicación Android desarrollada en Java que permite gestionar un catálogo de figuras de acción con Firebase como backend. La aplicación incluye autenticación, base de datos en tiempo real, almacenamiento de imágenes y notificaciones push.

## 🚀 Características

- **Catálogo de Figuras**: Visualización de figuras con imágenes, precios y descripciones
- **Autenticación**: Login/registro con email y contraseña
- **Panel de Administrador**: CRUD completo de figuras y notificaciones
- **Notificaciones Push**: Sistema de notificaciones en tiempo real
- **Almacenamiento de Imágenes**: Subida de imágenes a Firebase Storage
- **Sincronización en Tiempo Real**: Datos actualizados automáticamente con Firestore

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Java
- **Arquitectura**: MVC
- **Backend**: Firebase
  - Authentication (Email/Password)
  - Cloud Firestore (Base de datos NoSQL)
  - Firebase Storage (Almacenamiento de imágenes)
  - Cloud Messaging (Notificaciones push)
- **UI**: XML Layouts con Material Design
- **Librerías**: Glide (carga de imágenes), Material Components

## 📱 Funcionalidades

### Para Usuarios
- Ver catálogo de figuras
- Ver detalles de figuras
- Compartir figuras
- Recibir notificaciones push
- Iniciar sesión o continuar como invitado

### Para Administradores
- Gestionar figuras (agregar, editar, eliminar)
- Subir imágenes de figuras
- Crear y enviar notificaciones
- Acceso completo al sistema

## 🔧 Configuración de Firebase

### Paso 1: Crear Proyecto en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en "Crear un proyecto"
3. Ingresa el nombre del proyecto: `kamehouse-laguna`
4. Habilita Google Analytics (opcional)
5. Selecciona o crea una cuenta de Analytics
6. Haz clic en "Crear proyecto"

### Paso 2: Registrar la App Android

1. En la consola de Firebase, haz clic en "Agregar app" y selecciona Android
2. Ingresa los siguientes datos:
   - **Nombre del paquete Android**: `com.kamehouselaguna`
   - **Apodo de la app**: `KameHouse Laguna`
   - **Certificado de firma SHA-1**: (opcional para desarrollo)
3. Haz clic en "Registrar app"
4. Descarga el archivo `google-services.json`
5. Coloca el archivo en la carpeta `app/` del proyecto

### Paso 3: Configurar Authentication

1. En la consola de Firebase, ve a "Authentication"
2. Haz clic en "Comenzar"
3. Ve a la pestaña "Sign-in method"
4. Habilita "Correo electrónico/contraseña"
5. Haz clic en "Guardar"

### Paso 4: Configurar Firestore Database

1. En la consola de Firebase, ve a "Firestore Database"
2. Haz clic en "Crear base de datos"
3. Selecciona "Comenzar en modo de prueba" (para desarrollo)
4. Elige una ubicación para la base de datos (recomendado: us-central1)
5. Haz clic en "Listo"

### Paso 5: Configurar Firebase Storage

1. En la consola de Firebase, ve a "Storage"
2. Haz clic en "Comenzar"
3. Revisa las reglas de seguridad (modo de prueba para desarrollo)
4. Elige la misma ubicación que Firestore
5. Haz clic en "Siguiente" y luego "Listo"

### Paso 6: Configurar Cloud Messaging

1. En la consola de Firebase, ve a "Cloud Messaging"
2. No requiere configuración adicional para el funcionamiento básico
3. Para notificaciones personalizadas, puedes configurar un servidor backend

### Paso 7: Crear Usuario Administrador

1. Ejecuta la aplicación en Android Studio
2. Ve a la pantalla de login
3. Registra una cuenta con email y contraseña
4. En la consola de Firebase, ve a "Firestore Database"
5. Crea una colección llamada `usuarios`
6. Crea un documento con el UID del usuario registrado
7. Agrega los siguientes campos:
   ```json
   {
     "email": "admin@figuras.com",
     "rol": "admin",
     "fechaCreacion": "2025-01-20T10:00:00Z",
     "ultimoAcceso": "2025-01-20T10:00:00Z"
   }
   ```

## 🏗️ Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/kamehouselaguna/
│   │   ├── MainActivity.java                 # Actividad principal
│   │   ├── FigureDetailActivity.java         # Detalles de figura
│   │   ├── LoginActivity.java                # Autenticación
│   │   ├── AdminPanelActivity.java           # Panel de administrador
│   │   ├── AddEditFigureActivity.java        # Agregar/editar figuras
│   │   ├── FirebaseMessagingService.java     # Servicio de notificaciones
│   │   ├── models/                           # Modelos de datos
│   │   │   ├── Figure.java
│   │   │   ├── Notification.java
│   │   │   └── User.java
│   │   ├── helpers/                          # Clases helper
│   │   │   ├── FirestoreHelper.java
│   │   │   ├── StorageHelper.java
│   │   │   └── NotificationHelper.java
│   │   ├── adapters/                         # Adaptadores RecyclerView
│   │   │   ├── FigureAdapter.java
│   │   │   ├── FigureAdminAdapter.java
│   │   │   └── NotificationAdminAdapter.java
│   │   └── fragments/                        # Fragments
│   │       ├── FigurasAdminFragment.java
│   │       └── NotificacionesAdminFragment.java
│   ├── res/layout/                           # Layouts XML
│   └── AndroidManifest.xml
├── google-services.json                      # Configuración Firebase
└── build.gradle.kts                          # Dependencias
```

## 📊 Estructura de Datos en Firestore

### Colección: `figuras`
```json
{
  "id": "figura_001",
  "nombre": "Batman",
  "descripcion": "Figura edición limitada de Batman",
  "imagen_url": "https://firebasestorage.googleapis.com/...",
  "precio": 499.0,
  "fechaCreacion": "2025-01-20T10:00:00Z",
  "fechaActualizacion": "2025-01-20T10:00:00Z"
}
```

### Colección: `notificaciones`
```json
{
  "id": "notif_001",
  "titulo": "Nueva figura disponible",
  "mensaje": "Se ha agregado Iron Man al catálogo",
  "fecha": "2025-01-20T10:00:00Z",
  "autorId": "user_uid",
  "autorEmail": "admin@figuras.com"
}
```

### Colección: `usuarios`
```json
{
  "uid": "user_uid",
  "email": "admin@figuras.com",
  "rol": "admin",
  "fechaCreacion": "2025-01-20T10:00:00Z",
  "ultimoAcceso": "2025-01-20T10:00:00Z"
}
```

## 🚀 Instalación y Ejecución

### Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Dispositivo Android con API 24+ o emulador

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd KameHouseLaguna
   ```

2. **Configurar Firebase**
   - Sigue los pasos de configuración de Firebase arriba
   - Coloca el archivo `google-services.json` en la carpeta `app/`

3. **Sincronizar dependencias**
   - Abre el proyecto en Android Studio
   - Haz clic en "Sync Now" cuando aparezca la notificación
   - O ve a File > Sync Project with Gradle Files

4. **Ejecutar la aplicación**
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en el botón "Run" (▶️) en Android Studio
   - O presiona Shift + F10

## 🔐 Configuración de Seguridad

### Reglas de Firestore (Desarrollo)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para figuras (lectura pública, escritura solo admin)
    match /figuras/{document} {
      allow read: if true;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para notificaciones (lectura pública, escritura solo admin)
    match /notificaciones/{document} {
      allow read: if true;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para usuarios (lectura/escritura solo del propio usuario)
    match /usuarios/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Reglas de Storage (Desarrollo)
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /figuras/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
  }
}
```

## 🧪 Pruebas

### Pruebas Básicas
1. **Registro de usuario**: Crea una cuenta nueva
2. **Login**: Inicia sesión con credenciales válidas
3. **Visualización**: Verifica que las figuras se cargan correctamente
4. **Detalles**: Toca una figura para ver sus detalles
5. **Compartir**: Prueba la funcionalidad de compartir
6. **Notificaciones**: Verifica que se reciben notificaciones push

### Pruebas de Administrador
1. **Acceso**: Inicia sesión con cuenta de administrador
2. **CRUD Figuras**: Agrega, edita y elimina figuras
3. **Imágenes**: Sube imágenes y verifica que se muestran
4. **Notificaciones**: Crea y envía notificaciones

## 🐛 Solución de Problemas

### Error: "google-services.json not found"
- Verifica que el archivo esté en la carpeta `app/`
- Asegúrate de que el nombre del paquete coincida

### Error: "Firebase not initialized"
- Verifica que el plugin de Google Services esté aplicado
- Revisa que las dependencias de Firebase estén correctas

### Error: "Permission denied"
- Verifica las reglas de seguridad de Firestore
- Asegúrate de que el usuario esté autenticado
- Revisa que el rol de administrador esté configurado correctamente

### Las imágenes no se cargan
- Verifica las reglas de Storage
- Asegúrate de que las URLs de las imágenes sean válidas
- Revisa la conexión a internet

## 📝 Notas de Desarrollo

- **Modo Debug**: Las reglas de seguridad están en modo de prueba para desarrollo
- **Producción**: Cambia las reglas de seguridad antes de publicar
- **Backup**: Configura backups automáticos de Firestore
- **Monitoreo**: Usa Firebase Analytics para monitorear el uso

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

Si tienes problemas o preguntas:
- Abre un issue en GitHub
- Revisa la documentación de Firebase
- Consulta los logs de Android Studio

---

**¡Disfruta desarrollando con KameHouse Laguna! 🐉**
