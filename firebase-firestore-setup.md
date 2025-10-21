# 🗄️ Configuración de Firestore Database

Esta guía te explica paso a paso cómo configurar Cloud Firestore para el proyecto KameHouse Laguna.

## 📋 Requisitos Previos

- Proyecto Firebase creado
- Firebase Authentication configurado
- App Android registrada en Firebase

## 🚀 Paso 1: Crear Base de Datos

### 1.1 Acceder a Firestore
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `kamehouse-laguna`
3. En el menú lateral, haz clic en **"Firestore Database"**

### 1.2 Crear Base de Datos
1. Haz clic en **"Crear base de datos"**
2. Selecciona **"Comenzar en modo de prueba"** (para desarrollo)
3. **Importante**: Cambia a modo de producción antes de lanzar la app
4. Elige una ubicación:
   - **Recomendado**: `us-central1` (Iowa, USA)
   - **Alternativas**: `europe-west1` (Bélgica), `asia-southeast1` (Singapur)
5. Haz clic en **"Listo"**

## 🏗️ Paso 2: Configurar Estructura de Datos

### 2.1 Crear Colección de Usuarios
1. Haz clic en **"Comenzar colección"**
2. **ID de colección**: `usuarios`
3. **Primer documento**:
   - **ID del documento**: `[UID_DEL_ADMIN]` (copia el UID del usuario admin)
   - **Campos**:
     ```
     email: string = "admin@kamehouselaguna.com"
     rol: string = "admin"
     fechaCreacion: timestamp = [fecha actual]
     ultimoAcceso: timestamp = [fecha actual]
     ```
4. Haz clic en **"Guardar"**

### 2.2 Crear Colección de Figuras
1. Haz clic en **"Comenzar colección"**
2. **ID de colección**: `figuras`
3. **Primer documento** (ejemplo):
   - **ID del documento**: `figura_001`
   - **Campos**:
     ```
     nombre: string = "Batman"
     descripcion: string = "Figura edición limitada de Batman"
     imagen_url: string = "https://example.com/batman.jpg"
     precio: number = 499.0
     fechaCreacion: timestamp = [fecha actual]
     fechaActualizacion: timestamp = [fecha actual]
     ```
4. Haz clic en **"Guardar"**

### 2.3 Crear Colección de Notificaciones
1. Haz clic en **"Comenzar colección"**
2. **ID de colección**: `notificaciones`
3. **Primer documento** (ejemplo):
   - **ID del documento**: `notif_001`
   - **Campos**:
     ```
     titulo: string = "Bienvenido a KameHouse Laguna"
     mensaje: string = "Descubre nuestra colección de figuras de acción"
     fecha: timestamp = [fecha actual]
     autorId: string = "[UID_DEL_ADMIN]"
     autorEmail: string = "admin@kamehouselaguna.com"
     ```
4. Haz clic en **"Guardar"**

## 🔒 Paso 3: Configurar Reglas de Seguridad

### 3.1 Reglas para Desarrollo
1. Ve a la pestaña **"Reglas"**
2. Reemplaza el contenido con:

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
      allow create: if request.auth != null && 
        request.auth.uid == userId &&
        request.resource.data.keys().hasAll(['email', 'rol']) &&
        request.resource.data.rol in ['usuario', 'admin'];
    }
  }
}
```

3. Haz clic en **"Publicar"**

### 3.2 Reglas para Producción
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para figuras
    match /figuras/{document} {
      allow read: if true;
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
      allow read: if true;
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
      allow read, write: if request.auth != null && 
        request.auth.uid == userId &&
        request.auth.token.email_verified == true;
      
      allow create: if request.auth != null && 
        request.auth.uid == userId &&
        request.auth.token.email_verified == true &&
        request.resource.data.keys().hasAll(['email', 'rol', 'fechaCreacion']) &&
        request.resource.data.email == request.auth.token.email &&
        request.resource.data.rol in ['usuario', 'admin'];
    }
  }
}
```

## 📊 Paso 4: Configurar Índices

### 4.1 Índices Automáticos
Firestore creará automáticamente índices para:
- Consultas simples por campo
- Consultas de ordenamiento

### 4.2 Índices Compuestos (Opcional)
Si planeas hacer consultas complejas, crea índices compuestos:

1. Ve a la pestaña **"Índices"**
2. Haz clic en **"Crear índice"**
3. **Colección**: `figuras`
4. **Campos**:
   - `fechaCreacion` (Ascendente)
   - `precio` (Ascendente)
5. Haz clic en **"Crear"**

## 🔍 Paso 5: Probar la Configuración

### 5.1 Probar Lectura de Datos
1. Ejecuta la app en Android Studio
2. Ve a la pantalla principal
3. Verifica que las figuras se carguen correctamente
4. Revisa los logs en Android Studio para errores

### 5.2 Probar Escritura de Datos
1. Inicia sesión como administrador
2. Ve al panel de administrador
3. Intenta agregar una nueva figura
4. Verifica que se guarde en Firestore

### 5.3 Probar Reglas de Seguridad
1. Inicia sesión como usuario normal
2. Intenta acceder al panel de administrador
3. Verifica que sea redirigido correctamente

## 📱 Paso 6: Configuración en la App

### 6.1 Verificar Dependencias
Asegúrate de que `app/build.gradle.kts` incluya:
```kotlin
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    // ... otras dependencias
}
```

### 6.2 Verificar Inicialización
En tu `Application` class o `MainActivity`:
```java
// Firestore se inicializa automáticamente
FirebaseFirestore db = FirebaseFirestore.getInstance();
```

## 🚨 Solución de Problemas

### Error: "Missing or insufficient permissions"
- **Causa**: Reglas de seguridad muy restrictivas
- **Solución**: Revisa las reglas de Firestore

### Error: "Document not found"
- **Causa**: Documento no existe o ID incorrecto
- **Solución**: Verifica que el documento exista en Firestore

### Error: "Invalid data type"
- **Causa**: Tipo de dato incorrecto en el documento
- **Solución**: Verifica los tipos de datos en Firestore

### Error: "Index not found"
- **Causa**: Consulta requiere índice compuesto
- **Solución**: Crea el índice necesario en Firestore Console

## 📈 Monitoreo y Optimización

### 1. Monitorear Uso
1. Ve a la pestaña **"Uso"** en Firestore Console
2. Revisa:
   - **Lecturas**: Número de documentos leídos
   - **Escrituras**: Número de documentos escritos
   - **Eliminaciones**: Número de documentos eliminados
   - **Almacenamiento**: Espacio utilizado

### 2. Optimizar Consultas
- **Usa índices** para consultas complejas
- **Limita resultados** con `.limit()`
- **Usa paginación** para listas grandes
- **Evita consultas anidadas** profundas

### 3. Configurar Backups
1. Ve a **"Configuración"** > **"Backups"**
2. Habilita **"Backups automáticos"**
3. Configura frecuencia (diaria/semanal)
4. Establece retención (30-365 días)

## 🔄 Migración de Datos

### Exportar Datos
```bash
# Usando gcloud CLI
gcloud firestore export gs://your-bucket/backup-folder
```

### Importar Datos
```bash
# Usando gcloud CLI
gcloud firestore import gs://your-bucket/backup-folder
```

## 💰 Consideraciones de Costos

### Facturación
- **Lecturas**: $0.06 por 100,000 documentos
- **Escrituras**: $0.18 por 100,000 documentos
- **Eliminaciones**: $0.02 por 100,000 documentos
- **Almacenamiento**: $0.18 por GB/mes

### Optimización de Costos
1. **Cache local**: Usa cache de Firestore
2. **Consultas eficientes**: Minimiza lecturas
3. **Paginación**: No cargues todos los datos
4. **Índices**: Crea solo los necesarios

## 🔒 Mejores Prácticas

### Seguridad
1. **Reglas estrictas**: Nunca uses reglas abiertas en producción
2. **Validación de datos**: Valida en cliente y servidor
3. **Autenticación**: Siempre verifica usuarios autenticados
4. **Auditoría**: Revisa logs regularmente

### Rendimiento
1. **Índices**: Crea índices para consultas frecuentes
2. **Cache**: Aprovecha el cache local
3. **Paginación**: Implementa paginación para listas
4. **Offline**: Diseña para funcionar offline

### Escalabilidad
1. **Estructura de datos**: Diseña para escalar
2. **Consultas eficientes**: Evita consultas costosas
3. **Monitoreo**: Supervisa uso y rendimiento
4. **Backups**: Configura backups automáticos

---

## ✅ Checklist de Configuración

- [ ] Base de datos creada
- [ ] Colecciones creadas (usuarios, figuras, notificaciones)
- [ ] Reglas de seguridad configuradas
- [ ] Usuario administrador creado
- [ ] Índices configurados (si es necesario)
- [ ] App probada con lectura/escritura
- [ ] Reglas de seguridad probadas
- [ ] Backups configurados
- [ ] Monitoreo configurado

**¡Firestore Database está listo para usar! 🎉**
