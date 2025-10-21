package com.kamehouselaguna.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.kamehouselaguna.R;
import com.kamehouselaguna.models.Figure;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView de figuras en el panel de administrador
 * Incluye botones para editar y eliminar figuras
 */
public class FigureAdminAdapter extends RecyclerView.Adapter<FigureAdminAdapter.FigureAdminViewHolder> {
    private List<Figure> figuras;
    private OnFigureAdminClickListener listener;
    private NumberFormat currencyFormat;

    public FigureAdminAdapter(List<Figure> figuras, OnFigureAdminClickListener listener) {
        this.figuras = figuras;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public FigureAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_figure_admin, parent, false);
        return new FigureAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FigureAdminViewHolder holder, int position) {
        Figure figura = figuras.get(position);
        holder.bind(figura);
    }

    @Override
    public int getItemCount() {
        return figuras.size();
    }

    /**
     * Actualiza la lista de figuras
     * @param newFiguras Nueva lista de figuras
     */
    public void updateFiguras(List<Figure> newFiguras) {
        this.figuras = newFiguras;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para cada elemento de la lista
     */
    public class FigureAdminViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewNombre;
        private TextView textViewPrecio;
        private TextView textViewDescripcion;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public FigureAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewFigure);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // Configurar click listeners
            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onEditClick(figuras.get(position));
                    }
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteClick(figuras.get(position));
                    }
                }
            });
        }

        public void bind(Figure figura) {
            // Cargar imagen con Glide
            if (figura.getImagenUrl() != null && !figura.getImagenUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(figura.getImagenUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Configurar textos
            textViewNombre.setText(figura.getNombre());
            textViewPrecio.setText(currencyFormat.format(figura.getPrecio()));
            
            // Truncar descripción si es muy larga
            String descripcion = figura.getDescripcion();
            if (descripcion != null && descripcion.length() > 100) {
                descripcion = descripcion.substring(0, 100) + "...";
            }
            textViewDescripcion.setText(descripcion);
        }
    }

    /**
     * Interfaz para manejar clicks en las figuras del administrador
     */
    public interface OnFigureAdminClickListener {
        void onEditClick(Figure figura);
        void onDeleteClick(Figure figura);
    }
}
