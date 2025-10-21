package com.kamehouselaguna;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.models.User;

/**
 * Actividad de inicio de sesión y registro
 * Permite autenticación con email/contraseña y registro de nuevos usuarios
 */
public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    // Views
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private MaterialButton buttonRegister;
    private MaterialButton buttonGuest;
    private ProgressBar progressBar;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initializeViews();
        initializeFirebase();
        setupButtons();
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGuest = findViewById(R.id.buttonGuest);
        progressBar = findViewById(R.id.progressBar);
    }
    
    /**
     * Inicializa Firebase
     */
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreHelper = new FirestoreHelper();
    }
    
    /**
     * Configura los botones
     */
    private void setupButtons() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        
        buttonGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueAsGuest();
            }
        });
    }
    
    /**
     * Inicia sesión con email y contraseña
     */
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        if (!validateInputs(email, password)) {
            return;
        }
        
        showLoading(true);
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Crear o actualizar usuario en Firestore
                                createOrUpdateUser(user);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, 
                                    "Error al iniciar sesión: " + task.getException().getMessage(), 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    /**
     * Registra un nuevo usuario
     */
    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        if (!validateInputs(email, password)) {
            return;
        }
        
        if (password.length() < 6) {
            textInputLayoutPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        
        // Validaciones adicionales para registro
        if (!isValidEmail(email)) {
            textInputLayoutEmail.setError("Formato de email no válido");
            return;
        }
        
        if (!isValidPassword(password)) {
            textInputLayoutPassword.setError("La contraseña debe contener al menos 6 caracteres");
            return;
        }
        
        showLoading(true);
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Enviar email de verificación
                                sendEmailVerification(user);
                                
                                // Crear usuario en Firestore con rol "usuario"
                                User newUser = new User(user.getUid(), user.getEmail(), "usuario");
                                firestoreHelper.saveUser(newUser, new FirestoreHelper.OnOperationCompleteListener() {
                                    @Override
                                    public void onSuccess(String message) {
                                        Toast.makeText(LoginActivity.this, 
                                                "Usuario registrado exitosamente. Revisa tu email para verificar la cuenta.", 
                                                Toast.LENGTH_LONG).show();
                                        navigateToMainActivity();
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(LoginActivity.this, "Error al crear usuario: " + error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            Toast.makeText(LoginActivity.this, "Error al registrar usuario: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    /**
     * Continúa como invitado
     */
    private void continueAsGuest() {
        navigateToMainActivity();
    }
    
    /**
     * Crea o actualiza el usuario en Firestore
     */
    private void createOrUpdateUser(FirebaseUser firebaseUser) {
        firestoreHelper.getUser(firebaseUser.getUid(), new FirestoreHelper.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                // Usuario existe, actualizar último acceso
                user.setUltimoAcceso(new java.util.Date());
                firestoreHelper.saveUser(user, new FirestoreHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess(String message) {
                        navigateToMainActivity();
                    }
                    
                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, "Error al actualizar usuario: " + error, Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                // Usuario no existe, crear nuevo con rol "usuario"
                User newUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), "usuario");
                firestoreHelper.saveUser(newUser, new FirestoreHelper.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess(String message) {
                        navigateToMainActivity();
                    }
                    
                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, "Error al crear usuario: " + error, Toast.LENGTH_LONG).show();
                        navigateToMainActivity();
                    }
                });
            }
        });
    }
    
    /**
     * Valida los inputs del formulario
     */
    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        
        // Limpiar errores anteriores
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);
        
        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError("El email es requerido");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Email no válido");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setError("La contraseña es requerida");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Navega a la actividad principal
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Envía email de verificación al usuario
     */
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email de verificación enviado");
                        } else {
                            Log.e(TAG, "Error al enviar email de verificación", task.getException());
                        }
                    }
                });
    }
    
    /**
     * Valida formato de email
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Valida formato de contraseña
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }
    
    /**
     * Obtiene mensaje de error amigable
     */
    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "Error desconocido";
        }
        
        String errorCode = exception.getMessage();
        if (errorCode == null) {
            return "Error desconocido";
        }
        
        if (errorCode.contains("email-already-in-use")) {
            return "Este email ya está registrado";
        } else if (errorCode.contains("invalid-email")) {
            return "Email no válido";
        } else if (errorCode.contains("weak-password")) {
            return "La contraseña es muy débil";
        } else if (errorCode.contains("user-not-found")) {
            return "Usuario no encontrado";
        } else if (errorCode.contains("wrong-password")) {
            return "Contraseña incorrecta";
        } else if (errorCode.contains("user-disabled")) {
            return "Usuario deshabilitado";
        } else if (errorCode.contains("too-many-requests")) {
            return "Demasiados intentos. Intenta más tarde";
        } else if (errorCode.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet";
        } else {
            return errorCode;
        }
    }
    
    /**
     * Muestra/oculta el estado de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
        buttonRegister.setEnabled(!show);
        buttonGuest.setEnabled(!show);
    }
}
