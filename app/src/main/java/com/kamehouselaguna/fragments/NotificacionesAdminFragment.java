package com.kamehouselaguna.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kamehouselaguna.R;
import com.kamehouselaguna.adapters.NotificationAdminAdapter;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.models.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para mostrar y gestionar las notificaciones en el panel de administrador
 */
public class NotificacionesAdminFragment extends Fragment implements NotificationAdminAdapter.OnNotificationAdminClickListener {
    
    private static final String TAG = "NotificacionesAdminFragment";
    
    // Views
    private RecyclerView recyclerViewNotificaciones;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    
    // Adapter and helper
    private NotificationAdminAdapter notificationAdminAdapter;
    private FirestoreHelper firestoreHelper;
    
    // Data
    private List<Notification> notificaciones;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onDeleteNotification(Notification notificacion);
    }

    public void setOnNotificationActionListener(OnNotificationActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificaciones_admin, container, false);
        
        initializeViews(view);
        initializeHelpers();
        setupRecyclerView();
        loadNotificaciones();
        
        return view;
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews(View view) {
        recyclerViewNotificaciones = view.findViewById(R.id.recyclerViewNotificaciones);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        
        notificaciones = new ArrayList<>();
    }
    
    /**
     * Inicializa los helpers
     */
    private void initializeHelpers() {
        firestoreHelper = new FirestoreHelper();
    }
    
    /**
     * Configura el RecyclerView
     */
    private void setupRecyclerView() {
        notificationAdminAdapter = new NotificationAdminAdapter(notificaciones, this);
        recyclerViewNotificaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotificaciones.setAdapter(notificationAdminAdapter);
    }
    
    /**
     * Carga las notificaciones desde Firestore
     */
    private void loadNotificaciones() {
        showLoading(true);
        
        firestoreHelper.getNotificacionesRealtime(new FirestoreHelper.OnNotificacionesLoadedListener() {
            @Override
            public void onNotificacionesLoaded(List<Notification> notificacionesList) {
                showLoading(false);
                notificaciones.clear();
                notificaciones.addAll(notificacionesList);
                notificationAdminAdapter.updateNotificaciones(notificaciones);
                
                if (notificaciones.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                showEmptyState(true);
            }
        });
    }
    
    /**
     * Muestra/oculta el estado de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewNotificaciones.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Muestra/oculta el estado vacío
     */
    private void showEmptyState(boolean show) {
        textViewEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerViewNotificaciones.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onDeleteClick(Notification notificacion) {
        if (listener != null) {
            listener.onDeleteNotification(notificacion);
        }
    }
}
