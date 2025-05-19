package com.qtcoding.btl_android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.qtcoding.btl_android.R;
import com.qtcoding.btl_android.model.User;
import com.qtcoding.btl_android.model.VocabCollection;
import com.qtcoding.btl_android.service.ServiceCallback;
import com.qtcoding.btl_android.service.ServiceManager;
import com.qtcoding.btl_android.service.VocabCollectionService;
import java.util.ArrayList;
import java.util.List;

public class VocabCollectionAdapter extends RecyclerView.Adapter<VocabCollectionAdapter.VocabCollectionViewHolder> {

    public static final int VIEW_TYPE_DEFAULT = 0; // Cho các màn hình khác (có ảnh)
    public static final int VIEW_TYPE_SEARCH = 1;  // Cho tìm kiếm (không có ảnh)

    private final Context context;
    private final List<VocabCollection> allCollections; // Danh sách gốc
    private final List<VocabCollection> filteredCollections; // Danh sách đã lọc
    private final VocabCollectionService service;
    private final String currentUserId;
    private final int viewType; // Xác định loại layout
    private OnCollectionClickListener collectionClickListener;

    // Constructor với viewType
    public VocabCollectionAdapter(Context context, String currentUserId, int viewType) {
        this.context = context;
        this.allCollections = new ArrayList<>();
        this.filteredCollections = new ArrayList<>();
        this.service = new VocabCollectionService();
        this.currentUserId = currentUserId;
        this.viewType = viewType;
    }

    // Interface để xử lý click vào collection
    public interface OnCollectionClickListener {
        void onCollectionClick(VocabCollection collection);
    }

    // Setter cho listener
    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.collectionClickListener = listener;
    }

    // Cập nhật danh sách collections
    public void setCollections(List<VocabCollection> newCollections) {
        allCollections.clear();
        allCollections.addAll(newCollections);
        filteredCollections.clear();
        filteredCollections.addAll(newCollections);
        notifyDataSetChanged();
    }

    // Lọc danh sách dựa trên query
    public void filter(String query) {
        filteredCollections.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredCollections.addAll(allCollections);
        } else {
            String lowerCaseQuery = query.trim().toLowerCase();
            for (VocabCollection collection : allCollections) {
                if (collection.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (collection.getDescription() != null && collection.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredCollections.add(collection);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType; // Trả về viewType được truyền vào constructor
    }

    @NonNull
    @Override
    public VocabCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == VIEW_TYPE_SEARCH) ? R.layout.search_collection_item : R.layout.collection_item;
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new VocabCollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabCollectionViewHolder holder, int position) {
        VocabCollection collection = filteredCollections.get(position);
        holder.bind(collection);
    }

    @Override
    public int getItemCount() {
        return filteredCollections.size();
    }

    public List<VocabCollection> getCollections() {
        return this.allCollections;
    }

    class VocabCollectionViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivFavorite;
        private final TextView tvCollectionFollowers;
        private final TextView tvCardCount;
        private final TextView tvCollectionName;
        private final TextView tvCollectionOwner;
        private final ImageView ivCollectionImage; // Có thể null trong search_collection_item

        public VocabCollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvCollectionFollowers = itemView.findViewById(R.id.tvCollectionFollowers);
            tvCardCount = itemView.findViewById(R.id.tvCardCount);
            tvCollectionName = itemView.findViewById(R.id.tvCollectionName);
            tvCollectionOwner = itemView.findViewById(R.id.tvCollectionOwner);
            ivCollectionImage = itemView.findViewById(R.id.ivCollectionImage); // Có thể null
        }

        public void bind(VocabCollection collection) {
            // Ánh xạ dữ liệu
            tvCollectionName.setText(collection.getName());
            ServiceManager.getInstance().getUserService().getUser(collection.getOwnerId(), new ServiceCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    tvCollectionOwner.setText(result.getName());
                }

                @Override
                public void onFailure(Exception e) {
                    tvCollectionOwner.setText("Unknown Owner");
                }
            });

            // Hiển thị followers và card count dựa trên viewType
            if (viewType == VIEW_TYPE_SEARCH) {
                // VIEW_TYPE_SEARCH: Hiển thị "X Followers" và "X Cards"
                tvCollectionFollowers.setText(collection.getFollowerCount() + " Followers");
                tvCardCount.setText(collection.getCardCount()+ " Cards");
            } else {
                // VIEW_TYPE_DEFAULT: Hiển thị chỉ số (như "0")
                tvCollectionFollowers.setText(String.valueOf(collection.getFollowerCount()));
                tvCardCount.setText(String.valueOf(collection.getCardCount()));
            }


            // Xử lý trạng thái favorite (follow)
            if (collection.isFollowing()) {
                ivFavorite.setImageResource(R.drawable.ic_heart_solid); // Trái tim đậm
            } else {
                ivFavorite.setImageResource(R.drawable.ic_heart_regular); // Trái tim viền
            }

            // Ẩn nút favorite nếu là collection của người dùng
            if (collection.isOwned()) {
                ivFavorite.setVisibility(View.GONE);
            } else {
                ivFavorite.setVisibility(View.VISIBLE);
            }

            // Xử lý click vào favorite
            ivFavorite.setOnClickListener(v -> {
                boolean newFollowState = !collection.isFollowing();
                service.toggleFollow(currentUserId, collection.getId(), collection.isFollowing(), new ServiceCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        collection.setFollowing(newFollowState);
                        collection.setFollowerCount(collection.getFollowerCount() + (newFollowState ? 1 : -1));
                        notifyItemChanged(getAdapterPosition());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("VocabCollectionAdapter", "Failed to toggle follow: " + e.getMessage());
                        Toast.makeText(context, "Failed to toggle follow: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // Xử lý click vào toàn bộ item
            cardView.setOnClickListener(v -> {
                if (collectionClickListener != null) {
                    collectionClickListener.onCollectionClick(collection);
                }
            });
        }
    }
}
