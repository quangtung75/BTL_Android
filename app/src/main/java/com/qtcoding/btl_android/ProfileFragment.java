package com.qtcoding.btl_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.qtcoding.btl_android.config.CloudinaryConfig;
import com.qtcoding.btl_android.model.User;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvCollections;
    private Button btnEditProfile, btnSetStudyGoals, btnLogout;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivUser;
    private ImageView ivProfilePicture;

    private NavController navController;
    private String currentUserId;
    private Uri selectedImageUri;
    private User currentUser;
    private ProgressDialog progressDialog;
    private static boolean isCloudinaryInitialized = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setEvents();
        loadUserData();
        requestNotificationPermission();
        initializeCloudinary();
    }

    private void initializeCloudinary() {
        if (!isCloudinaryInitialized) {
            try {
                MediaManager.init(requireContext(), CloudinaryConfig.getCloudinary().config);
                isCloudinaryInitialized = true;
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to initialize Cloudinary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCollections = view.findViewById(R.id.tvCollections);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        ivUser = view.findViewById(R.id.ivUser);

        btnSetStudyGoals = view.findViewById(R.id.btnSetStudyGoals);
        btnLogout = view.findViewById(R.id.btnLogout);
        navController = Navigation.findNavController(view);
        currentUserId = ServiceManager.getInstance().getAuthService().getCurrentUser().getUid();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    private void setEvents() {
        btnEditProfile.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        btnSetStudyGoals.setOnClickListener(v -> showStudyGoalsDialog());

        btnLogout.setOnClickListener(v -> {
            ServiceManager.getInstance().getAuthService().logout(new ServiceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_main_to_auth);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Logout failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }


    private void loadUserData() {
        progressDialog.show();
        ServiceManager.getInstance().getUserService().getUser(currentUserId, new ServiceCallback<User>() {
            @Override
            public void onSuccess(User user) {
                progressDialog.dismiss();
                currentUser = user;
                tvName.setText(user.getName());
                tvEmail.setText("Email: " + user.getEmail());

                // Load profile image if exists
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivUser);
                } else {
                    // Set default icon
                    ivUser.setImageResource(R.drawable.ic_user);
                }

                loadCollectionsCount();
                // Đồng bộ SharedPreferences và tái thiết lập thông báo
                SharedPreferences prefs = getContext().getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putInt("notificationHour", user.getNotificationHour())
                        .putInt("notificationMinute", user.getNotificationMinute())
                        .putInt("studyDuration", user.getStudyDurationInMinutes())
                        .apply();
                setupNotification(user.getNotificationHour(), user.getNotificationMinute(), user.getStudyDurationInMinutes());
            }

            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_profile, null);
        builder.setView(dialogView);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        ivProfilePicture = dialogView.findViewById(R.id.ivProfilePicture);
        Button btnSelectPhoto = dialogView.findViewById(R.id.btnSelectPhoto);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Hiển thị thông tin hiện tại của người dùng
        if (currentUser != null) {
            etName.setText(currentUser.getName());

            // Load ảnh đại diện hiện tại
            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .circleCrop()
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_user);
            }
        }

        AlertDialog dialog = builder.create();

        // Xử lý sự kiện khi bấm nút chọn ảnh
        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Xử lý sự kiện khi bấm nút hủy
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            ivProfilePicture = null; // Reset selected image URI
        });

        // Xử lý sự kiện khi bấm nút lưu
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật thông tin người dùng
            updateUserProfile(name, selectedImageUri, dialog);
        });

        dialog.show();
    }

    private void updateUserProfile(String name, Uri imageUri, AlertDialog dialog) {
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        if (imageUri != null) {
            // Upload ảnh lên Cloudinary trước khi cập nhật thông tin người dùng
            uploadImageToCloudinary(imageUri, name, dialog);
        } else {
            // Nếu không chọn ảnh mới, chỉ cập nhật tên
            if (currentUser != null) {
                currentUser.setName(name);
                updateUserInFirestore(currentUser, dialog);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, String name, AlertDialog dialog) {

        String fileName = "memo_app/profiles/profile_" + currentUserId;


        // Tạo request upload ảnh
        MediaManager.get().upload(imageUri)
                .option("public_id", fileName)
                .option("overwrite", true)
                .option("folder", "memo_app/profiles")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload bắt đầu
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Cập nhật tiến trình nếu cần
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Upload thành công
                        String imageUrl = resultData.get("secure_url").toString();

                        // Cập nhật thông tin người dùng với URL ảnh mới
                        if (currentUser != null) {
                            currentUser.setName(name);
                            currentUser.setPhotoUrl(imageUrl);
                            updateUserInFirestore(currentUser, dialog);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        // Xử lý lỗi upload
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload image: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Upload bị hoãn lại
                    }
                }).dispatch();
    }

    private void updateUserInFirestore(User user, AlertDialog dialog) {
        ServiceManager.getInstance().getUserService().updateUser(user, new ServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressDialog.dismiss();
                dialog.dismiss();
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                // Cập nhật UI sau khi cập nhật thành công
                tvName.setText(user.getName());
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivUser);
                }

                // Reset selectedImageUri sau khi đã sử dụng
                selectedImageUri = null;
            }

            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (ivProfilePicture != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(ivProfilePicture);
            }

        }
    }


    private void loadCollectionsCount() {
        ServiceManager.getInstance().getUserService().countCollectionsByUser(currentUserId, new ServiceCallback<Long>() {
            @Override
            public void onSuccess(Long count) {
                tvCollections.setText("Collections: " + count);
            }

            @Override
            public void onFailure(Exception e) {
                tvCollections.setText("Collections: 0");
                Toast.makeText(getContext(), "Failed to load collections count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStudyGoalsDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_study_goals, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        TextInputEditText etStudyDuration = dialogView.findViewById(R.id.etStudyDuration);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Load current values
        ServiceManager.getInstance().getUserService().getUser(currentUserId, new ServiceCallback<User>() {
            @Override
            public void onSuccess(User user) {
                etStudyDuration.setText(String.valueOf(user.getStudyDurationInMinutes()));
                timePicker.setHour(user.getNotificationHour());
                timePicker.setMinute(user.getNotificationMinute());
            }

            @Override
            public void onFailure(Exception e) {
                etStudyDuration.setText("30");
                timePicker.setHour(8);
                timePicker.setMinute(0);
            }
        });

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Validate study duration
            String durationStr = etStudyDuration.getText().toString();
            if (durationStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter study duration", Toast.LENGTH_SHORT).show();
                return;
            }
            int studyDuration;
            try {
                studyDuration = Integer.parseInt(durationStr);
                if (studyDuration < 1) {
                    Toast.makeText(getContext(), "Study duration must be at least 1 minute", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid study duration format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get notification time from TimePicker
            int notificationHour = timePicker.getHour();
            int notificationMinute = timePicker.getMinute();

            // Update study duration
            ServiceManager.getInstance().getUserService().updateStudyDuration(currentUserId, studyDuration, new ServiceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Update notification time
                    ServiceManager.getInstance().getUserService().updateNotificationTime(currentUserId, notificationHour, notificationMinute, new ServiceCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(getContext(), "Study goals and notification time updated!", Toast.LENGTH_SHORT).show();
                            setupNotification(notificationHour, notificationMinute, studyDuration);
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to update notification time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to update study duration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void setupNotification(int hour, int minute, int studyDuration) {
        NotificationHelper.scheduleDailyNotification(getContext(), hour, minute, studyDuration);
    }
}