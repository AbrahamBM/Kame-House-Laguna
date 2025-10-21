# 🔐 Configuración de Firebase Authentication

Esta guía te explica paso a paso cómo configurar Firebase Authentication para el proyecto KameHouse Laguna.

## 📋 Requisitos Previos

- Proyecto Firebase creado
- App Android registrada en Firebase
- Archivo `google-services.json` descargado y colocado en `/app/`

## 🚀 Paso 1: Habilitar Authentication

### 1.1 Acceder a Firebase Console
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `kamehouse-laguna`

### 1.2 Habilitar Authentication
1. En el menú lateral, haz clic en **"Authentication"**
2. Haz clic en **"Comenzar"** (Get started)
3. Se abrirá la página de Authentication

## 🔧 Paso 2: Configurar Métodos de Autenticación

### 2.1 Habilitar Email/Password
1. En la pestaña **"Sign-in method"**
2. Haz clic en **"Email/Password"**
3. Activa el primer toggle: **"Email/Password"**
4. **Opcional**: Activa el segundo toggle: **"Email link (passwordless sign-in)"**
5. Haz clic en **"Guardar"**

### 2.2 Configurar Dominios Autorizados (Opcional)
1. En la pestaña **"Settings"** (Configuración)
2. En la sección **"Authorized domains"**
3. Agrega tu dominio si planeas usar autenticación web
4. Por defecto ya incluye:
   - `localhost` (para desarrollo)
   - `your-project-id.firebaseapp.com`

## ⚙️ Paso 3: Configurar Templates de Email

### 3.1 Personalizar Email de Verificación
1. Ve a la pestaña **"Templates"**
2. Haz clic en **"Email address verification"**
3. Personaliza:
   - **Subject**: "Verifica tu cuenta de KameHouse Laguna"
   - **Body**: Personaliza el mensaje
   - **Action URL**: Deja por defecto
4. Haz clic en **"Guardar"**

### 3.2 Personalizar Email de Restablecimiento
1. En la misma pestaña **"Templates"**
2. Haz clic en **"Password reset"**
3. Personaliza:
   - **Subject**: "Restablece tu contraseña - KameHouse Laguna"
   - **Body**: Personaliza el mensaje
4. Haz clic en **"Guardar"**

## 🛡️ Paso 4: Configurar Reglas de Seguridad

### 4.1 Reglas Básicas (Desarrollo)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
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

### 4.2 Reglas para Producción
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
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

## 👤 Paso 5: Crear Usuario Administrador

### 5.1 Crear Usuario desde la Consola
1. Ve a la pestaña **"Users"**
2. Haz clic en **"Add user"**
3. Ingresa:
   - **Email**: `admin@kamehouselaguna.com`
   - **Password**: `Admin123456`
4. Haz clic en **"Add user"**

### 5.2 Configurar Rol de Administrador
1. Ve a **"Firestore Database"**
2. Crea una colección llamada `usuarios`
3. Crea un documento con el UID del usuario admin
4. Agrega los siguientes campos:
   ```json
   {
     "email": "admin@kamehouselaguna.com",
     "rol": "admin",
     "fechaCreacion": "2025-01-20T10:00:00Z",
     "ultimoAcceso": "2025-01-20T10:00:00Z"
   }
   ```

## 🔍 Paso 6: Verificar Configuración

### 6.1 Probar Registro
1. Ejecuta la app en Android Studio
2. Ve a la pantalla de login
3. Haz clic en **"Registrarse"**
4. Ingresa un email y contraseña válidos
5. Verifica que se cree el usuario en Authentication

### 6.2 Probar Login
1. Usa las credenciales del usuario creado
2. Verifica que el login funcione correctamente
3. Verifica que se redirija según el rol del usuario

## 📱 Paso 7: Configuración en la App

### 7.1 Verificar Dependencias
Asegúrate de que `app/build.gradle.kts` incluya:
```kotlin
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // ... otras dependencias
}
```

### 7.2 Verificar AndroidManifest.xml
Asegúrate de que incluya los permisos necesarios:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🚨 Solución de Problemas

### Error: "Email already in use"
- **Causa**: El email ya está registrado
- **Solución**: Usa un email diferente o restablece la contraseña

### Error: "Invalid email"
- **Causa**: Formato de email incorrecto
- **Solución**: Verifica que el email tenga formato válido

### Error: "Weak password"
- **Causa**: Contraseña muy simple
- **Solución**: Usa una contraseña de al menos 6 caracteres

### Error: "Network request failed"
- **Causa**: Sin conexión a internet
- **Solución**: Verifica la conexión a internet

### Error: "Too many requests"
- **Causa**: Demasiados intentos de login
- **Solución**: Espera unos minutos antes de intentar nuevamente

## 🔒 Mejores Prácticas

### Seguridad
1. **Habilita verificación de email** para usuarios nuevos
2. **Configura reglas de Firestore** apropiadas
3. **Usa contraseñas seguras** (mínimo 8 caracteres)
4. **Implementa rate limiting** en producción

### UX/UI
1. **Muestra mensajes de error claros** al usuario
2. **Incluye validación en tiempo real** en los formularios
3. **Proporciona feedback visual** durante las operaciones
4. **Permite recuperación de contraseña** fácil

### Monitoreo
1. **Revisa logs de Authentication** regularmente
2. **Monitorea intentos de login fallidos**
3. **Configura alertas** para actividades sospechosas
4. **Mantén estadísticas** de usuarios activos

## 📊 Métricas y Analytics

### Métricas Importantes
- **Usuarios registrados** por día/semana
- **Tasa de verificación de email**
- **Intentos de login fallidos**
- **Usuarios activos** mensuales

### Configurar Analytics
1. Ve a **"Analytics"** en Firebase Console
2. Habilita **"Google Analytics"**
3. Configura eventos personalizados para:
   - Registro exitoso
   - Login exitoso
   - Verificación de email
   - Recuperación de contraseña

---

## ✅ Checklist de Configuración

- [ ] Authentication habilitado
- [ ] Email/Password configurado
- [ ] Templates de email personalizados
- [ ] Usuario administrador creado
- [ ] Reglas de Firestore configuradas
- [ ] App probada con registro/login
- [ ] Manejo de errores implementado
- [ ] Analytics configurado (opcional)

**¡Firebase Authentication está listo para usar! 🎉**
