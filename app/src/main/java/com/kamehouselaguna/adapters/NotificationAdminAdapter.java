package com.kamehouselaguna.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.kamehouselaguna.R;
import com.kamehouselaguna.models.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView de notificaciones en el panel de administrador
 * Incluye botón para eliminar notificaciones
 */
public class NotificationAdminAdapter extends RecyclerView.Adapter<NotificationAdminAdapter.NotificationAdminViewHolder> {
    private List<Notification> notificaciones;
    private OnNotificationAdminClickListener listener;
    private SimpleDateFormat dateFormat;

    public NotificationAdminAdapter(List<Notification> notificaciones, OnNotificationAdminClickListener listener) {
        this.notificaciones = notificaciones;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "ES"));
    }

    @NonNull
    @Override
    public NotificationAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_admin, parent, false);
        return new NotificationAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdminViewHolder holder, int position) {
        Notification notificacion = notificaciones.get(position);
        holder.bind(notificacion);
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    /**
     * Actualiza la lista de notificaciones
     * @param newNotificaciones Nueva lista de notificaciones
     */
    public void updateNotificaciones(List<Notification> newNotificaciones) {
        this.notificaciones = newNotificaciones;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para cada elemento de la lista
     */
    public class NotificationAdminViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitulo;
        private TextView textViewMensaje;
        private TextView textViewFecha;
        private MaterialButton buttonDelete;

        public NotificationAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.textViewTitulo);
            textViewMensaje = itemView.findViewById(R.id.textViewMensaje);
            textViewFecha = itemView.findViewById(R.id.textViewFecha);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // Configurar click listener
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteClick(notificaciones.get(position));
                    }
                }
            });
        }

        public void bind(Notification notificacion) {
            // Configurar textos
            textViewTitulo.setText(notificacion.getTitulo());
            textViewMensaje.setText(notificacion.getMensaje());
            
            // Formatear fecha
            if (notificacion.getFecha() != null) {
                textViewFecha.setText(dateFormat.format(notificacion.getFecha()));
            } else {
                textViewFecha.setText("Fecha no disponible");
            }
        }
    }

    /**
     * Interfaz para manejar clicks en las notificaciones del administrador
     */
    public interface OnNotificationAdminClickListener {
        void onDeleteClick(Notification notificacion);
    }
}
