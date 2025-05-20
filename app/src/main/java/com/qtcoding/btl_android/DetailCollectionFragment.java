package com.qtcoding.btl_android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import com.qtcoding.btl_android.adapter.VocabularyAdapter;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.model.Vocabulary;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.List;
import java.util.Locale;

public class DetailCollectionFragment extends Fragment {

    private MaterialToolbar toolbar;
    private TextView etCollectionName;
    private ImageView btnToolbar;
    private TextView tvVocabularyCount;
    private Button btnStartStudy;
    private FloatingActionButton fab;
    private NavController navController;
    private RecyclerView rvVocabulary;
    private VocabularyAdapter adapter;
    private VocabCollection currentCollection;
    private TextToSpeech textToSpeech;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_collection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initTextToSpeech();
        setEvents();
        if (getArguments() != null) {
            currentCollection = DetailCollectionFragmentArgs.fromBundle(getArguments()).getCollection();
            updateUI(); // Cập nhật UI sau khi lấy được đối tượng
            loadVocabularies();
        } else {
            // Xử lý trường hợp không có arguments
            Log.e("DetailCollectionFragment", "No arguments found");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentCollection != null) {
            updateUI();
            loadVocabularies();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown(); // Giải phóng TextToSpeech
        }
    }

    private void initView(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        etCollectionName = view.findViewById(R.id.etCollectionName);
        btnToolbar = view.findViewById(R.id.btnToolbar);
        tvVocabularyCount = view.findViewById(R.id.tvVocabularyCount);
        btnStartStudy = view.findViewById(R.id.btnStudy);
        rvVocabulary = view.findViewById(R.id.rvCollectionItems);
        adapter = new VocabularyAdapter();
        rvVocabulary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVocabulary.setAdapter(adapter);
        fab = view.findViewById(R.id.fab);
        navController = Navigation.findNavController(view);
        currentUserId = ServiceManager.getInstance().getAuthService().getCurrentUser().getUid();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getContext(), "Language not supported. !", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Initialization failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEvents() {
        // Nút back trên toolbar
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());


        // Nút toolbar (save hoặc follow/unfollow)
        btnToolbar.setOnClickListener(v -> {
            if (currentCollection == null) return;
            if (currentCollection.isOwned()) {
                // Navigate to AddCollectionFragment (Edit)
                DetailCollectionFragmentDirections.ActionDetailCollectionFragmentToAddCollectionFragment action =
                        DetailCollectionFragmentDirections.actionDetailCollectionFragmentToAddCollectionFragment(currentCollection);
                navController.navigate(action);
            } else {
                performToggleFollow();
            }
        });

        // Nút Start Study
        btnStartStudy.setOnClickListener(v -> {
            if (currentCollection == null) {
                Toast.makeText(getContext(), "No collection selected", Toast.LENGTH_SHORT).show();
                return;
            }
            showStudySettingsDialog();
        });

        // FAB để thêm từ vựng
        fab.setOnClickListener(v -> {
            if (currentCollection != null && currentCollection.isOwned()) {
                Vocabulary vocabulary = new Vocabulary();
                vocabulary.setCollectionId(currentCollection.getId());
                DetailCollectionFragmentDirections.ActionDetailCollectionFragmentToAddVocabFragment action =
                        DetailCollectionFragmentDirections.actionDetailCollectionFragmentToAddVocabFragment(vocabulary);
                navController.navigate(action);
            }
        });

        adapter.setOnClick(new VocabularyAdapter.OnVocabClickListener() {
            @Override
            public void onVocabClick(Vocabulary vocabulary) {
                if(currentCollection.isOwned()) {
                    DetailCollectionFragmentDirections.ActionDetailCollectionFragmentToAddVocabFragment action =
                            DetailCollectionFragmentDirections.actionDetailCollectionFragmentToAddVocabFragment(vocabulary);
                    navController.navigate(action);
                }
            }

            @Override
            public void onSpeakerClick(Vocabulary vocabulary) {
                String wordToSpeak = vocabulary.getWord();
                if (wordToSpeak != null && !wordToSpeak.isEmpty()) {
                    textToSpeech.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Toast.makeText(getContext(), "No word found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showStudySettingsDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_study_settings, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        TextInputEditText etQuestionCount = dialogView.findViewById(R.id.etQuestionCount);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnStart = dialogView.findViewById(R.id.btnStart);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(v -> {
            String questionCountStr = etQuestionCount.getText().toString();
            if (questionCountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter number of questions", Toast.LENGTH_SHORT).show();
                return;
            }

            int numberOfQuestions;
            try {
                numberOfQuestions = Integer.parseInt(questionCountStr);
                if (numberOfQuestions < 1) {
                    Toast.makeText(getContext(), "Number of questions must be at least 1", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
                return;
            }

            DetailCollectionFragmentDirections.ActionDetailCollectionFragmentToStudyFragment action =
                    DetailCollectionFragmentDirections.actionDetailCollectionFragmentToStudyFragment(currentCollection);
            action.setNumberOfQuestions(numberOfQuestions);
            navController.navigate(action);

            dialog.dismiss();
        });

        dialog.show();
    }


    private void performToggleFollow() {
        if (currentCollection == null) return;
        ServiceManager.getInstance().getVocabCollectionService().toggleFollow(currentUserId, currentCollection.getId(),
                currentCollection.isFollowing(), new ServiceCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        boolean newFollowState = !currentCollection.isFollowing();
                        currentCollection.setFollowing(newFollowState);
                        updateUI();
                        Toast.makeText(getContext(), newFollowState ? "Followed" : "Unfollowed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to toggle follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (currentCollection == null) return;
        etCollectionName.setText(currentCollection.getName());
        tvVocabularyCount.setText(String.valueOf(currentCollection.getCardCount()));
        updateToolbarIcon();
        fab.setVisibility(currentCollection.isOwned() ? View.VISIBLE : View.GONE);
        etCollectionName.setEnabled(currentCollection.isOwned());
    }

    private void loadVocabularies() {
        ServiceManager.getInstance().getVocabularyService()
                .getVocabulariesByCollectionId(currentCollection.getId(), new ServiceCallback<List<Vocabulary>>() {
                    @Override
                    public void onSuccess(List<Vocabulary> result) {
                        adapter.setVocabList(result);
                        tvVocabularyCount.setText(String.valueOf(result.size()));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to load vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateToolbarIcon() {
        if (currentCollection == null) return;
        if (currentCollection.isOwned()) {
            btnToolbar.setImageResource(R.drawable.ic_edit);
        } else {
            btnToolbar.setImageResource(currentCollection.isFollowing() ? R.drawable.ic_heart_solid : R.drawable.ic_heart_regular);
        }
    }
}