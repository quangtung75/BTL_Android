package com.qtcoding.btl_android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;


public class AddCollectionFragment extends Fragment {
    private MaterialToolbar toolbar;
    private EditText edtTitle, edtDescription;
    private Button btnSave, btnCancel, btnDelete;
    private ProgressBar progressBar;
    private VocabCollection vocabCollection;
    private NavController navController;

    //tạo và trả về gd add collection
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_collection, container, false);
    }

    //Khởi tạo thành phần gq và thiết lập sự kiện
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
    }

    //Ánh xạ các thành phần gd
    private void initView(View view){
        toolbar = view.findViewById(R.id.toolbar);
        edtTitle = view.findViewById(R.id.etTitle);
        edtDescription = view.findViewById(R.id.etDescription);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        progressBar = view.findViewById(R.id.progressBar);

        navController = Navigation.findNavController(view);

        // lay collection ID tu arguments kiểm tra xem là "Add" or "Edit"
        if (getArguments() != null) {
            vocabCollection = AddCollectionFragmentArgs.fromBundle(getArguments()).getVocabCollection();
            if (vocabCollection != null) {
                // dieu chinh UI cho Edit
                toolbar.setTitle("Edit Collection");
                btnDelete.setVisibility(View.VISIBLE);
                edtTitle.setText(vocabCollection.getName());
                edtDescription.setText(vocabCollection.getDescription());
            } else {
                // ẩn Delete
                toolbar.setTitle("Add Collection");
                btnDelete.setVisibility(View.GONE);
            }
        }
    }

    //xử lý sk
    private void setEvents(){
        //XU ly Save
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = edtTitle.getText().toString().trim();
                String description = edtDescription.getText().toString().trim();
                String currentUserId = ServiceManager.getInstance().getAuthService().getCurrentUser().getUid();
                //validate dữ liệu
                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                // xu ly logic collection
                if (vocabCollection != null) {
                    // Cap nhat collection

                    vocabCollection.setName(title);
                    vocabCollection.setDescription(description);
                    ServiceManager.getInstance().getVocabCollectionService().updateCollection(vocabCollection, new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Collection updated successfully", Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to update collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    // tao mới collection

                    VocabCollection newCollection = new VocabCollection();
                    newCollection.setName(title);
                    newCollection.setDescription(description);
                    newCollection.setOwnerId(currentUserId);
                    ServiceManager.getInstance().getVocabCollectionService().addCollection(currentUserId, newCollection, new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Collection added successfully", Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to add collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //xử lý quay lại
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigateUp();
            }
        });
        // xu ly nut Xoa, chỉ có thể dùng ở Edit
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if (vocabCollection != null) {
                    // logic Xoa colletion
                    String currentUserId = ServiceManager.getInstance().getAuthService().getCurrentUser().getUid();
                    ServiceManager.getInstance().getVocabCollectionService().deleteCollection(currentUserId, vocabCollection.getId(), new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Collection deleted successfully", Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to delete collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}