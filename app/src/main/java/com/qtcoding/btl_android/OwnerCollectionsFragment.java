package com.qtcoding.btl_android;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.qtcoding.btl_android.adapter.VocabCollectionAdapter;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.List;

public class OwnerCollectionsFragment extends Fragment {

    private FloatingActionButton fab;
    private TextInputEditText edtCollectionName;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavController navController;
    private VocabCollectionAdapter adapter;
    private String currentQuery = "";
    private boolean needsRefresh = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_collections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
        loadOwnedCollections();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needsRefresh) {
            loadOwnedCollections();
        }
    }

    private void initView(View view) {
        fab = view.findViewById(R.id.fab);
        edtCollectionName = view.findViewById(R.id.etCollectionName);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment);
        adapter = new VocabCollectionAdapter(requireContext(), ServiceManager.getInstance().getAuthService().getCurrentUser().getUid(), VocabCollectionAdapter.VIEW_TYPE_DEFAULT);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void setEvents() {
        fab.setOnClickListener(v -> {
            NavDirections action = CollectionsFragmentDirections.actionCollectionsFragmentToAddCollectionFragment(null);
            navController.navigate(action);
            needsRefresh = true;
        });

        edtCollectionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim(); // Chỉ lưu query
            }
        });

        edtCollectionName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    adapter.filter(currentQuery); // Tìm kiếm khi nhấn Enter
                    needsRefresh = false;
                    // Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtCollectionName.getWindowToken(), 0);
                    return true; // Chặn xuống dòng
                }
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadOwnedCollections);

        adapter.setOnCollectionClickListener(collection -> {
            NavDirections action = CollectionsFragmentDirections.actionCollectionsFragmentToDetailCollectionFragment(collection);
            navController.navigate(action);
            needsRefresh = true;
        });
    }

    public void loadOwnedCollections() {
        swipeRefreshLayout.setRefreshing(true); // Hiển thị vòng xoay
        ServiceManager.getInstance().getVocabCollectionService().getOwnedCollections(
                ServiceManager.getInstance().getAuthService().getCurrentUser().getUid(),
                new ServiceCallback<List<VocabCollection>>() {
                    @Override
                    public void onSuccess(List<VocabCollection> result) {
                        adapter.setCollections(result);
                        if (!currentQuery.isEmpty()) {
                            adapter.filter(currentQuery); // Khôi phục trạng thái tìm kiếm
                        }
                        if (result.isEmpty()) {
                            Toast.makeText(requireContext(), "No owned collections found", Toast.LENGTH_SHORT).show();
                        }
                        needsRefresh = false;
                        swipeRefreshLayout.setRefreshing(false); // Ẩn vòng xoay
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to load collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }
}