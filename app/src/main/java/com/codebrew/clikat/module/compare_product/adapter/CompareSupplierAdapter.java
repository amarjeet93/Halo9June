package com.codebrew.clikat.module.compare_product.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.data.constants.AppConstants;
import com.codebrew.clikat.databinding.ItemAllSupplierBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CompareResultDetail;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

/*
 * Created by cbl80 on 8/7/16.
 */
public class CompareSupplierAdapter extends RecyclerView.Adapter<CompareSupplierAdapter.Viewholder> {

    private Context mContext;
    private List<CompareResultDetail> list;
    private int lastPos = -1;

    private DecimalFormat decimalFormat = new DecimalFormat("###.##");

    public CompareSupplierAdapter(Context context, List<CompareResultDetail> list) {
        this.mContext = context;
        this.list = list;
    }


    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAllSupplierBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_all_supplier, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        return new Viewholder(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        CompareResultDetail data = list.get(position);
        holder.tvStoreName.setText(data.getSupplier_name());
        holder.tvReviewCount.setText(MessageFormat.format("({0} {1})", data.getTotal_reviews(), mContext.getResources().getString(R.string.reviews)));
        holder.tvMinOrderValue.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + data.getMin_order() + "");
        holder.tvDeliveryTimeValue.setText(GeneralFunctions.getFormattedTime(data.getDelivery_min_time(), mContext) + " - " + GeneralFunctions.getFormattedTime(data.getDelivery_max_time(), mContext));
        holder.ratingBar.setText("" + data.getRating());
        float netPrice = 0f;
        if (data.getPrice_type() == 1) {
            netPrice = data.getHourly_price().get(0).getPrice_per_hour();
            holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + netPrice
                    + " " + mContext.getString(R.string.per_hour));
        } else {
            if (!data.getPrice().equals(""))
                netPrice = Float.parseFloat(data.getPrice());
            holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + netPrice);
        }

        StaticFunction.INSTANCE.loadImage(data.getLogo(), holder.image, false);

       /* Glide.with(mContext)
                .load(data.getLogo())
                .apply(new RequestOptions()
                        .fitCenter())
                .into(holder.image);*/

        // 0 : cash on delivery , 1: card , 2: both
        if (data.getPayment_method().intValue() == DataNames.DELIVERY_CARD) {
            holder.ivPaymentCash.setVisibility(View.GONE);
            holder.ivPaymentCard.setVisibility(View.VISIBLE);
        } else if (data.getPayment_method() == DataNames.DELIVERY_CASH) {
            holder.ivPaymentCard.setVisibility(View.GONE);
            holder.ivPaymentCash.setVisibility(View.VISIBLE);
        } else {
            holder.ivPaymentCash.setVisibility(View.VISIBLE);
            holder.ivPaymentCard.setVisibility(View.VISIBLE);
        }
        switch (data.getCommissionPackage()) {
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
                holder.ivBadge.setImageResource(R.drawable.ic_np);
                break;
            default:
                holder.ivBadge.setVisibility(View.GONE);
                break;
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
       /* float actualPrice = 0;
        if (!data.getDisplay_price().equals(""))
            actualPrice = Float.parseFloat((data.getDisplay_price()));
        if (actualPrice == 0f || actualPrice == 0.0
        ) {
            holder.tvActualPrice.setVisibility(View.GONE);
        } else if (actualPrice == netPrice) {
            holder.tvActualPrice.setVisibility(View.GONE);
        } else {
            holder.tvActualPrice.setText(StaticFunction.getCurrency(mContext) + " " + actualPrice);
            holder.tvActualPrice.setVisibility(View.VISIBLE);
        }*/

        holder.gpPrice.setVisibility(data.getFixed_price()==null ? View.GONE : View.VISIBLE);

        if(data.getFixed_price()!=null)
        {
            holder.tvPrice.setText(!data.getFixed_price().equals(data.getDisplay_price()) ?
                    mContext.getString(R.string.discountprice_tag, AppConstants.Companion.getCURRENCY_SYMBOL(), Float.valueOf(data.getDisplay_price())) :
                    mContext.getString(R.string.discountprice_tag,AppConstants.Companion.getCURRENCY_SYMBOL(), Float.valueOf(data.getFixed_price())));
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
        TextView tvPrice;
        Button tvActualPrice;
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
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvActualPrice=itemView.findViewById(R.id.tvActualPrice);
            ivBadge=itemView.findViewById(R.id.ivBadge);
            gpPrice=itemView.findViewById(R.id.gp_price);

            itemView.setOnClickListener(this);

/*            tvDeliveryTime.setTypeface(AppGlobal.regular);
            tvMinOrder.setTypeface(AppGlobal.regular);
            tvStoreName.setTypeface(AppGlobal.regular);
            tvReviewCount.setTypeface(AppGlobal.regular);
            tvPaymentOptions.setTypeface(AppGlobal.regular);
            tvMinOrderValue.setTypeface(AppGlobal.regular);
            ratingBar.setTypeface(AppGlobal.regular);
            ivStatus.setTypeface(AppGlobal.semi_bold);
            tvPrice.setTypeface(AppGlobal.regular);
            tvActualPrice.setTypeface(AppGlobal.regular);
            tvDeliveryTimeValue.setTypeface(AppGlobal.regular);*/

        }


        @Override
        public void onClick(View v) {
            Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO, list.get(getAdapterPosition()).getLogo());
            Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_ID, "" + list.get((getAdapterPosition())).getSupplier_id());
            Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_BRANCH_ID, "" + list.get((getAdapterPosition())).getSupplier_branch_id());
            Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_NAME, "" + list.get((getAdapterPosition())).getSupplier_name());
            Bundle bundle = new Bundle();
            bundle.putInt("categoryId", list.get(getAdapterPosition()).getCategory_id());
            bundle.putString("title", list.get((getAdapterPosition())).getSupplier_name());
            ((MainActivity)mContext).supplierId = list.get((getAdapterPosition())).getSupplier_id();
            // ((MainActivity) mContext).supplierBranchId = list.get((getAdapterPosition())).getSupplier_branch_id();

            ((MainActivity)mContext).supplierImage = list.get((getAdapterPosition())).getLogo();
            ((MainActivity)mContext).supplierName = list.get((getAdapterPosition())).getSupplier_name();

            bundle.putInt("supplier_branch_id", list.get(getAdapterPosition()).getSupplier_branch_id());
            bundle.putInt("productId", list.get(getAdapterPosition()).getProduct_id());
            bundle.putString("title", list.get(getAdapterPosition()).getName());

            ProductDetails productDetails = new ProductDetails();
            bundle.putInt("offerType", 0);
            productDetails.setArguments(bundle);
            ((MainActivity)mContext).pushFragments(DataNames.TAB1,
                    productDetails, true, true, "", true);


        }
    }


}