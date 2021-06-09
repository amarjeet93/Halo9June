package com.codebrew.clikat.module.supplier_all.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.data.constants.AppConstants;
import com.codebrew.clikat.databinding.ItemAllSupplierBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ExampleAllSupplier;
import com.codebrew.clikat.modal.SupplierList;
import com.codebrew.clikat.utils.ClikatConstants;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.ArrayList;
import java.util.List;


public class SupplierAllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int categoryId;

    private Context mContext;
    private List<SupplierList> list = new ArrayList<>();
    private int ITEM_VIEW_TYPE_ROW = 1;
    private int lastPos = -1;
    private SupplierListCallback mCallback;

    public SupplierAllAdapter(Context context, ExampleAllSupplier exampleAllSupplier, int categoryId) {
        this.mContext = context;
        this.list.addAll(exampleAllSupplier.getData().getSupplierList());
        this.categoryId = categoryId;

    }

    public void settingCallback(SupplierListCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAllSupplierBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.item_all_supplier, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        return new ViewHolder_row(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_ROW) {
            ViewHolder_row holder_row = (ViewHolder_row)holder;
            SupplierList data = list.get(position);

            holder_row.gpPrice.setVisibility(View.GONE);

            holder_row.tvStoreName.setText(data.getName());
            holder_row.tvReviewCount.setText("(" + data.getTotalReviews() + " " + mContext.getResources().getString(R.string.reviews) + ")");
            holder_row.tvMinOrderValue.setText(AppConstants.Companion.getCURRENCY_SYMBOL() + " " + data.getMinOrder() + "");
            holder_row.tvDeliveryTimeValue.setText(GeneralFunctions.getFormattedTime(data.getDeliveryMinTime(), mContext) + " - " + GeneralFunctions.getFormattedTime(data.getDeliveryMaxTime(), mContext));
            holder_row.ratingBar.setText("" + data.getRating());

            StaticFunction.INSTANCE.loadImage(data.getLogo(), holder_row.image, false);

       /*     Glide.with(mContext)
                    .load(data.getLogo())
                    .apply(new RequestOptions().fitCenter())
                    .into(((ViewHolder_row) holder).image);*/

            // 0 : cash on delivery , 1: card , 2: both
            switch (data.getPaymentMethod()) {
                case 0:
                    holder_row.ivPaymentCash.setVisibility(View.VISIBLE);
                    holder_row.ivPaymentCard.setVisibility(View.GONE);
                    break;
                case 1:
                    holder_row.ivPaymentCash.setVisibility(View.GONE);
                    holder_row.ivPaymentCard.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder_row.ivPaymentCash.setVisibility(View.VISIBLE);
                    holder_row.ivPaymentCard.setVisibility(View.VISIBLE);
                    break;
            }
            // 0: available, 1:busy , 2:closed
            switch (data.getStatus()) {
                case 1:
                    holder_row.ivStatus.setText(mContext.getString(R.string.open));
                    holder_row.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.username4));
                    holder_row.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_online), null, null, null);
                    break;
                case 2:
                    holder_row.ivStatus.setText(mContext.getString(R.string.busy));
                    holder_row.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
                    holder_row.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_busy), null, null, null);
                    break;
                default:
                    holder_row.ivStatus.setText(mContext.getString(R.string.close));
                    holder_row.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                    holder_row.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_offline), null, null, null);
                    break;
            }
            if (data.getCommissionPackage() == null)
                data.setCommissionPackage(0);
            switch (data.getCommissionPackage()) {
                case 0:
                    holder_row.ivBadge.setVisibility(View.VISIBLE);
                    holder_row.ivBadge.setImageResource(R.drawable.ic_badge_mini_silver);
                    break;
                case 1:
                    holder_row.ivBadge.setVisibility(View.VISIBLE);
                    holder_row.ivBadge.setImageResource(R.drawable.ic_badge_mini_bronze);
                    break;
                case 2:
                    holder_row.ivBadge.setVisibility(View.VISIBLE);
                    holder_row.ivBadge.setImageResource(R.drawable.ic_badge_mini_gold);
                    break;
                case 4:
                    holder_row.ivBadge.setVisibility(View.VISIBLE);
                    holder_row.ivBadge.setImageResource(R.drawable.ic_np);
                    break;
                default:
                    holder_row.ivBadge.setVisibility(View.GONE);
                    break;
            }
            if (data.isSponsor() == 1) {
                holder_row.ivSponser.setVisibility(View.VISIBLE);
                if (StaticFunction.INSTANCE.getLanguage(mContext) == ClikatConstants.LANGUAGE_OTHER) {
                    holder_row.ivSponser.setRotation(-270);
                }
                holder_row.itemView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.card_sponser));
            } else {
                holder_row.ivSponser.setVisibility(View.GONE);
                holder_row.itemView.setBackground(ContextCompat.getDrawable(mContext, R.color.white));
            }
        }
        try {
            Animation animation = AnimationUtils.loadAnimation(mContext, (holder.getAdapterPosition() > lastPos) ? R.anim.up_from_bottom : R.anim.bottom_from_up);
            holder.itemView.startAnimation(animation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastPos = holder.getAdapterPosition();
    }


    @Override
    public int getItemViewType(int position) {

        return ITEM_VIEW_TYPE_ROW;
    }


    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public interface SupplierListCallback {
        void onSupplierListDetail(int position, List<SupplierList> list);
    }


    public class ViewHolder_row extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvStoreName;
        TextView tvReviewCount;
        TextView tvMinOrder;
        TextView tvDeliveryTime;
        TextView tvMinOrderValue;
        TextView tvDeliveryTimeValue;
        TextView tvPaymentOptions;
        ImageView image;
        ImageView ivPaymentCard;
        ImageView ivPaymentCash;
        TextView ivStatus;
        TextView ratingBar;
        ImageView ivBadge;
        ImageView ivSponser;
        Group gpPrice;


        public ViewHolder_row(View itemView) {
            super(itemView);

            tvStoreName=itemView.findViewById(R.id.tvStoreName);
            tvReviewCount=itemView.findViewById(R.id.tvReviewCount);
            tvMinOrder=itemView.findViewById(R.id.tvMinOrder);
            tvDeliveryTime=itemView.findViewById(R.id.tvDeliveryTime);
            tvMinOrderValue=itemView.findViewById(R.id.tvMinOrderValue);
            tvDeliveryTimeValue=itemView.findViewById(R.id.tvDeliveryTimeValue);
            tvPaymentOptions=itemView.findViewById(R.id.tvPaymentOptions);
            image=itemView.findViewById(R.id.image);
            ivPaymentCard=itemView.findViewById(R.id.ivPaymentCard);
            ivPaymentCash=itemView.findViewById(R.id.ivPaymentCash);
            ivStatus=itemView.findViewById(R.id.ivStatus);
            ratingBar=itemView.findViewById(R.id.ratingBar);
            ivBadge=itemView.findViewById(R.id.ivBadge);
            ivSponser=itemView.findViewById(R.id.ivSponser);
            gpPrice=itemView.findViewById(R.id.gp_price);

            itemView.setOnClickListener(this);

            tvDeliveryTime.setTypeface(AppGlobal.regular);
            tvMinOrder.setTypeface(AppGlobal.regular);
            tvStoreName.setTypeface(AppGlobal.regular);
            tvReviewCount.setTypeface(AppGlobal.regular);
            tvPaymentOptions.setTypeface(AppGlobal.regular);
            tvMinOrderValue.setTypeface(AppGlobal.regular);
            tvDeliveryTimeValue.setTypeface(AppGlobal.regular);
            ratingBar.setTypeface(AppGlobal.regular);
            ivStatus.setTypeface(AppGlobal.semi_bold);

        }


        @Override
        public void onClick(View v) {
            if (categoryId != 0) {

                mCallback.onSupplierListDetail(getAdapterPosition(), list);

            }
        }
    }


    public SupplierList removeItem(int position) {
        final SupplierList model = list.remove(position);

        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, SupplierList model) {
        list.add(position, model);

        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final SupplierList model = list.remove(fromPosition);
        list.add(toPosition, model);

        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<SupplierList> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<SupplierList> newModels) {
        for (int i = list.size() - 1; i >= 0; i--) {
            final SupplierList model = list.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<SupplierList> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final SupplierList model = newModels.get(i);
            if (!list.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<SupplierList> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final SupplierList model = newModels.get(toPosition);
            final int fromPosition = list.indexOf(model);
            if (fromPosition >= 0) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}
