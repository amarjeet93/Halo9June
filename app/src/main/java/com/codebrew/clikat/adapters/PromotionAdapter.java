package com.codebrew.clikat.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.databinding.ItemFavBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.other.PromotionListModel;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;


/*
 * Created by cbl80 on 2/6/16.
 */
public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.View_holder> {
    private Context mContext;
    private List<PromotionListModel.DataBean.ListBean> list;

    public PromotionAdapter(Context context, List<PromotionListModel.DataBean.ListBean> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFavBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_fav, parent, false);
       // binding.setColor(Configurations.colors);
      //  binding.setDrawables(Configurations.drawables);
       // binding.setStrings(Configurations.strings);
        return new View_holder(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        PromotionListModel.DataBean.ListBean product = list.get(position);

        StaticFunction.INSTANCE.loadImage(list.get(position).getPromotion_image(),holder.sdvProduct,false);

   /*     Glide.with(mContext)
                .load(list.get(position).getPromotion_image())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_product)
                        .fitCenter().override(150,150))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.sdvProduct);*/
        holder.tvProductName.setText(product.getPromotion_name());
        holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + product.getPromotion_price() + "");

        holder.itemView.setEnabled(true);

        holder.itemView.setOnClickListener(v -> {
            holder.itemView.setEnabled(false);
            itemDetail(holder.getAdapterPosition());
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductSupplier;
        ImageView sdvProduct;
        TextView tvPrice;
        ImageView ivTick;

        public View_holder(View itemView) {
            super(itemView);

            tvProductName=itemView.findViewById(R.id.tvProductName);
            tvProductSupplier=itemView.findViewById(R.id.tvProductSupplier);
            sdvProduct=itemView.findViewById(R.id.sdvProduct);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            ivTick=itemView.findViewById(R.id.ivTick);


/*            tvProductName.setTypeface(AppGlobal.regular);
            tvProductSupplier.setTypeface(AppGlobal.semi_bold);
            tvProductSupplier.setVisibility(View.GONE);
            tvPrice.setTypeface(AppGlobal.regular);*/
            ivTick.setVisibility(View.GONE);

        }
    }

    private void itemDetail(int position) {

        //String supplierBranchId = Prefs.with(mContext).getString(DataNames.SUPPLIERBRANCHID, "");
        //String catId=Prefs.with(mContext).getString(DataNames.CATEGORY_ID_TEST,"");
        //String catiDMain= String.valueOf(list.get(position).getCategory_id());
               /* if ((StaticFunction.cartCount(mContext, 0) > 0&& catId!=null&&
                !catId.isEmpty()&& !catId.equals(catiDMain))||(StaticFunction.cartCount(mContext, 0) > 0 && !supplierBranchId.isEmpty() && !supplierBranchId.equalsIgnoreCase("" + list.get(position).getSupplier_branch_id()))) {
                    StaticFunction.clearCartDialog(mContext, true, true);
                } else {*/
        Prefs.with(mContext).save(DataNames.DATA_PROMOTION, list.get(position));
        Bundle bundle = new Bundle();
        Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO, list.get(position).getLogo());
        Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_ID, "" + list.get((position)).getSupplier_id());
        Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_BRANCH_ID, "" + list.get((position)).getSupplier_branch_id());
        Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_NAME, "" + list.get((position)).getSupplier_name());
        bundle.putInt("categoryId", list.get(position).getCategory_id());
        bundle.putString("title", list.get((position)).getSupplier_name());
        ((MainActivity) mContext).supplierId = list.get((position)).getSupplier_id();
        ((MainActivity) mContext).supplierBranchId = list.get((position)).getSupplier_branch_id();

        ((MainActivity) mContext).supplierImage = list.get((position)).getPromotion_image();
        ((MainActivity) mContext).supplierName = list.get((position)).getSupplier_name();
        bundle.putInt("productId", list.get(position).getId());
        bundle.putString("title", list.get(position).getPromotion_name());
        bundle.putInt("supplier_branch_id",list.get(position).getSupplier_branch_id());
        ProductDetails productDetails = new ProductDetails();
        bundle.putInt("offerType", 3);
        productDetails.setArguments(bundle);
        ((MainActivity) mContext).pushFragments(DataNames.TAB1,
                productDetails, true, true, "", true);
        // }
    }
}