package com.kamehouselaguna;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kamehouselaguna.fragments.FigurasAdminFragment;
import com.kamehouselaguna.fragments.NotificacionesAdminFragment;
import com.kamehouselaguna.helpers.FirestoreHelper;
import com.kamehouselaguna.helpers.NotificationHelper;
import com.kamehouselaguna.models.Figure;
import com.kamehouselaguna.models.Notification;
import com.kamehouselaguna.models.User;

/**
 * Actividad del panel de administrador
 * Permite gestionar figuras y notificaciones
 */
public class AdminPanelActivity extends AppCompatActivity implements 
        FigurasAdminFragment.OnFigureActionListener,
        NotificacionesAdminFragment.OnNotificationActionListener {
    
    private static final String TAG = "AdminPanelActivity";
    
    // Views
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAdd;
    
    // Fragments
    private FigurasAdminFragment figurasFragment;
    private NotificacionesAdminFragment notificacionesFragment;
    
    // Helpers
    private FirestoreHelper firestoreHelper;
    private NotificationHelper notificationHelper;
    
    // Data
    private FirebaseUser currentUser;
    private User userData;
    private int currentTab = 0; // 0 = Figuras, 1 = Notificaciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        
        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupViewPager();
        setupFab();
        
        // Verificar autenticación y rol
        checkUserAuth();
    }
    
    /**
     * Inicializa las vistas
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabAdd = findViewById(R.id.fabAdd);
    }
    
    /**
     * Inicializa los helpers
     */
    private void initializeHelpers() {
        firestoreHelper = new FirestoreHelper();
        notificationHelper = new NotificationHelper(this);
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
     * Configura el ViewPager con los fragments
     */
    private void setupViewPager() {
        figurasFragment = new FigurasAdminFragment();
        notificacionesFragment = new NotificacionesAdminFragment();
        
        // Configurar listeners
        figurasFragment.setOnFigureActionListener(this);
        notificacionesFragment.setOnNotificationActionListener(this);
        
        // Crear adapter
        AdminPagerAdapter adapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Figuras");
                        break;
                    case 1:
                        tab.setText("Notificaciones");
                        break;
                }
            }
        }).attach();
        
        // Listener para cambiar el FAB según la pestaña
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentTab = position;
                updateFabVisibility();
            }
        });
    }
    
    /**
     * Configura el FAB
     */
    private void setupFab() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab == 0) {
                    // Agregar nueva figura
                    Intent intent = new Intent(AdminPanelActivity.this, AddEditFigureActivity.class);
                    startActivity(intent);
                } else if (currentTab == 1) {
                    // Agregar nueva notificación
                    showAddNotificationDialog();
                }
            }
        });
    }
    
    /**
     * Actualiza la visibilidad del FAB según la pestaña activa
     */
    private void updateFabVisibility() {
        fabAdd.setVisibility(View.VISIBLE);
        if (currentTab == 0) {
            fabAdd.setImageResource(R.drawable.ic_add);
        } else if (currentTab == 1) {
            fabAdd.setImageResource(R.drawable.ic_add);
        }
    }
    
    /**
     * Verifica la autenticación del usuario
     */
    private void checkUserAuth() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Verificar rol de administrador
        firestoreHelper.getUser(currentUser.getUid(), new FirestoreHelper.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                userData = user;
                if (!user.isAdmin()) {
                    Toast.makeText(AdminPanelActivity.this, "Acceso denegado. Se requiere rol de administrador.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(AdminPanelActivity.this, "Error al verificar usuario: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Muestra el diálogo para agregar una nueva notificación
     */
    private void showAddNotificationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_notification, null);
        
        com.google.android.material.textfield.TextInputEditText editTextTitulo = 
                dialogView.findViewById(R.id.editTextTitulo);
        com.google.android.material.textfield.TextInputEditText editTextMensaje = 
                dialogView.findViewById(R.id.editTextMensaje);
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Nueva Notificación")
                .setPositiveButton("Enviar", null)
                .setNegativeButton("Cancelar", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Configurar botón positivo
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = editTextTitulo.getText().toString().trim();
                String mensaje = editTextMensaje.getText().toString().trim();
                
                if (titulo.isEmpty() || mensaje.isEmpty()) {
                    Toast.makeText(AdminPanelActivity.this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Crear y enviar notificación
                Notification notificacion = new Notification(titulo, mensaje, currentUser.getUid(), currentUser.getEmail());
                sendNotification(notificacion);
                dialog.dismiss();
            }
        });
    }
    
    /**
     * Envía una notificación push
     */
    private void sendNotification(Notification notificacion) {
        // Guardar en Firestore
        firestoreHelper.addNotificacion(notificacion, new FirestoreHelper.OnOperationCompleteListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AdminPanelActivity.this, "Notificación enviada exitosamente", Toast.LENGTH_SHORT).show();
                
                // Aquí podrías integrar con un servicio backend para enviar FCM
                // Por ahora solo se guarda en Firestore
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(AdminPanelActivity.this, "Error al enviar notificación: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    // ========== IMPLEMENTACIÓN DE INTERFACES ==========
    
    @Override
    public void onEditFigure(Figure figura) {
        Intent intent = new Intent(this, AddEditFigureActivity.class);
        intent.putExtra("figura_id", figura.getId());
        startActivity(intent);
    }
    
    @Override
    public void onDeleteFigure(Figure figura) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Figura")
                .setMessage("¿Estás seguro de que quieres eliminar la figura \"" + figura.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    firestoreHelper.deleteFigura(figura.getId(), new FirestoreHelper.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminPanelActivity.this, "Figura eliminada exitosamente", Toast.LENGTH_SHORT).show();
                        }
                        
                        @Override
                        public void onError(String error) {
                            Toast.makeText(AdminPanelActivity.this, "Error al eliminar figura: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    @Override
    public void onDeleteNotification(Notification notificacion) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Notificación")
                .setMessage("¿Estás seguro de que quieres eliminar esta notificación?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    firestoreHelper.deleteNotificacion(notificacion.getId(), new FirestoreHelper.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminPanelActivity.this, "Notificación eliminada exitosamente", Toast.LENGTH_SHORT).show();
                        }
                        
                        @Override
                        public void onError(String error) {
                            Toast.makeText(AdminPanelActivity.this, "Error al eliminar notificación: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    // ========== ADAPTER PARA VIEWPAGER ==========
    
    private class AdminPagerAdapter extends FragmentStateAdapter {
        
        public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return figurasFragment;
                case 1:
                    return notificacionesFragment;
                default:
                    return figurasFragment;
            }
        }
        
        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
