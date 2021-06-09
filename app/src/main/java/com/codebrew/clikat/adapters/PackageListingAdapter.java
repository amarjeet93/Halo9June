package com.codebrew.clikat.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.databinding.ItemProductListBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CartInfo;
import com.codebrew.clikat.modal.other.PackageProductListModel;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import java.util.List;


public class PackageListingAdapter extends RecyclerView.Adapter<PackageListingAdapter.ViewHolder> {
    private Context mContext;
    private List<PackageProductListModel.DataBean.ListBean> list;


    public PackageListingAdapter(Context context, List<PackageProductListModel.DataBean.ListBean> data) {
        this.mContext = context;
        this.list = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemProductListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_product_list, parent, false);
      //  binding.setColor(Configurations.colors);
       // binding.setDrawables(Configurations.drawables);
       // binding.setStrings(Configurations.strings);
       // binding.setSingleVndorType();
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PackageProductListModel.DataBean.ListBean data = list.get(position);
        holder.tvName.setText(list.get(position).getName());
        holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + data.getPrice() + "");


        StaticFunction.INSTANCE.loadImage(data.getImage_path(),holder.sdvImage,false);

   /*     Glide.with(mContext).load(Uri.parse(data.getImage_path()))
                .apply(new RequestOptions().placeholder(R.drawable.placeholder_product).override(100, 100))
                .into(holder.sdvImage);*/

        if (data.getQuantity() !=0) {
            holder.tvCount.setText(0 + "");
        } else {
            holder.tvCount.setText(data.getQuantity() + "");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView tvPrice;
        TextView tvCount;
        TextView tvName;
        ImageView sdvImage;
        ImageView ivPlus;
        ImageView ivMinus;
        TextView tvActualPrice;

        public ViewHolder(View itemView) {
            super(itemView);

            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvCount=itemView.findViewById(R.id.tvCount);
            tvName=itemView.findViewById(R.id.tvName);
            sdvImage=itemView.findViewById(R.id.sdvImage);
            ivPlus=itemView.findViewById(R.id.ivPlus);
            ivMinus=itemView.findViewById(R.id.ivMinus);
            tvActualPrice=itemView.findViewById(R.id.tvActualPrice);


  /*          tvPrice.setTypeface(AppGlobal.regular);
            tvName.setTypeface(AppGlobal.regular);
            tvCount.setTypeface(AppGlobal.regular);*/
            tvActualPrice.setVisibility(View.GONE);
            ivPlus.setOnClickListener(v -> {
                String supplierBranchId = Prefs.with(mContext).getString(DataNames.SUPPLIERBRANCHID, "");
                String catId = Prefs.with(mContext).getString(DataNames.CATEGORY_ID_TEST, "");
                String catIDMain = Prefs.with(mContext).getString(DataNames.PACKAGE_CATEGORY_ID, "");
                if ((StaticFunction.INSTANCE.cartCount(mContext, 0) != 0 && catId != null
                        && !catId.isEmpty() && !catId.equals(catIDMain)) || (StaticFunction.INSTANCE.cartCount(mContext, 0) != 0 && supplierBranchId != null && !supplierBranchId.isEmpty() && !supplierBranchId.equals("" + (((MainActivity) mContext).supplierBranchId)))) {
                  //  StaticFunction.clearCartDialog(mContext, true, true,this);
                } else {
                    Prefs.with(mContext).save(DataNames.CATEGORY_ID_TEST, "" + catIDMain);
                    Prefs.with(mContext).save(DataNames.CATEGORY_ID, "" + (((MainActivity) mContext).categoryId));
                    Prefs.with(mContext).save(DataNames.SUPPLIERBRANCHID, "" + (((MainActivity) mContext).supplierBranchId));

                    if (list.get(getAdapterPosition()).getQuantity() == 0) {
                        CartInfo cartInfo = new CartInfo();
                        cartInfo.setQuantity(1);
                        cartInfo.setPackageType(0);
                        cartInfo.setProductName(list.get(getAdapterPosition()).getName());
                        cartInfo.setProductId(list.get(getAdapterPosition()).getPackage_id());
                        cartInfo.setImagePath(list.get(getAdapterPosition()).getImage_path());
                        cartInfo.setPrice(list.get(getAdapterPosition()).getPrice());
                       // cartInfo.setCategoryId(list.get(getAdapterPosition()));
                        cartInfo.setSuplierBranchId(((MainActivity) mContext).supplierBranchId);
                        cartInfo.setMeasuringUnit(list.get(getAdapterPosition()).getMeasuring_unit());
                        cartInfo.setDeliveryCharges(list.get(getAdapterPosition()).getDelivery_charges());
                        cartInfo.setSupplierId(((MainActivity) mContext).supplierId);
                        cartInfo.setHandlingAdmin(list.get(getAdapterPosition()).getHandlingAdmin());
                        cartInfo.setHandlingSupplier(list.get(getAdapterPosition()).getHandling_supplier());
                        cartInfo.setHandlingCharges((list.get(getAdapterPosition()).getHandlingAdmin() + list.get(getAdapterPosition()).getHandling_supplier()));
                        StaticFunction.INSTANCE.
                                addToCart(mContext, cartInfo);
                        list.get(getAdapterPosition()).setQuantity(1);
                    } else {
                        Integer quantity = list.get(getAdapterPosition()).getQuantity();
                        quantity++;
                        list.get(getAdapterPosition()).setQuantity(quantity);
                        StaticFunction.INSTANCE.updateCart(mContext, list.get(getAdapterPosition()).getPackage_id(), quantity, list.get(getAdapterPosition()).getPrice());
                    }

//                        Prefs.with(mContext).save(DataNames.PACKAGES, list);
                    notifyItemChanged(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });

            ivMinus.setOnClickListener(v -> {
                if (list.get(getAdapterPosition()).getQuantity() != 0) {
                    Integer quantity = list.get(getAdapterPosition()).getQuantity();

                    if (quantity > 0) {
                        quantity--;
                        list.get(getAdapterPosition()).setQuantity(quantity);

                        if (quantity == 0) {
                            StaticFunction.INSTANCE.removeFromCart(mContext, list.get(getAdapterPosition()).getPackage_id(), 0);
                            notifyItemChanged(getAdapterPosition());
                        } else {
                            StaticFunction.INSTANCE.updateCart(mContext, list.get(getAdapterPosition()).getPackage_id(), quantity, list.get(getAdapterPosition()).getPrice());
                            notifyItemChanged(getAdapterPosition());
                        }
                        notifyDataSetChanged();
                    }
                }
            });


            tvName.setOnClickListener(v -> sdvImage.performClick());

            sdvImage.setOnClickListener(v -> {

                Bundle bundle = new Bundle();
                bundle.putInt("productId", list.get(getAdapterPosition()).getPackage_id());
                bundle.putString("title", list.get(getAdapterPosition()).getName());
                bundle.putInt("offerType", 2);
                String catID = Prefs.with(mContext).getString(DataNames.CATEGORY_ID, "0");
                int cat = 0;
                try {
                    cat = Integer.parseInt(catID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bundle.putInt("categoryId", cat);


                //bundle.putInt("categoryId", list.get(getAdapterPosition()).getCategory_id());
                bundle.putInt("supplier_branch_id",list.get(getAdapterPosition()).getSupplier_branch_id());

                ProductDetails productDetails = new ProductDetails();
                productDetails.setArguments(bundle);
                ((MainActivity) mContext).pushFragments(DataNames.TAB1,
                        productDetails, true, true, "", true);
            });

        }


    }
}
