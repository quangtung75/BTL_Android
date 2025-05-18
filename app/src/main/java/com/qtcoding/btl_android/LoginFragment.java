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
import android.widget.TextView;
import android.widget.Toast;

import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

public class LoginFragment extends Fragment {
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnLoginWithGoogle;
    private TextView tvDontHaveAccount;
    private ProgressBar progressBar;
    private NavController navController;
    private String email, password;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
    }

    private void initView(View view){
        edtEmail = view.findViewById(R.id.edt_email_login);
        edtPassword = view.findViewById(R.id.edt_password_login);
        btnLogin = view.findViewById(R.id.btn_login);
        btnLoginWithGoogle = view.findViewById(R.id.btn_loginWithGoogle);
        tvDontHaveAccount = view.findViewById(R.id.tv_dontHaveAccount);
        progressBar = view.findViewById(R.id.progressBar);
        navController = Navigation.findNavController(view);
    }

    private void setEvents(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateData()){
                    performLogin();
                }
            }
        });
        btnLoginWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoginWithGoogle();
            }
        });
        tvDontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_loginFragment_to_signUpFragment);
            }
        });
    }

    private void performLogin(){
        progressBar.setVisibility(View.VISIBLE);
        ServiceManager.getInstance().getAuthService().login(email, password, new ServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                //Chuyen den trang chinh
                navController.navigate(R.id.action_auth_to_main);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                //Hien thi loi
                Toast.makeText(getContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLoginWithGoogle(){
        Toast.makeText(getContext(),"Login with Google", Toast.LENGTH_SHORT).show();
    }

    private boolean validateData(){
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();

        if (email.isEmpty()){
            edtEmail.setError("Email required");
            return false;
        }
        if (password.isEmpty()){
            edtPassword.setError("Password required");
            return false;
        }
        return true;
    }
}