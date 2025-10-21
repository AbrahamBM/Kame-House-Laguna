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
import com.kamehouselaguna.R;
import com.kamehouselaguna.models.Figure;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView de figuras
 * Maneja la visualización de la lista de figuras en el catálogo
 */
public class FigureAdapter extends RecyclerView.Adapter<FigureAdapter.FigureViewHolder> {
    private List<Figure> figuras;
    private OnFigureClickListener listener;
    private NumberFormat currencyFormat;

    public FigureAdapter(List<Figure> figuras, OnFigureClickListener listener) {
        this.figuras = figuras;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public FigureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_figure, parent, false);
        return new FigureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FigureViewHolder holder, int position) {
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
    public class FigureViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewNombre;
        private TextView textViewPrecio;
        private TextView textViewDescripcion;

        public FigureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewFigure);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);

            // Configurar click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onFigureClick(figuras.get(position));
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
     * Interfaz para manejar clicks en las figuras
     */
    public interface OnFigureClickListener {
        void onFigureClick(Figure figura);
    }
}
