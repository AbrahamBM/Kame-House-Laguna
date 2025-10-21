# 🔥 Configuración Completa de Firebase - KameHouse Laguna

Esta guía te lleva paso a paso a través de toda la configuración de Firebase para el proyecto KameHouse Laguna.

## 📋 Resumen de Componentes

El proyecto utiliza los siguientes servicios de Firebase:
- ✅ **Authentication** - Login/registro de usuarios
- ✅ **Cloud Firestore** - Base de datos en tiempo real
- ✅ **Firebase Storage** - Almacenamiento de imágenes
- ✅ **Cloud Messaging** - Notificaciones push

## 🚀 Configuración Paso a Paso

### 1. Crear Proyecto Firebase
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en **"Crear un proyecto"**
3. **Nombre**: `kamehouse-laguna`
4. Habilita Google Analytics (opcional)
5. Haz clic en **"Crear proyecto"**

### 2. Registrar App Android
1. En la consola de Firebase, haz clic en **"Agregar app"** > Android
2. **Nombre del paquete**: `com.kamehouselaguna`
3. **Apodo**: `KameHouse Laguna`
4. Descarga `google-services.json`
5. Coloca el archivo en la carpeta `app/` del proyecto

### 3. Configurar Authentication
📖 **Guía detallada**: [firebase-authentication-setup.md](firebase-authentication-setup.md)

**Pasos rápidos**:
1. Ve a **Authentication** > **Sign-in method**
2. Habilita **Email/Password**
3. Personaliza templates de email
4. Crea usuario administrador:
   - Email: `admin@kamehouselaguna.com`
   - Password: `Admin123456`

### 4. Configurar Firestore Database
📖 **Guía detallada**: [firebase-firestore-setup.md](firebase-firestore-setup.md)

**Pasos rápidos**:
1. Ve a **Firestore Database** > **Crear base de datos**
2. Selecciona **"Modo de prueba"** (para desarrollo)
3. Ubicación: `us-central1`
4. Crea colecciones:
   - `usuarios` - Datos de usuarios
   - `figuras` - Catálogo de figuras
   - `notificaciones` - Notificaciones del sistema

### 5. Configurar Firebase Storage
📖 **Guía detallada**: [firebase-storage-setup.md](firebase-storage-setup.md)

**Pasos rápidos**:
1. Ve a **Storage** > **Comenzar**
2. Revisa reglas de seguridad (modo de prueba)
3. Crea carpeta `figuras` para imágenes
4. Sube imagen de prueba

### 6. Configurar Cloud Messaging
📖 **Guía detallada**: [firebase-messaging-setup.md](firebase-messaging-setup.md)

**Pasos rápidos**:
1. Ve a **Cloud Messaging** (se habilita automáticamente)
2. No requiere configuración adicional
3. Prueba enviando notificación desde la consola

## 🗂️ Estructura de Datos en Firestore

### Colección: `usuarios`
```json
{
  "uid": "user_uid_here",
  "email": "admin@kamehouselaguna.com",
  "rol": "admin",
  "fechaCreacion": "2025-01-20T10:00:00Z",
  "ultimoAcceso": "2025-01-20T10:00:00Z"
}
```

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
  "mensaje": "Se ha agregado Batman al catálogo",
  "fecha": "2025-01-20T10:00:00Z",
  "autorId": "admin_uid",
  "autorEmail": "admin@kamehouselaguna.com"
}
```

## 🔒 Reglas de Seguridad

### Firestore (Desarrollo)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /figuras/{document} {
      allow read: if true;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    match /notificaciones/{document} {
      allow read: if true;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    match /usuarios/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Storage (Desarrollo)
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

## 👤 Crear Usuario Administrador

### Método 1: Desde la App
1. Ejecuta la app
2. Ve a la pantalla de login
3. Haz clic en **"Registrarse"**
4. Crea cuenta con email: `admin@kamehouselaguna.com`
5. Ve a Firestore Console
6. En la colección `usuarios`, encuentra el documento con el UID del usuario
7. Cambia el campo `rol` de `"usuario"` a `"admin"`

### Método 2: Desde Firebase Console
1. Ve a **Authentication** > **Users**
2. Haz clic en **"Add user"**
3. Email: `admin@kamehouselaguna.com`
4. Password: `Admin123456`
5. Ve a **Firestore Database**
6. Crea documento en `usuarios` con:
   - **ID**: UID del usuario creado
   - **email**: `admin@kamehouselaguna.com`
   - **rol**: `admin`

## 🧪 Probar la Configuración

### 1. Probar Registro de Usuario
1. Ejecuta la app
2. Ve a login > **"Registrarse"**
3. Crea una cuenta nueva
4. Verifica que se cree en Authentication
5. Verifica que se cree documento en Firestore

### 2. Probar Login
1. Inicia sesión con la cuenta creada
2. Verifica que funcione correctamente
3. Si es admin, verifica que redirija al panel de administrador

### 3. Probar Panel de Administrador
1. Inicia sesión como admin
2. Ve al panel de administrador
3. Intenta agregar una figura con imagen
4. Verifica que se guarde en Firestore y Storage

### 4. Probar Notificaciones
1. En Firebase Console > **Cloud Messaging**
2. Envía notificación de prueba
3. Verifica que llegue al dispositivo

## 🚨 Solución de Problemas Comunes

### Error: "google-services.json not found"
- **Solución**: Descarga el archivo desde Firebase Console y colócalo en `app/`

### Error: "Permission denied"
- **Solución**: Verifica las reglas de seguridad de Firestore/Storage

### Error: "User not authenticated"
- **Solución**: Asegúrate de que el usuario esté logueado

### Error: "Invalid role"
- **Solución**: Verifica que el usuario tenga rol de admin en Firestore

### Error: "Image upload failed"
- **Solución**: Verifica permisos de Storage y tamaño de imagen

### Error: "Notifications not received"
- **Solución**: Verifica que el dispositivo tenga Google Play Services

## 📊 Monitoreo y Mantenimiento

### Métricas Importantes
- **Usuarios activos** diarios/semanales
- **Figuras agregadas** por administradores
- **Notificaciones enviadas** y tasa de apertura
- **Uso de Storage** (espacio utilizado)

### Tareas de Mantenimiento
- **Revisar logs** de errores regularmente
- **Monitorear uso** de Firestore y Storage
- **Actualizar reglas** de seguridad según necesidades
- **Hacer backups** de datos importantes

## 🔄 Migración a Producción

### Antes de Lanzar
1. **Cambiar reglas** de Firestore a modo producción
2. **Cambiar reglas** de Storage a modo producción
3. **Configurar backups** automáticos
4. **Configurar monitoreo** y alertas
5. **Probar exhaustivamente** todas las funcionalidades

### Reglas de Producción
- Usar reglas más restrictivas
- Validar datos en servidor
- Implementar rate limiting
- Configurar auditoría

## 📞 Soporte y Recursos

### Documentación Oficial
- [Firebase Documentation](https://firebase.google.com/docs)
- [Android Setup Guide](https://firebase.google.com/docs/android/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

### Comunidad
- [Firebase Community](https://firebase.google.com/community)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/firebase)
- [Firebase YouTube Channel](https://www.youtube.com/user/Firebase)

---

## ✅ Checklist Final

- [ ] Proyecto Firebase creado
- [ ] App Android registrada
- [ ] `google-services.json` descargado
- [ ] Authentication configurado
- [ ] Firestore Database creado
- [ ] Firebase Storage habilitado
- [ ] Cloud Messaging configurado
- [ ] Usuario administrador creado
- [ ] Reglas de seguridad configuradas
- [ ] Estructura de datos creada
- [ ] App probada completamente
- [ ] Notificaciones funcionando
- [ ] Monitoreo configurado

**¡Firebase está completamente configurado y listo para usar! 🎉**

---

## 📁 Archivos de Configuración Creados

- `firebase-authentication-setup.md` - Guía de Authentication
- `firebase-firestore-setup.md` - Guía de Firestore Database
- `firebase-storage-setup.md` - Guía de Firebase Storage
- `firebase-messaging-setup.md` - Guía de Cloud Messaging
- `firebase-setup.md` - Reglas de seguridad
- `FIREBASE-SETUP-COMPLETE.md` - Esta guía completa

**¡Disfruta desarrollando con Firebase! 🚀**
