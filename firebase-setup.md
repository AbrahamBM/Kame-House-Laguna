# Configuración de Seguridad de Firebase

Este documento contiene las reglas de seguridad recomendadas para Firestore Database y Firebase Storage en el proyecto KameHouse Laguna.

## 🔐 Reglas de Firestore Database

### Reglas para Desarrollo (Modo de Prueba)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para figuras
    match /figuras/{document} {
      // Lectura pública para todos los usuarios
      allow read: if true;
      
      // Escritura solo para administradores autenticados
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para notificaciones
    match /notificaciones/{document} {
      // Lectura pública para todos los usuarios
      allow read: if true;
      
      // Escritura solo para administradores autenticados
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para usuarios
    match /usuarios/{userId} {
      // Lectura y escritura solo del propio usuario
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Permitir creación de usuario durante el registro
      allow create: if request.auth != null && 
        request.auth.uid == userId &&
        request.resource.data.keys().hasAll(['email', 'rol']) &&
        request.resource.data.rol in ['usuario', 'admin'];
    }
  }
}
```

### Reglas para Producción (Más Restrictivas)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para figuras
    match /figuras/{document} {
      // Lectura pública pero con validación de datos
      allow read: if true;
      
      // Escritura solo para administradores con validación estricta
      allow create: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.data.keys().hasAll(['nombre', 'descripcion', 'precio', 'fechaCreacion', 'fechaActualizacion']) &&
        request.resource.data.nombre is string &&
        request.resource.data.descripcion is string &&
        request.resource.data.precio is number &&
        request.resource.data.precio > 0;
        
      allow update: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.data.keys().hasAll(['nombre', 'descripcion', 'precio', 'fechaActualizacion']) &&
        request.resource.data.nombre is string &&
        request.resource.data.descripcion is string &&
        request.resource.data.precio is number &&
        request.resource.data.precio > 0;
        
      allow delete: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para notificaciones
    match /notificaciones/{document} {
      // Lectura pública
      allow read: if true;
      
      // Escritura solo para administradores con validación
      allow create: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.data.keys().hasAll(['titulo', 'mensaje', 'fecha', 'autorId', 'autorEmail']) &&
        request.resource.data.titulo is string &&
        request.resource.data.mensaje is string &&
        request.resource.data.autorId == request.auth.uid;
        
      allow delete: if request.auth != null && 
        get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Reglas para usuarios
    match /usuarios/{userId} {
      // Solo el propio usuario puede leer/escribir sus datos
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Creación con validación estricta
      allow create: if request.auth != null && 
        request.auth.uid == userId &&
        request.resource.data.keys().hasAll(['email', 'rol', 'fechaCreacion']) &&
        request.resource.data.email is string &&
        request.resource.data.rol in ['usuario', 'admin'] &&
        request.resource.data.email == request.auth.token.email;
    }
  }
}
```

## 🗄️ Reglas de Firebase Storage

### Reglas para Desarrollo

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Reglas para imágenes de figuras
    match /figuras/{allPaths=**} {
      // Lectura pública
      allow read: if true;
      
      // Escritura solo para administradores
      allow write: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.size < 5 * 1024 * 1024 && // Máximo 5MB
        request.resource.contentType.matches('image/.*');
    }
  }
}
```

### Reglas para Producción

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Reglas para imágenes de figuras
    match /figuras/{allPaths=**} {
      // Lectura pública
      allow read: if true;
      
      // Escritura con validaciones estrictas
      allow create: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.size < 2 * 1024 * 1024 && // Máximo 2MB
        request.resource.contentType in ['image/jpeg', 'image/png', 'image/webp'] &&
        resource == null; // Solo crear nuevos archivos
        
      allow update: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.size < 2 * 1024 * 1024 &&
        request.resource.contentType in ['image/jpeg', 'image/png', 'image/webp'];
        
      allow delete: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
  }
}
```

## 🔧 Cómo Aplicar las Reglas

### Para Firestore Database

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a "Firestore Database" > "Reglas"
4. Copia y pega las reglas correspondientes
5. Haz clic en "Publicar"

### Para Firebase Storage

1. En la consola de Firebase, ve a "Storage"
2. Haz clic en la pestaña "Reglas"
3. Copia y pega las reglas correspondientes
4. Haz clic en "Publicar"

## ⚠️ Consideraciones de Seguridad

### Desarrollo
- Las reglas de desarrollo son más permisivas para facilitar las pruebas
- **NO uses estas reglas en producción**
- Siempre valida los datos en el cliente antes de enviarlos

### Producción
- Implementa validaciones estrictas en las reglas
- Limita el tamaño de archivos subidos
- Valida tipos de contenido
- Considera implementar rate limiting
- Monitorea el uso y accesos

### Mejores Prácticas

1. **Principio de Menor Privilegio**: Solo otorga los permisos mínimos necesarios
2. **Validación de Datos**: Valida tanto en cliente como en servidor
3. **Autenticación**: Siempre verifica que el usuario esté autenticado
4. **Autorización**: Verifica roles y permisos específicos
5. **Monitoreo**: Revisa regularmente los logs de acceso
6. **Backup**: Configura backups automáticos de las reglas

## 🚨 Reglas de Emergencia (Solo Lectura)

Si necesitas deshabilitar temporalmente todas las escrituras:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read: if true;
      allow write: if false; // Bloquea todas las escrituras
    }
  }
}
```

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if false; // Bloquea todas las escrituras
    }
  }
}
```

## 📊 Monitoreo y Logs

### Firestore
- Ve a "Firestore Database" > "Uso" para ver estadísticas
- Revisa "Reglas" > "Registros de reglas" para ver intentos de acceso

### Storage
- Ve a "Storage" > "Uso" para ver estadísticas de almacenamiento
- Revisa los logs de Cloud Functions si usas triggers

## 🔄 Actualización de Reglas

1. **Prueba en Desarrollo**: Siempre prueba las reglas en un entorno de desarrollo
2. **Backup**: Haz backup de las reglas actuales antes de cambiar
3. **Implementación Gradual**: Considera implementar cambios gradualmente
4. **Monitoreo**: Supervisa el comportamiento después de los cambios
5. **Rollback**: Ten un plan de rollback en caso de problemas

---

**⚠️ Importante**: Estas reglas son ejemplos y deben adaptarse según las necesidades específicas de tu aplicación. Siempre revisa y prueba las reglas antes de aplicarlas en producción.
