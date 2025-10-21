package com.kamehouselaguna.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.kamehouselaguna.models.Figure;
import com.kamehouselaguna.models.Notification;
import com.kamehouselaguna.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase helper para operaciones con Firestore
 * Maneja todas las operaciones CRUD para figuras, notificaciones y usuarios
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static final String COLLECTION_FIGURAS = "figuras";
    private static final String COLLECTION_NOTIFICACIONES = "notificaciones";
    private static final String COLLECTION_USUARIOS = "usuarios";

    private FirebaseFirestore db;
    private CollectionReference figurasRef;
    private CollectionReference notificacionesRef;
    private CollectionReference usuariosRef;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        figurasRef = db.collection(COLLECTION_FIGURAS);
        notificacionesRef = db.collection(COLLECTION_NOTIFICACIONES);
        usuariosRef = db.collection(COLLECTION_USUARIOS);
    }

    // ========== OPERACIONES CON FIGURAS ==========

    /**
     * Obtiene todas las figuras en tiempo real
     */
    public void getFigurasRealtime(OnFigurasLoadedListener listener) {
        figurasRef.orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al obtener figuras", error);
                        listener.onError(error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Figure> figuras = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Figure figura = doc.toObject(Figure.class);
                            if (figura != null) {
                                figura.setId(doc.getId());
                                figuras.add(figura);
                            }
                        }
                        listener.onFigurasLoaded(figuras);
                    }
                });
    }

    /**
     * Obtiene una figura por ID
     */
    public void getFigura(String id, OnFiguraLoadedListener listener) {
        figurasRef.document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Figure figura = documentSnapshot.toObject(Figure.class);
                        if (figura != null) {
                            figura.setId(documentSnapshot.getId());
                            listener.onFiguraLoaded(figura);
                        } else {
                            listener.onError("Error al convertir documento a figura");
                        }
                    } else {
                        listener.onError("Figura no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener figura", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Agrega una nueva figura
     */
    public void addFigura(Figure figura, OnOperationCompleteListener listener) {
        figura.setFechaCreacion(new Date());
        figura.setFechaActualizacion(new Date());

        figurasRef.add(figura)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Figura agregada con ID: " + documentReference.getId());
                    listener.onSuccess("Figura agregada exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al agregar figura", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Actualiza una figura existente
     */
    public void updateFigura(String id, Figure figura, OnOperationCompleteListener listener) {
        figura.setFechaActualizacion(new Date());

        figurasRef.document(id).set(figura)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Figura actualizada exitosamente");
                    listener.onSuccess("Figura actualizada exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar figura", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Elimina una figura
     */
    public void deleteFigura(String id, OnOperationCompleteListener listener) {
        figurasRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Figura eliminada exitosamente");
                    listener.onSuccess("Figura eliminada exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar figura", e);
                    listener.onError(e.getMessage());
                });
    }

    // ========== OPERACIONES CON NOTIFICACIONES ==========

    /**
     * Obtiene todas las notificaciones en tiempo real
     */
    public void getNotificacionesRealtime(OnNotificacionesLoadedListener listener) {
        notificacionesRef.orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al obtener notificaciones", error);
                        listener.onError(error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Notification> notificaciones = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Notification notificacion = doc.toObject(Notification.class);
                            if (notificacion != null) {
                                notificacion.setId(doc.getId());
                                notificaciones.add(notificacion);
                            }
                        }
                        listener.onNotificacionesLoaded(notificaciones);
                    }
                });
    }

    /**
     * Agrega una nueva notificación
     */
    public void addNotificacion(Notification notificacion, OnOperationCompleteListener listener) {
        notificacion.setFecha(new Date());

        notificacionesRef.add(notificacion)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notificación agregada con ID: " + documentReference.getId());
                    listener.onSuccess("Notificación agregada exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al agregar notificación", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Elimina una notificación
     */
    public void deleteNotificacion(String id, OnOperationCompleteListener listener) {
        notificacionesRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación eliminada exitosamente");
                    listener.onSuccess("Notificación eliminada exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar notificación", e);
                    listener.onError(e.getMessage());
                });
    }

    // ========== OPERACIONES CON USUARIOS ==========

    /**
     * Obtiene un usuario por UID
     */
    public void getUser(String uid, OnUserLoadedListener listener) {
        usuariosRef.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setUid(documentSnapshot.getId());
                            listener.onUserLoaded(user);
                        } else {
                            listener.onError("Error al convertir documento a usuario");
                        }
                    } else {
                        listener.onError("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener usuario", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Crea o actualiza un usuario
     */
    public void saveUser(User user, OnOperationCompleteListener listener) {
        user.setUltimoAcceso(new Date());

        usuariosRef.document(user.getUid()).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado exitosamente");
                    listener.onSuccess("Usuario guardado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar usuario", e);
                    listener.onError(e.getMessage());
                });
    }

    // ========== INTERFACES DE CALLBACK ==========

    public interface OnFigurasLoadedListener {
        void onFigurasLoaded(List<Figure> figuras);
        void onError(String error);
    }

    public interface OnFiguraLoadedListener {
        void onFiguraLoaded(Figure figura);
        void onError(String error);
    }

    public interface OnNotificacionesLoadedListener {
        void onNotificacionesLoaded(List<Notification> notificaciones);
        void onError(String error);
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }
}
