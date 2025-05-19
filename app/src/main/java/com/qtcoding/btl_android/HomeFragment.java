package com.qtcoding.btl_android;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;

import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.VocabCollectionService;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardView cardView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private VocabCollectionService service;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
        loadTopCollections();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.rePopularCollections);
        cardView = view.findViewById(R.id.cardStudyNow);
        progressBar = view.findViewById(R.id.progressBarLoading);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        navController = Navigation.findNavController(view);

        // Khởi tạo RecyclerView và Adapter
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        // Khởi tạo service
        service = new VocabCollectionService();
    }

    private void setEvents() {
        // Xử lý click vào collection để điều hướng đến màn hình chi tiết
        // Xử lý kéo xuống để làm mới
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTopCollections();
        });
    }

    private void loadTopCollections() {
//        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true); // Hiển thị vòng xoay khi làm mới
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        service.getTopCollections(currentUserId, new ServiceCallback<List<VocabCollection>>() {
            @Override
            public void onSuccess(List<VocabCollection> result) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ẩn vòng xoay
                Log.d("Collections", result.toString());
            }

            @Override
            public void onFailure(Exception e) {
//                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ẩn vòng xoay
                Toast.makeText(getContext(), "Failed to load collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error loading top collections", e);
            }
        });
    }
}