package com.kamehouselaguna;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kamehouselaguna.adapters.FigureAdapter;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.helpers.NotificationHelper;
import com.kamehouselaguna.models.Figure;
import com.kamehouselaguna.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad principal que muestra el catálogo de figuras
 * Permite ver las figuras disponibles y acceder al login
 */
public class MainActivity extends AppCompatActivity implements FigureAdapter.OnFigureClickListener {
    
    private static final String TAG = "MainActivity";
    
    // Views
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewFiguras;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private FloatingActionButton fabLogin;
    
    // Adapters and helpers
    private FigureAdapter figureAdapter;
    private FirestoreHelper firestoreHelper;
    private NotificationHelper notificationHelper;
    
    // Data
    private List<Figure> figuras;
    private FirebaseUser currentUser;
    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeHelpers();
        setupRecyclerView();
        setupToolbar();
        setupFab();
        
        // Suscribirse a notificaciones push
        notificationHelper.subscribeToAllUsersTopic();
        
        // Cargar figuras
        loadFiguras();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        checkUserAuth();
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewFiguras = findViewById(R.id.recyclerViewFiguras);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        fabLogin = findViewById(R.id.fabLogin);
        
        figuras = new ArrayList<>();
    }
    
    /**
     * Inicializa los helpers
     */
    private void initializeHelpers() {
        firestoreHelper = new FirestoreHelper();
        notificationHelper = new NotificationHelper(this);
    }
    
    /**
     * Configura el RecyclerView
     */
    private void setupRecyclerView() {
        figureAdapter = new FigureAdapter(figuras, this);
        recyclerViewFiguras.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFiguras.setAdapter(figureAdapter);
    }
    
    /**
     * Configura la toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
    
    /**
     * Configura el FAB de login
     */
    private void setupFab() {
        fabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    
    /**
     * Verifica la autenticación del usuario
     */
    private void checkUserAuth() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Usuario autenticado, verificar rol
            firestoreHelper.getUser(currentUser.getUid(), new FirestoreHelper.OnUserLoadedListener() {
                @Override
                public void onUserLoaded(User user) {
                    userData = user;
                    if (user.isAdmin()) {
                        // Redirigir a panel de administrador
                        Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    // Si es usuario normal, continuar en MainActivity
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, "Error al cargar usuario: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                figureAdapter.updateFiguras(figuras);
                
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
                Toast.makeText(MainActivity.this, "Error al cargar figuras: " + error, Toast.LENGTH_LONG).show();
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
    public void onFigureClick(Figure figura) {
        Intent intent = new Intent(this, FigureDetailActivity.class);
        intent.putExtra("figura_id", figura.getId());
        startActivity(intent);
    }
}
