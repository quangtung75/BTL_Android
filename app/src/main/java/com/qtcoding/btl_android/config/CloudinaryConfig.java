package com.qtcoding.btl_android.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;

    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "ddhzybyor",      // Tên Cloudinary
                    "api_key", "298527834638236",            // API Key
                    "api_secret", "beqgLYFxT4yL-_dt3ktH37-N0EI"       // API Secret
            ));
        }
        return cloudinary;
    }
}
