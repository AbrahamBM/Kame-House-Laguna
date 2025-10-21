package com.kamehouselaguna;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.models.Figure;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Actividad para mostrar los detalles de una figura
 * Permite ver la imagen, descripción y compartir la figura
 */
public class FigureDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "FigureDetailActivity";
    
    // Views
    private MaterialToolbar toolbar;
    private ImageView imageViewFigure;
    private TextView textViewNombre;
    private TextView textViewPrecio;
    private TextView textViewDescripcion;
    private MaterialButton buttonCompartir;
    private ProgressBar progressBar;
    
    // Helpers
    private FirestoreHelper firestoreHelper;
    
    // Data
    private Figure figura;
    private String figuraId;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_figure_detail);
        
        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupButton();
        
        // Obtener ID de la figura
        figuraId = getIntent().getStringExtra("figura_id");
        if (figuraId == null) {
            Toast.makeText(this, "Error: ID de figura no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Cargar figura
        loadFigura();
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewFigure = findViewById(R.id.imageViewFigure);
        textViewNombre = findViewById(R.id.textViewNombre);
        textViewPrecio = findViewById(R.id.textViewPrecio);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        buttonCompartir = findViewById(R.id.buttonCompartir);
        progressBar = findViewById(R.id.progressBar);
        
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }
    
    /**
     * Inicializa los helpers
     */
    private void initializeHelpers() {
        firestoreHelper = new FirestoreHelper();
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
     * Configura el botón de compartir
     */
    private void setupButton() {
        buttonCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartirFigura();
            }
        });
    }
    
    /**
     * Carga la figura desde Firestore
     */
    private void loadFigura() {
        showLoading(true);
        
        firestoreHelper.getFigura(figuraId, new FirestoreHelper.OnFiguraLoadedListener() {
            @Override
            public void onFiguraLoaded(Figure figuraLoaded) {
                showLoading(false);
                figura = figuraLoaded;
                updateUI();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(FigureDetailActivity.this, "Error al cargar figura: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Actualiza la interfaz con los datos de la figura
     */
    private void updateUI() {
        if (figura == null) return;
        
        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(figura.getNombre());
        }
        
        // Cargar imagen
        if (figura.getImagenUrl() != null && !figura.getImagenUrl().isEmpty()) {
            Glide.with(this)
                    .load(figura.getImagenUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageViewFigure);
        } else {
            imageViewFigure.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // Configurar textos
        textViewNombre.setText(figura.getNombre());
        textViewPrecio.setText(currencyFormat.format(figura.getPrecio()));
        textViewDescripcion.setText(figura.getDescripcion());
    }
    
    /**
     * Comparte la información de la figura
     */
    private void compartirFigura() {
        if (figura == null) return;
        
        String shareText = "¡Mira esta increíble figura de acción!\n\n" +
                "Nombre: " + figura.getNombre() + "\n" +
                "Precio: " + currencyFormat.format(figura.getPrecio()) + "\n" +
                "Descripción: " + figura.getDescripcion() + "\n\n" +
                "Descárgate la app KameHouse Laguna para ver más figuras.";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Figura: " + figura.getNombre());
        
        startActivity(Intent.createChooser(shareIntent, "Compartir figura"));
    }
    
    /**
     * Muestra/oculta el estado de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
