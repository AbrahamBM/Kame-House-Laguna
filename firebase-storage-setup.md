# 📁 Configuración de Firebase Storage

Esta guía te explica paso a paso cómo configurar Firebase Storage para el proyecto KameHouse Laguna.

## 📋 Requisitos Previos

- Proyecto Firebase creado
- Firebase Authentication configurado
- App Android registrada en Firebase

## 🚀 Paso 1: Habilitar Firebase Storage

### 1.1 Acceder a Storage
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `kamehouse-laguna`
3. En el menú lateral, haz clic en **"Storage"**

### 1.2 Crear Bucket de Storage
1. Haz clic en **"Comenzar"** (Get started)
2. Revisa las reglas de seguridad (modo de prueba para desarrollo)
3. Elige la misma ubicación que Firestore:
   - **Recomendado**: `us-central1` (Iowa, USA)
4. Haz clic en **"Siguiente"** y luego **"Listo"**

## 🔒 Paso 2: Configurar Reglas de Seguridad

### 2.1 Reglas para Desarrollo
1. Ve a la pestaña **"Reglas"**
2. Reemplaza el contenido con:

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

3. Haz clic en **"Publicar"**

### 2.2 Reglas para Producción
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

## 📁 Paso 3: Organizar Estructura de Archivos

### 3.1 Crear Carpetas
1. En la pestaña **"Archivos"**
2. Haz clic en **"Crear carpeta"**
3. **Nombre**: `figuras`
4. Haz clic en **"Crear"**

### 3.2 Subir Imagen de Prueba
1. Haz clic en la carpeta `figuras`
2. Haz clic en **"Subir archivo"**
3. Selecciona una imagen de prueba
4. **Recomendaciones**:
   - **Formato**: JPG, PNG o WebP
   - **Tamaño**: Máximo 2MB
   - **Resolución**: 800x600 o similar
5. Haz clic en **"Subir"**

## 🔧 Paso 4: Configurar en la App

### 4.1 Verificar Dependencias
Asegúrate de que `app/build.gradle.kts` incluya:
```kotlin
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.storage)
    // ... otras dependencias
}
```

### 4.2 Verificar Permisos
En `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
```

### 4.3 Inicializar Storage
En tu clase helper `StorageHelper.java`:
```java
private FirebaseStorage storage;
private StorageReference storageRef;

public StorageHelper() {
    storage = FirebaseStorage.getInstance();
    storageRef = storage.getReference();
}
```

## 🖼️ Paso 5: Implementar Funcionalidades

### 5.1 Subir Imagen
```java
public void uploadFigureImage(Uri imageUri, OnImageUploadListener listener) {
    String fileName = "figura_" + UUID.randomUUID().toString() + ".jpg";
    StorageReference imageRef = storageRef.child("figuras").child(fileName);
    
    UploadTask uploadTask = imageRef.putFile(imageUri);
    
    uploadTask.addOnSuccessListener(taskSnapshot -> {
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            listener.onSuccess(uri.toString());
        });
    }).addOnFailureListener(e -> {
        listener.onError(e.getMessage());
    });
}
```

### 5.2 Eliminar Imagen
```java
public void deleteFigureImage(String imageUrl, OnImageDeleteListener listener) {
    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
    
    imageRef.delete().addOnSuccessListener(aVoid -> {
        listener.onSuccess("Imagen eliminada exitosamente");
    }).addOnFailureListener(e -> {
        listener.onError(e.getMessage());
    });
}
```

### 5.3 Obtener URL de Descarga
```java
public void getDownloadUrl(String imagePath, OnUrlReceivedListener listener) {
    StorageReference imageRef = storageRef.child(imagePath);
    
    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
        listener.onUrlReceived(uri.toString());
    }).addOnFailureListener(e -> {
        listener.onError(e.getMessage());
    });
}
```

## 🔍 Paso 6: Probar la Configuración

### 6.1 Probar Subida de Imagen
1. Ejecuta la app en Android Studio
2. Inicia sesión como administrador
3. Ve al panel de administrador
4. Intenta agregar una nueva figura con imagen
5. Verifica que la imagen se suba correctamente

### 6.2 Probar Visualización
1. Ve a la pantalla principal
2. Verifica que las imágenes se muestren correctamente
3. Revisa que no haya errores en los logs

### 6.3 Probar Eliminación
1. En el panel de administrador
2. Intenta eliminar una figura
3. Verifica que la imagen también se elimine del Storage

## 🚨 Solución de Problemas

### Error: "User does not have permission"
- **Causa**: Reglas de seguridad muy restrictivas
- **Solución**: Verifica que el usuario tenga rol de admin

### Error: "File too large"
- **Causa**: Archivo excede el límite de tamaño
- **Solución**: Comprime la imagen o reduce su tamaño

### Error: "Invalid content type"
- **Causa**: Tipo de archivo no permitido
- **Solución**: Usa solo imágenes (JPG, PNG, WebP)

### Error: "Network request failed"
- **Causa**: Sin conexión a internet
- **Solución**: Verifica la conexión a internet

### Error: "Object not found"
- **Causa**: Archivo no existe en Storage
- **Solución**: Verifica que el archivo exista

## 📊 Monitoreo y Optimización

### 1. Monitorear Uso
1. Ve a la pestaña **"Uso"** en Storage Console
2. Revisa:
   - **Almacenamiento**: Espacio utilizado
   - **Descargas**: Número de descargas
   - **Operaciones**: Lecturas/escrituras

### 2. Optimizar Imágenes
- **Comprimir imágenes** antes de subir
- **Usar formatos eficientes** (WebP, JPEG optimizado)
- **Redimensionar** para el tamaño necesario
- **Implementar cache** en la app

### 3. Configurar Lifecycle
```javascript
// Reglas con lifecycle
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /figuras/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && 
        firestore.get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' &&
        request.resource.size < 2 * 1024 * 1024;
    }
  }
}
```

## 💰 Consideraciones de Costos

### Facturación
- **Almacenamiento**: $0.026 por GB/mes
- **Operaciones de descarga**: $0.12 por GB
- **Operaciones de carga**: $0.12 por GB
- **Operaciones de eliminación**: $0.01 por 10,000 operaciones

### Optimización de Costos
1. **Comprimir imágenes**: Reduce tamaño de archivos
2. **Usar CDN**: Aprovecha la red de entrega de contenido
3. **Cache local**: Evita descargas repetidas
4. **Limpieza automática**: Elimina archivos no utilizados

## 🔒 Mejores Prácticas

### Seguridad
1. **Validar tipos de archivo**: Solo permitir imágenes
2. **Limitar tamaño**: Establecer límites razonables
3. **Verificar autenticación**: Solo usuarios autenticados
4. **Escaneo de malware**: Considerar escaneo de archivos

### Rendimiento
1. **Comprimir imágenes**: Optimizar antes de subir
2. **Usar thumbnails**: Crear versiones pequeñas
3. **Implementar cache**: Cache local y remoto
4. **Lazy loading**: Cargar imágenes bajo demanda

### UX/UI
1. **Indicadores de progreso**: Mostrar progreso de subida
2. **Manejo de errores**: Mensajes claros al usuario
3. **Fallbacks**: Imágenes por defecto si falla la carga
4. **Optimización**: Cargar imágenes en calidad apropiada

## 🔄 Backup y Recuperación

### Backup Automático
1. Ve a **"Configuración"** > **"Backups"**
2. Habilita **"Backups automáticos"**
3. Configura frecuencia y retención

### Exportar Datos
```bash
# Usando gsutil
gsutil -m cp -r gs://your-bucket/figuras ./backup/
```

### Importar Datos
```bash
# Usando gsutil
gsutil -m cp -r ./backup/figuras gs://your-bucket/
```

## 📱 Integración con Glide

### Configurar Glide para Firebase Storage
```java
// En tu Application class
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Configurar Glide para Firebase Storage
        Glide.with(this)
            .using(new FirebaseImageLoader())
            .load(storageRef)
            .into(imageView);
    }
}
```

### Cache de Imágenes
```java
// Configurar cache de Glide
Glide.with(context)
    .load(imageUrl)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .into(imageView);
```

---

## ✅ Checklist de Configuración

- [ ] Firebase Storage habilitado
- [ ] Bucket creado
- [ ] Reglas de seguridad configuradas
- [ ] Estructura de carpetas creada
- [ ] Imagen de prueba subida
- [ ] Dependencias agregadas
- [ ] Permisos configurados
- [ ] Funcionalidades implementadas
- [ ] Pruebas realizadas
- [ ] Monitoreo configurado
- [ ] Backups configurados

**¡Firebase Storage está listo para usar! 🎉**
