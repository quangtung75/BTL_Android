package com.qtcoding.btl_android;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private TextInputEditText etCollectionName;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NavController navController;
    private String currentQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
    }

    private void initView(View view) {
        etCollectionName = view.findViewById(R.id.etCollectionName);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        navController = Navigation.findNavController(view);
    }

    private void setEvents() {
        etCollectionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim(); // Chỉ lưu query
            }
        });

        etCollectionName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    search(currentQuery); // Tìm kiếm khi nhấn Enter
                    // Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etCollectionName.getWindowToken(), 0);
                    return true; // Chặn xuống dòng
                }
                return false;
            }
        });


    }

    private void search(String query) {
        if (query.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ServiceManager.getInstance().getVocabCollectionService().searchCollectionsByName(
                query,
                ServiceManager.getInstance().getAuthService().getCurrentUser().getUid(),
                new ServiceCallback<List<VocabCollection>>() {
                    @Override
                    public void onSuccess(List<VocabCollection> result) {
                        Log.d("Collections", result.toString());
                        progressBar.setVisibility(View.GONE);
                        if (result.isEmpty()) {
                            Toast.makeText(requireContext(), "No collections found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to search collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }
}