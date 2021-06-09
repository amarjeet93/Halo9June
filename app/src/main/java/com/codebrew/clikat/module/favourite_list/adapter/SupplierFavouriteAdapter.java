package com.codebrew.clikat.module.favourite_list.adapter;

import android.content.Context;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.data.AppDataType;
import com.codebrew.clikat.databinding.ItemAllSupplierBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.other.FavouriteListModel;
import com.codebrew.clikat.modal.other.SettingModel;
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailFrag;
import com.codebrew.clikat.module.supplier_detail.SupplierDetailFragment;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;


public class SupplierFavouriteAdapter extends RecyclerView.Adapter<SupplierFavouriteAdapter.Viewholder> {

    private Context mContext;
    private List<FavouriteListModel.FavouriteDataBean> list;
    private int lastPos = -1;

    public SupplierFavouriteAdapter(Context context, List<FavouriteListModel.FavouriteDataBean> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAllSupplierBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.item_all_supplier, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        return new Viewholder(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        FavouriteListModel.FavouriteDataBean data = list.get(position);
        holder.tvStoreName.setText(data.getName());
        holder.tvReviewCount.setText("(" + data.getTotal_reviews() + " " + mContext.getResources().getString(R.string.reviews) + ")");
        holder.tvMinOrderValue.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + data.getMin_order() + "");
        holder.tvDeliveryTimeValue.setText(GeneralFunctions.getFormattedTime(data.getDelivery_min_time(), mContext) + " - " + GeneralFunctions.getFormattedTime(data.getDelivery_max_time(), mContext));
        holder.ratingBar.setText("" + data.getRating());

        holder.gpPrice.setVisibility(View.GONE);

        StaticFunction.INSTANCE.loadImage(data.getLogo(), holder.image, false);

 /*       Glide.with(mContext)
                .load(data.getLogo())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_product)
                        .fitCenter().override(80,80))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);*/

        // 0 : cash on delivery , 1: card , 2: both
        if (data.getPayment_method() == DataNames.DELIVERY_CARD) {
            holder.ivPaymentCash.setVisibility(View.GONE);
            holder.ivPaymentCard.setVisibility(View.VISIBLE);
        } else if (data.getPayment_method() == DataNames.DELIVERY_CASH) {
            holder.ivPaymentCard.setVisibility(View.GONE);
            holder.ivPaymentCash.setVisibility(View.VISIBLE);
        } else {
            holder.ivPaymentCash.setVisibility(View.VISIBLE);
            holder.ivPaymentCard.setVisibility(View.VISIBLE);
        }

        // 0: available, 1:busy , 2:closed
        switch (data.getStatus()) {
            case 1:
                holder.ivStatus.setText(mContext.getString(R.string.open));
                holder.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.username4));
                holder.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_online), null, null, null);
                break;
            case 2:
                holder.ivStatus.setText(mContext.getString(R.string.busy));
                holder.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
                holder.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_busy), null, null, null);
                break;
            default:
                holder.ivStatus.setText(mContext.getString(R.string.close));
                holder.ivStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                holder.ivStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_status_offline), null, null, null);
                break;
        }
        switch (data.getCommission_package()) {
            case 0:
                holder.ivBadge.setVisibility(View.VISIBLE);
                holder.ivBadge.setImageResource(R.drawable.ic_badge_mini_silver);
                break;
            case 1:
                holder.ivBadge.setVisibility(View.VISIBLE);
                holder.ivBadge.setImageResource(R.drawable.ic_badge_mini_bronze);
                break;
            case 2:
                holder.ivBadge.setVisibility(View.VISIBLE);
                holder.ivBadge.setImageResource(R.drawable.ic_badge_mini_gold);
                break;
            case 4:
                holder.ivBadge.setVisibility(View.VISIBLE);
                holder.ivBadge.setImageResource(R.drawable.common_google_signin_btn_icon_dark);
                break;
            default:
                holder.ivBadge.setVisibility(View.GONE);
                break;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPos) ? R.anim.up_from_bottom : R.anim.bottom_from_up);
        holder.itemView.startAnimation(animation);
        lastPos = holder.getAdapterPosition();
    }


    @Override
    public void onViewDetachedFromWindow(Viewholder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
        Group gpPrice;


        public Viewholder(View itemView) {
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
            gpPrice=itemView.findViewById(R.id.gp_price);


            itemView.setOnClickListener(this);

            tvDeliveryTime.setTypeface(AppGlobal.regular);
            tvMinOrder.setTypeface(AppGlobal.regular);
            tvStoreName.setTypeface(AppGlobal.regular);
            tvReviewCount.setTypeface(AppGlobal.regular);
            tvPaymentOptions.setTypeface(AppGlobal.regular);
            tvMinOrderValue.setTypeface(AppGlobal.regular);
            ratingBar.setTypeface(AppGlobal.regular);
            ivStatus.setTypeface(AppGlobal.semi_bold);
            tvDeliveryTimeValue.setTypeface(AppGlobal.regular);

        }


        @Override
        public void onClick(View v) {
            ((MainActivity)mContext).supplierId = list.get((getAdapterPosition())).getSupplier_id();
            ((MainActivity)mContext).supplierBranchId = list.get((getAdapterPosition())).getSupplier_branch_id();
       /*     SelectCategory dialog = new SelectCategory(mContext, list.get(getAdapterPosition()).getCategory());
            dialog.show();*/


            Bundle bundle = new Bundle();
            bundle.putInt("supplierId", list.get((getAdapterPosition())).getSupplier_id());
            bundle.putInt("branchId", list.get((getAdapterPosition())).getSupplier_branch_id());
            //bundle.putInt(DataNames.DATA_NAMES_ORDER, categoryList.get(getAdapterPosition()).getOrder());

            SettingModel.DataBean.ScreenFlowBean screenFlowBean = Prefs.with(mContext).getObject(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean.class);
            Fragment fragment;
            if (screenFlowBean.getApp_type() == AppDataType.Food.getType()) {
                fragment = new RestaurantDetailFrag();
            } else {
                fragment = new SupplierDetailFragment();
            }

            fragment.setArguments(bundle);
            ((MainActivity)mContext).
                    pushFragments(DataNames.TAB1,
                            fragment,
                            true, true, "supplierDetail", true);


        }
    }


}
