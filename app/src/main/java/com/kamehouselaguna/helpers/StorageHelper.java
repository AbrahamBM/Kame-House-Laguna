package com.kamehouselaguna.helpers;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Clase helper para operaciones con Firebase Storage
 * Maneja la subida y descarga de imágenes
 */
public class StorageHelper {
    private static final String TAG = "StorageHelper";
    private static final String FOLDER_FIGURAS = "figuras";

    private FirebaseStorage storage;
    private StorageReference storageRef;

    public StorageHelper() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * Sube una imagen de figura al Storage
     * @param imageUri URI de la imagen a subir
     * @param listener Callback para manejar el resultado
     */
    public void uploadFigureImage(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onError("URI de imagen no válida");
            return;
        }

        // Generar nombre único para la imagen
        String fileName = "figura_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(FOLDER_FIGURAS).child(fileName);

        // Subir la imagen
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Obtener la URL de descarga
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        Log.d(TAG, "Imagen subida exitosamente: " + downloadUri.toString());
                        listener.onSuccess(downloadUri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al obtener URL de descarga", e);
                        listener.onError("Error al obtener URL de descarga: " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error al subir imagen", e);
                listener.onError("Error al subir imagen: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina una imagen del Storage
     * @param imageUrl URL de la imagen a eliminar
     * @param listener Callback para manejar el resultado
     */
    public void deleteFigureImage(String imageUrl, OnImageDeleteListener listener) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            listener.onError("URL de imagen no válida");
            return;
        }

        try {
            // Crear referencia a la imagen usando la URL
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            
            // Eliminar la imagen
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Imagen eliminada exitosamente");
                    listener.onSuccess("Imagen eliminada exitosamente");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error al eliminar imagen", e);
                    listener.onError("Error al eliminar imagen: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al crear referencia de imagen", e);
            listener.onError("Error al crear referencia de imagen: " + e.getMessage());
        }
    }

    /**
     * Obtiene una referencia de Storage para una imagen
     * @param imageUrl URL de la imagen
     * @return StorageReference o null si hay error
     */
    public StorageReference getImageReference(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            return storage.getReferenceFromUrl(imageUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error al crear referencia de imagen", e);
            return null;
        }
    }

    /**
     * Verifica si una URL de imagen es válida
     * @param imageUrl URL a verificar
     * @return true si es válida, false en caso contrario
     */
    public boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null && 
               !imageUrl.isEmpty() && 
               (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"));
    }

    // ========== INTERFACES DE CALLBACK ==========

    public interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    public interface OnImageDeleteListener {
        void onSuccess(String message);
        void onError(String error);
    }
}
