package com.qtcoding.btl_android;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.model.Vocabulary;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StudyFragment extends Fragment {

    private com.google.android.material.appbar.MaterialToolbar toolbar;
    private TextView tvQuestionNumber, tvWord;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4;
    private LinearLayout questionContainer;
    private MaterialCardView resultContainer;
    private TextView tvResultTitle, tvCorrectAnswers, tvPercentage;
    private MaterialButton btnRestart, btnBack;
    private NavController navController;
    private VocabCollection currentCollection;
    private List<Vocabulary> vocabList;
    private List<Vocabulary> selectedVocabs;
    private int currentQuestionIndex;
    private int correctAnswers;
    private int numberOfQuestions;
    private Random random;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
        if (getArguments() != null) {
            StudyFragmentArgs args = StudyFragmentArgs.fromBundle(getArguments());
            currentCollection = args.getCollection();
            numberOfQuestions = args.getNumberOfQuestions();
            loadVocabularies();
        } else {
            Toast.makeText(getContext(), "No collection found", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }
    }

    private void initView(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvQuestionNumber = view.findViewById(R.id.tvQuestionNumber);
        tvWord = view.findViewById(R.id.tvWord);
        btnOption1 = view.findViewById(R.id.btnOption1);
        btnOption2 = view.findViewById(R.id.btnOption2);
        btnOption3 = view.findViewById(R.id.btnOption3);
        btnOption4 = view.findViewById(R.id.btnOption4);
        questionContainer = view.findViewById(R.id.questionContainer);
        resultContainer = view.findViewById(R.id.resultContainer);
        tvResultTitle = view.findViewById(R.id.tvResultTitle);
        tvCorrectAnswers = view.findViewById(R.id.tvCorrectAnswers);
        tvPercentage = view.findViewById(R.id.tvPercentage);
        btnRestart = view.findViewById(R.id.btnRestart);
        btnBack = view.findViewById(R.id.btnBack);
        navController = Navigation.findNavController(view);
        random = new Random();
    }

    private void setEvents() {
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1, btnOption1.getText().toString()));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2, btnOption2.getText().toString()));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3, btnOption3.getText().toString()));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4, btnOption4.getText().toString()));
        btnRestart.setOnClickListener(v -> startNewSession());
        btnBack.setOnClickListener(v -> navController.navigateUp());
    }

    private void loadVocabularies() {
        ServiceManager.getInstance().getVocabularyService()
                .getVocabulariesByCollectionId(currentCollection.getId(), new ServiceCallback<List<Vocabulary>>() {
                    @Override
                    public void onSuccess(List<Vocabulary> result) {
                        vocabList = result;
                        startNewSession();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to load vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        navController.navigateUp();
                    }
                });
    }

    private void startNewSession() {
        selectedVocabs = new ArrayList<>(vocabList);

        if (selectedVocabs.size() < 4) {
            Toast.makeText(getContext(), "Not enough vocabularies to study (minimum 4 required)", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return;
        }

        if (numberOfQuestions > selectedVocabs.size()) {
            numberOfQuestions = selectedVocabs.size();
            Toast.makeText(getContext(), "Not enough vocabularies, using " + numberOfQuestions + " questions", Toast.LENGTH_SHORT).show();
        }

        Collections.shuffle(selectedVocabs, random);
        selectedVocabs = selectedVocabs.subList(0, numberOfQuestions);

        currentQuestionIndex = 0;
        correctAnswers = 0;
        questionContainer.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.GONE);
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= selectedVocabs.size()) {
            showResult();
            return;
        }

        Vocabulary currentVocab = selectedVocabs.get(currentQuestionIndex);
        tvQuestionNumber.setText(String.format("Question %d/%d", currentQuestionIndex + 1, selectedVocabs.size()));
        tvWord.setText(currentVocab.getWord());

        List<String> options = new ArrayList<>();
        options.add(currentVocab.getMeaning());

        List<Vocabulary> otherVocabs = new ArrayList<>(vocabList);
        otherVocabs.remove(currentVocab);
        Collections.shuffle(otherVocabs, random);
        for (int i = 0; i < 3 && i < otherVocabs.size(); i++) {
            options.add(otherVocabs.get(i).getMeaning());
        }

        Collections.shuffle(options, random);

        btnOption1.setText(options.get(0));
        btnOption1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)));
        btnOption2.setText(options.get(1));
        btnOption2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)));
        btnOption3.setText(options.get(2));
        btnOption3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)));
        btnOption4.setText(options.get(3));
        btnOption4.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)));

        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);
        btnOption4.setEnabled(true);
    }

    private void checkAnswer(MaterialButton selectedButton, String selectedAnswer) {
        Vocabulary currentVocab = selectedVocabs.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswer.equals(currentVocab.getMeaning());

        selectedButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(
                isCorrect ? android.R.color.holo_green_light : android.R.color.holo_red_light)));

        if (isCorrect) {
            correctAnswers++;
        } else {
            Toast.makeText(getContext(), "Wrong! Correct answer: " + currentVocab.getMeaning(), Toast.LENGTH_SHORT).show();
        }

        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);
        btnOption4.setEnabled(false);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuestionIndex++;
            showNextQuestion();
        }, 1000);
    }

    private void showResult() {
        questionContainer.setVisibility(View.GONE);
        resultContainer.setVisibility(View.VISIBLE);
        tvCorrectAnswers.setText(String.format("Correct: %d/%d", correctAnswers, selectedVocabs.size()));
        double percentage = (double) correctAnswers / selectedVocabs.size() * 100;
        tvPercentage.setText(String.format("Percentage: %.2f%%", percentage));
    }
}