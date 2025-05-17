package com.qtcoding.btl_android;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qtcoding.btl_android.service.ServiceManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        initNavController();
        initBottomNavigationView();

        // Điều hướng đến trang chính nếu người dùng đã đăng nhập
        if(ServiceManager.getInstance().getAuthService().getCurrentUser() != null) {
            navController.navigate(R.id.action_auth_to_main);
        }
    }

    private void initView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void initNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.NavHostFragment);
        navController = navHostFragment.getNavController();
    }

    private void initBottomNavigationView() {
        // Định nghĩa NavController cho BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Lắng nghe sự thay đổi fragment để ẩn hoặc hiện BottomNavigationView
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (shouldHideBottomNavigation(destination.getId())) {
                hideBottomNavigation();
            } else {
                showBottomNavigation();
            }
        });
    }

    private boolean shouldHideBottomNavigation(int destinationId) {
        return destinationId != R.id.homeFragment &&
                destinationId != R.id.searchFragment &&
                destinationId != R.id.collectionsFragment &&
                destinationId != R.id.profileFragment;
    }

    // Ẩn BottomNavigationView
    private void hideBottomNavigation() {
        bottomNavigationView.setVisibility(BottomNavigationView.GONE);
    }

    // Hiển thị BottomNavigationView
    private void showBottomNavigation() {
        bottomNavigationView.setVisibility(BottomNavigationView.VISIBLE);
    }
}