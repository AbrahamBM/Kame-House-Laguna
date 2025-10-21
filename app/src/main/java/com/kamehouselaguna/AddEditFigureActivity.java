package com.kamehouselaguna;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.helpers.StorageHelper;
import com.kamehouselaguna.models.Figure;

/**
 * Actividad para agregar o editar figuras
 * Permite subir imágenes, ingresar datos y guardar en Firestore
 */
public class AddEditFigureActivity extends AppCompatActivity {
    
    private static final String TAG = "AddEditFigureActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    
    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewFigure;
    private MaterialButton buttonSelectImage;
    private TextInputLayout textInputLayoutNombre;
    private TextInputLayout textInputLayoutPrecio;
    private TextInputLayout textInputLayoutDescripcion;
    private TextInputEditText editTextNombre;
    private TextInputEditText editTextPrecio;
    private TextInputEditText editTextDescripcion;
    private MaterialButton buttonSave;
    private MaterialButton buttonDelete;
    private ProgressBar progressBar;
    
    // Helpers
    private FirestoreHelper firestoreHelper;
    private StorageHelper storageHelper;
    
    // Data
    private String figuraId;
    private Figure figura;
    private Uri imageUri;
    private String imageUrl;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_figure);
        
        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupButtons();
        
        // Verificar si es modo edición
        figuraId = getIntent().getStringExtra("figura_id");
        if (figuraId != null) {
            isEditMode = true;
            loadFigura();
        } else {
            updateUI();
        }
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewFigure = findViewById(R.id.imageViewFigure);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        textInputLayoutNombre = findViewById(R.id.textInputLayoutNombre);
        textInputLayoutPrecio = findViewById(R.id.textInputLayoutPrecio);
        textInputLayoutDescripcion = findViewById(R.id.textInputLayoutDescripcion);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextPrecio = findViewById(R.id.editTextPrecio);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        progressBar = findViewById(R.id.progressBar);
    }
    
    /**
     * Inicializa los helpers
     */
    private void initializeHelpers() {
        firestoreHelper = new FirestoreHelper();
        storageHelper = new StorageHelper();
    }
    
    /**
     * Configura la toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    /**
     * Configura los botones
     */
    private void setupButtons() {
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFigura();
            }
        });
        
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFigura();
            }
        });
    }
    
    /**
     * Carga una figura existente para editar
     */
    private void loadFigura() {
        showLoading(true);
        
        firestoreHelper.getFigura(figuraId, new FirestoreHelper.OnFiguraLoadedListener() {
            @Override
            public void onFiguraLoaded(Figure figuraLoaded) {
                showLoading(false);
                figura = figuraLoaded;
                imageUrl = figura.getImagenUrl();
                updateUI();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AddEditFigureActivity.this, "Error al cargar figura: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Actualiza la interfaz según el modo (agregar/editar)
     */
    private void updateUI() {
        if (isEditMode) {
            // Modo edición
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Figura");
            }
            buttonSave.setText("Actualizar Figura");
            buttonDelete.setVisibility(View.VISIBLE);
            
            if (figura != null) {
                // Cargar datos existentes
                editTextNombre.setText(figura.getNombre());
                editTextPrecio.setText(String.valueOf(figura.getPrecio()));
                editTextDescripcion.setText(figura.getDescripcion());
                
                // Cargar imagen existente
                if (figura.getImagenUrl() != null && !figura.getImagenUrl().isEmpty()) {
                    Glide.with(this)
                            .load(figura.getImagenUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(imageViewFigure);
                }
            }
        } else {
            // Modo agregar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Agregar Figura");
            }
            buttonSave.setText("Guardar Figura");
            buttonDelete.setVisibility(View.GONE);
        }
    }
    
    /**
     * Abre el selector de imágenes
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            
            // Mostrar imagen seleccionada
            Glide.with(this)
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageViewFigure);
        }
    }
    
    /**
     * Guarda la figura en Firestore
     */
    private void saveFigura() {
        if (!validateInputs()) {
            return;
        }
        
        showLoading(true);
        
        if (imageUri != null) {
            // Subir imagen primero
            uploadImageAndSaveFigura();
        } else if (isEditMode && imageUrl != null) {
            // Usar imagen existente
            saveFiguraToFirestore(imageUrl);
        } else {
            // Sin imagen
            saveFiguraToFirestore(null);
        }
    }
    
    /**
     * Sube la imagen y luego guarda la figura
     */
    private void uploadImageAndSaveFigura() {
        storageHelper.uploadFigureImage(imageUri, new StorageHelper.OnImageUploadListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                imageUrl = downloadUrl;
                saveFiguraToFirestore(downloadUrl);
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AddEditFigureActivity.this, "Error al subir imagen: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Guarda la figura en Firestore
     */
    private void saveFiguraToFirestore(String imageUrl) {
        String nombre = editTextNombre.getText().toString().trim();
        double precio = Double.parseDouble(editTextPrecio.getText().toString().trim());
        String descripcion = editTextDescripcion.getText().toString().trim();
        
        Figure figuraToSave = new Figure(nombre, descripcion, imageUrl, precio);
        
        if (isEditMode) {
            // Actualizar figura existente
            firestoreHelper.updateFigura(figuraId, figuraToSave, new FirestoreHelper.OnOperationCompleteListener() {
                @Override
                public void onSuccess(String message) {
                    showLoading(false);
                    Toast.makeText(AddEditFigureActivity.this, "Figura actualizada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                }
                
                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(AddEditFigureActivity.this, "Error al actualizar figura: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Crear nueva figura
            firestoreHelper.addFigura(figuraToSave, new FirestoreHelper.OnOperationCompleteListener() {
                @Override
                public void onSuccess(String message) {
                    showLoading(false);
                    Toast.makeText(AddEditFigureActivity.this, "Figura agregada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                }
                
                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(AddEditFigureActivity.this, "Error al agregar figura: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    /**
     * Elimina la figura
     */
    private void deleteFigura() {
        if (!isEditMode || figura == null) {
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Figura")
                .setMessage("¿Estás seguro de que quieres eliminar la figura \"" + figura.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    showLoading(true);
                    
                    // Eliminar imagen del Storage si existe
                    if (figura.getImagenUrl() != null && !figura.getImagenUrl().isEmpty()) {
                        storageHelper.deleteFigureImage(figura.getImagenUrl(), new StorageHelper.OnImageDeleteListener() {
                            @Override
                            public void onSuccess(String message) {
                                // Eliminar figura de Firestore
                                deleteFiguraFromFirestore();
                            }
                            
                            @Override
                            public void onError(String error) {
                                // Continuar con la eliminación de Firestore aunque falle la imagen
                                deleteFiguraFromFirestore();
                            }
                        });
                    } else {
                        // Eliminar figura de Firestore directamente
                        deleteFiguraFromFirestore();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    /**
     * Elimina la figura de Firestore
     */
    private void deleteFiguraFromFirestore() {
        firestoreHelper.deleteFigura(figuraId, new FirestoreHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                Toast.makeText(AddEditFigureActivity.this, "Figura eliminada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AddEditFigureActivity.this, "Error al eliminar figura: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Valida los inputs del formulario
     */
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Limpiar errores anteriores
        textInputLayoutNombre.setError(null);
        textInputLayoutPrecio.setError(null);
        textInputLayoutDescripcion.setError(null);
        
        String nombre = editTextNombre.getText().toString().trim();
        String precioStr = editTextPrecio.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        
        if (TextUtils.isEmpty(nombre)) {
            textInputLayoutNombre.setError("El nombre es requerido");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(precioStr)) {
            textInputLayoutPrecio.setError("El precio es requerido");
            isValid = false;
        } else {
            try {
                double precio = Double.parseDouble(precioStr);
                if (precio <= 0) {
                    textInputLayoutPrecio.setError("El precio debe ser mayor a 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                textInputLayoutPrecio.setError("Precio no válido");
                isValid = false;
            }
        }
        
        if (TextUtils.isEmpty(descripcion)) {
            textInputLayoutDescripcion.setError("La descripción es requerida");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Muestra/oculta el estado de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!show);
        buttonDelete.setEnabled(!show);
        buttonSelectImage.setEnabled(!show);
    }
}
