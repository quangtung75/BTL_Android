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
import com.qtcoding.btl_android.model.Vocabulary;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.UUID;


public class AddVocabFragment extends Fragment {
    private MaterialToolbar toolbar;
    private EditText etWord, etMeaning, etExample;
    private Button btnSave, btnDelete;
    private ProgressBar progressBar;
    private NavController navController;
    private Vocabulary currentVocabulary;

    //tạo và trả về gd
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_vocab, container, false);
    }

    //Khởi tạo thành phần gq và thiết lập sự kiện
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
        if (getArguments() != null) {
            currentVocabulary = AddVocabFragmentArgs.fromBundle(getArguments()).getVocabulary();
            updateUI(); // Cập nhật UI sau khi lấy được đối tượng
        }
    }

    //Ánh xạ các thành phần gd
    private void initView(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        etWord = view.findViewById(R.id.etWord);
        etMeaning = view.findViewById(R.id.etMeaning);
        etExample = view.findViewById(R.id.etExample);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        progressBar = view.findViewById(R.id.progressBar);
        navController = Navigation.findNavController(view);
    }

    //xử lý sk
    private void setEvents(){
        // nust quay lai
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigateUp();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(currentVocabulary.getId() != null) {
                    // Cap nhat vocab nếu chỉnh sửa
                    String word = etWord.getText().toString();
                    String meaning = etMeaning.getText().toString();
                    String example = etExample.getText().toString();
                    currentVocabulary.setWord(word);
                    currentVocabulary.setMeaning(meaning);
                    currentVocabulary.setExample(example);

                    ServiceManager.getInstance().getVocabularyService().updateVocabulary(currentVocabulary, new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            navController.navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    // Tao mới vocab
                    String word = etWord.getText().toString();
                    String meaning = etMeaning.getText().toString();
                    String example = etExample.getText().toString();

                    currentVocabulary.setWord(word);
                    currentVocabulary.setMeaning(meaning);
                    currentVocabulary.setExample(example);
                    currentVocabulary.setId(UUID.randomUUID().toString());
                    // Luu vao CSDL
                    ServiceManager.getInstance().getVocabularyService().addVocabulary(currentVocabulary, new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Saved successfully", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            navController.navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                ServiceManager.getInstance().getVocabularyService().deleteVocabulary(currentVocabulary, new ServiceCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        navController.navigateUp();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    //Cập nhật giao diện theo chế độ
    private void updateUI() {
        if (currentVocabulary.getId() != null) {
            toolbar.setTitle("Edit Vocabulary");
            etWord.setText(currentVocabulary.getWord());
            etMeaning.setText(currentVocabulary.getMeaning());
            etExample.setText(currentVocabulary.getExample());
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle("Add Vocabulary");
            btnDelete.setVisibility(View.GONE);
        }
    }
}