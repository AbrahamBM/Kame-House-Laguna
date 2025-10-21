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
import com.kamehouselaguna.adapters.FigureAdminAdapter;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.models.Figure;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para mostrar y gestionar las figuras en el panel de administrador
 */
public class FigurasAdminFragment extends Fragment implements FigureAdminAdapter.OnFigureAdminClickListener {
    
    private static final String TAG = "FigurasAdminFragment";
    
    // Views
    private RecyclerView recyclerViewFiguras;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    
    // Adapter and helper
    private FigureAdminAdapter figureAdminAdapter;
    private FirestoreHelper firestoreHelper;
    
    // Data
    private List<Figure> figuras;
    private OnFigureActionListener listener;

    public interface OnFigureActionListener {
        void onEditFigure(Figure figura);
        void onDeleteFigure(Figure figura);
    }

    public void setOnFigureActionListener(OnFigureActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_figuras_admin, container, false);
        
        initializeViews(view);
        initializeHelpers();
        setupRecyclerView();
        loadFiguras();
        
        return view;
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews(View view) {
        recyclerViewFiguras = view.findViewById(R.id.recyclerViewFiguras);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        
        figuras = new ArrayList<>();
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
        figureAdminAdapter = new FigureAdminAdapter(figuras, this);
        recyclerViewFiguras.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFiguras.setAdapter(figureAdminAdapter);
    }
    
    /**
     * Carga las figuras desde Firestore
     */
    private void loadFiguras() {
        showLoading(true);
        
        firestoreHelper.getFigurasRealtime(new FirestoreHelper.OnFigurasLoadedListener() {
            @Override
            public void onFigurasLoaded(List<Figure> figurasList) {
                showLoading(false);
                figuras.clear();
                figuras.addAll(figurasList);
                figureAdminAdapter.updateFiguras(figuras);
                
                if (figuras.isEmpty()) {
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
        recyclerViewFiguras.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Muestra/oculta el estado vacío
     */
    private void showEmptyState(boolean show) {
        textViewEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerViewFiguras.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onEditClick(Figure figura) {
        if (listener != null) {
            listener.onEditFigure(figura);
        }
    }
    
    @Override
    public void onDeleteClick(Figure figura) {
        if (listener != null) {
            listener.onDeleteFigure(figura);
        }
    }
}
