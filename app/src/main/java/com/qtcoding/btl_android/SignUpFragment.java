package com.qtcoding.btl_android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qtcoding.btl_android.model.User;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;


public class SignUpFragment extends Fragment {


    private EditText edtFullname, edtEmail, edtPassword, edtPasswordAgain;
    private Button btnSignUp;
    private TextView tvHaveAccount;
    private ProgressBar progressBar;
    private NavController navController;

    private String fullname, email, password, passwordAgain;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
    }

    private void initView(View view) {
        edtFullname = view.findViewById(R.id.edt_fullname_signup);
        edtEmail = view.findViewById(R.id.edt_email_signup);
        edtPassword = view.findViewById(R.id.edt_password_signup);
        edtPasswordAgain = view.findViewById(R.id.edt_passwordAgainSignup);
        btnSignUp = view.findViewById(R.id.btn_SignUp);
        tvHaveAccount = view.findViewById(R.id.tv_haveAccount);
        progressBar = view.findViewById(R.id.progressBar);
        navController = Navigation.findNavController(view);
    }

    private void setEvents() {
        btnSignUp.setOnClickListener(v -> {
            if (validateData()) {
                performSignUp(); // Nếu validate ok thì thực hiện signUp
            }
        });

        tvHaveAccount.setOnClickListener(v -> {
            navController.navigate(R.id.action_signUpFragment_to_loginFragment);
        });
    }

    private void performSignUp() {
        // Hiển thị progress bar trong khi xử lý
        progressBar.setVisibility(View.VISIBLE);

        // Gọi ServiceManager để đăng ký người dùng
        ServiceManager.getInstance().getAuthService().signUp(email, password, new ServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                createProfile();
            }

            @Override
            public void onFailure(Exception e) {
                // Khi có lỗi trong quá trình đăng ký
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createProfile() {
        String userId = ServiceManager.getInstance().getAuthService().getCurrentUser().getUid();
        if(userId == null) {
            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User(userId, fullname, email, null, 8, 0, 60); // Tạo User với thông tin cần thiết

        ServiceManager.getInstance().getUserService().createUser(user, new ServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getContext(), "Profile Created", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_auth_to_main);
            }

            @Override
            public void onFailure(Exception e) {
                // Khi có lỗi trong quá trình tạo profile
                Toast.makeText(getContext(), "Profile Creation Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean validateData() {
        fullname = edtFullname.getText().toString().trim();
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        passwordAgain = edtPasswordAgain.getText().toString().trim();

        if (fullname.isEmpty()) {
            edtFullname.setError("Fullname is required");
            edtFullname.requestFocus();
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Valid email is required");
            edtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6 || !password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$")) {
            edtPassword.setError("Password must be at least 6 characters, contain letters and numbers");
            edtPassword.requestFocus();
            return false;
        }

        if (!password.equals(passwordAgain)) {
            edtPasswordAgain.setError("Passwords do not match");
            edtPasswordAgain.requestFocus();
            return false;
        }

        return true; // Đã validate thành công
    }


}