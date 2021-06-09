package com.codebrew.clikat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.databinding.ItemSubSearchItemBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CartInfo;
import com.codebrew.clikat.modal.Result;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;


/*
 * Created by cbl80 on 6/5/16.
 */
public class SearchSubAdapter extends RecyclerView.Adapter<SearchSubAdapter.View_holder> {
    private Context mContext;
    private List<Result> results;

    private int textSubHead= Color.parseColor(Configurations.colors.textListSubhead);

    public SearchSubAdapter(Context context, List<Result> results) {
        this.mContext = context;
        this.results = results;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemSubSearchItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_sub_search_item, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        holder.itemView.getLayoutParams().width = (int) (GeneralFunctions.getScreenWidth(mContext) / 3.2);
        holder.sdvProduct.getLayoutParams().width = (int) (GeneralFunctions.getScreenWidth(mContext) / 3.2);
        holder.sdvProduct.getLayoutParams().height = (int) (GeneralFunctions.getScreenWidth(mContext) / 3.2);

        Result res = results.get(position);


        StaticFunction.INSTANCE.loadImage(res.image_path,holder.sdvProduct,false);

      /*  Glide.with(mContext)
                .load(Uri.parse(res.image_path))
                .apply(new RequestOptions()
                        .fitCenter())
                .into(holder.sdvProduct);*/
        holder.tvProductName.setText(res.name);
        holder.tvCount.setText(String.valueOf(res.quantity));


        if (res.price_type== 1) {
            String s = res.netPrice + " " + StaticFunction.INSTANCE.getCurrency(mContext);
            Spannable st = new SpannableString(s
                    + "\n" + mContext.getString(R.string.per_hour));
            st.setSpan(new RelativeSizeSpan(0.8f), s.length(), st.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            st.setSpan(new ForegroundColorSpan(textSubHead), s.length(), st.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvPrice.setText(st);
        } else {
            holder.tvPrice.setText(res.netPrice + " " + StaticFunction.INSTANCE.getCurrency(mContext));
        }

    }


    @Override
    public int getItemCount() {
        return results.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {


        TextView tvProductName;
        TextView tvPrice;
        ImageView sdvProduct;
        ImageView ivMinus;
        ImageView ivPlus;
        TextView tvCount;

        public View_holder(View itemView) {
            super(itemView);

            tvCount=itemView.findViewById(R.id.tvCount);
            tvProductName=itemView.findViewById(R.id.tvProductName);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            sdvProduct=itemView.findViewById(R.id.sdvProduct);
            ivMinus=itemView.findViewById(R.id.ivMinus);
            ivPlus=itemView.findViewById(R.id.ivPlus);

    /*        tvProductName.setTypeface(AppGlobal.regular);
            tvPrice.setTypeface(AppGlobal.regular);
            tvCount.setTypeface(AppGlobal.regular);*/

            ivPlus.setOnClickListener(v -> {
                if (getAdapterPosition() != -1) {
                    String supplierBranchId = Prefs.with(mContext).getString(DataNames.SUPPLIERBRANCHID, "");
                    String catId = Prefs.with(mContext).getString(DataNames.CATEGORY_ID_TEST, "");
                    String catIDMain = String.valueOf(((MainActivity) mContext).categoryId);
                    if ((StaticFunction.INSTANCE.cartCount(mContext, Prefs.with(mContext)
                            .getInt(DataNames.FLOW_STROE,0)) != 0 && catId!=null && !catId.isEmpty() && !catId.equals(catIDMain))
                    ||(StaticFunction.INSTANCE.cartCount(mContext, Prefs.with(mContext)
                            .getInt(DataNames.FLOW_STROE,0)) != 0 && supplierBranchId != null && !supplierBranchId.isEmpty() && !supplierBranchId.equals("" + (((MainActivity) mContext).supplierBranchId)))) {
                      //  StaticFunction.clearCartDialog(mContext, true, true,this);
                    } else {
                        Prefs.with(mContext).save(DataNames.CATEGORY_ID_TEST, "" + catIDMain);
                        Prefs.with(mContext).save(DataNames.CATEGORY_ID, "" + (((MainActivity) mContext).categoryId));
                        Prefs.with(mContext).save(DataNames.SUPPLIERBRANCHID, "" + (((MainActivity) mContext).supplierBranchId));
                        if (results.get(getAdapterPosition()).quantity == 0) {
                            CartInfo cartInfo = new CartInfo();
                            cartInfo.setQuantity(1);
                            cartInfo.setProductName(results.get(getAdapterPosition()).name);
                            cartInfo.setProductId(results.get(getAdapterPosition()).id);
                            cartInfo.setSupplierName(results.get(getAdapterPosition()).supplier_name);
                            cartInfo.setImagePath(results.get(getAdapterPosition()).image_path);
                            cartInfo.setPrice(results.get(getAdapterPosition()).netPrice);
                            cartInfo.setSuplierBranchId(((MainActivity) mContext).supplierBranchId);
                            cartInfo.setMeasuringUnit(results.get(getAdapterPosition()).measuring_unit);
                            cartInfo.setDeliveryCharges(results.get(getAdapterPosition()).delivery_charges);
                            cartInfo.setSupplierId(((MainActivity) mContext).supplierId);
                            cartInfo.setUrgent_type(results.get(getAdapterPosition()).urgent_type);
                            cartInfo.setUrgent(results.get(getAdapterPosition()).can_urgent);
                            cartInfo.setUrgentValue(results.get(getAdapterPosition()).urgent_value);
                            cartInfo.setHandlingAdmin(results.get(getAdapterPosition()).handling_admin);
                            cartInfo.setHandlingSupplier(results.get(getAdapterPosition()).handling_supplier);
                            cartInfo.setPriceType(results.get(getAdapterPosition()).price_type);
                            cartInfo.setHourlyPrice(results.get(getAdapterPosition()).hourly_price);
                           // cartInfo.setCategoryId(results.get(getAdapterPosition()).ca);

                            cartInfo.setHandlingCharges((results.get(getAdapterPosition()).handling_admin + results.get(getAdapterPosition()).handling_supplier));
                            switch ((Prefs.with(mContext).getInt(DataNames.FLOW_STROE,0))) {
                                case DataNames.FLOW_BEAUTY_SALOON:
                                    StaticFunction.INSTANCE.addToCartSallonHome(mContext, cartInfo, true, true, ((MainActivity) mContext).supplierImage, ((MainActivity) mContext).supplierName);
                                    break;
                                case DataNames.FLOW_BEAUTY_SALOON_PLACE:
                                    StaticFunction.INSTANCE.addToCartSallonPlace(mContext, cartInfo, true, true, ((MainActivity) mContext).supplierImage, ((MainActivity) mContext).supplierName);
                                    break;
                                default:
                                    StaticFunction.INSTANCE.addToCart(mContext, cartInfo);
                                    break;
                            }

                            results.get(getAdapterPosition()).quantity=1;
                        } else {
                            Integer quantity = results.get(getAdapterPosition()).quantity;
                            quantity++;
                            results.get(getAdapterPosition()).quantity=quantity;
                            if (results.get(getAdapterPosition()).price_type != 0) {
                                for (int i = 0; i < results.get(getAdapterPosition()).hourly_price.size(); i++) {
                                    if (quantity >= results.get(getAdapterPosition()).hourly_price.get(i).getMin_hour() && quantity <= results.get(getAdapterPosition()).hourly_price.get(i).getMax_hour()) {
                                        results.get(getAdapterPosition()).netPrice=((results.get(getAdapterPosition()).hourly_price.get(i).getPrice_per_hour()));
                                    }
                                }
                            }
                            switch (Prefs.with(mContext).getInt(DataNames.FLOW_STROE,0)) {
                                case DataNames.FLOW_BEAUTY_SALOON:
                                    StaticFunction.INSTANCE.updateCartSallonHome(mContext, results.get(getAdapterPosition()).id, quantity, true, true,
                                            results.get(getAdapterPosition()).netPrice);
                                    break;
                                case DataNames.FLOW_BEAUTY_SALOON_PLACE:
                                    StaticFunction.INSTANCE.updateCartSallonPlace(mContext, results.get(getAdapterPosition()).id, quantity, true, true,
                                            results.get(getAdapterPosition()).netPrice);
                                    break;
                                default:
                                    StaticFunction.INSTANCE.updateCart(mContext, results.get(getAdapterPosition()).id, quantity,
                                            results.get(getAdapterPosition()).netPrice);
                                    break;
                            }
                        }

                        notifyItemChanged(getAdapterPosition());
                        notifyDataSetChanged();
                    }
                }

            });

            ivMinus.setOnClickListener(v -> {

                if (getAdapterPosition() != -1) {
                    if (results.get(getAdapterPosition()).quantity != 0) {
                        int quantity = results.get(getAdapterPosition()).quantity;

                        if (quantity > 0) {
                            quantity--;
                            results.get(getAdapterPosition()).quantity=(quantity);
                            if (results.get(getAdapterPosition()).price_type != 0) {
                                for (int i = 0; i < results.get(getAdapterPosition()).hourly_price.size(); i++) {
                                    quantity = results.get(getAdapterPosition()).quantity;
                                    if (quantity >= results.get(getAdapterPosition()).hourly_price.get(i).getMin_hour() && quantity <= results.get(getAdapterPosition()).hourly_price.get(i).getMax_hour()) {
                                        results.get(getAdapterPosition()).netPrice=((results.get(getAdapterPosition()).hourly_price.get(i).getPrice_per_hour()));
                                    }
                                }
                            }
                            if (quantity == 0) {
                                switch (Prefs.with(mContext).getInt(DataNames.FLOW_STROE,0)) {
                                    case DataNames.FLOW_BEAUTY_SALOON:
                                        StaticFunction.INSTANCE.removeFromCartSaloonHome(mContext, results.get(getAdapterPosition()).id, true, true);
                                        break;
                                    case DataNames.FLOW_BEAUTY_SALOON_PLACE:
                                        StaticFunction.INSTANCE.removeFromCartSallonPlace(mContext, results.get(getAdapterPosition()).id, true, true);
                                        break;
                                    default:
                                        StaticFunction.INSTANCE.removeFromCart(mContext, results.get(getAdapterPosition()).id, 0);
                                        break;
                                }
                                notifyItemChanged(getAdapterPosition());
                            } else {
                                switch (Prefs.with(mContext).getInt(DataNames.FLOW_STROE,0)) {
                                    case DataNames.FLOW_BEAUTY_SALOON:
                                        StaticFunction.INSTANCE.updateCartSallonHome(mContext, results.get(getAdapterPosition()).id, quantity, true, true,
                                                results.get(getAdapterPosition()).netPrice);
                                        break;
                                    case DataNames.FLOW_BEAUTY_SALOON_PLACE:
                                        StaticFunction.INSTANCE.updateCartSallonPlace(mContext, results.get(getAdapterPosition()).id, quantity, true, true,
                                                results.get(getAdapterPosition()).netPrice);
                                        break;
                                    default:
                                        StaticFunction.INSTANCE.updateCart(mContext, results.get(getAdapterPosition()).id, quantity,
                                                results.get(getAdapterPosition()).netPrice);
                                        break;
                                }

                                notifyItemChanged(getAdapterPosition());
                            }
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            sdvProduct.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("productId", results.get(getAdapterPosition()).id);
                bundle.putString("title", results.get(getAdapterPosition()).name);
                bundle.putInt("offerType",0);
                String catId=Prefs.with(mContext).getString(DataNames.CATEGORY_ID,"0");
                int cat=0;
                try {
                    cat= Integer.parseInt(catId);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                bundle.putInt("categoryId",cat);
                ProductDetails productDetails = new ProductDetails();
                productDetails.setArguments(bundle);
                ((MainActivity) mContext).pushFragments(DataNames.TAB1,
                        productDetails, true, true, "", true);
            });

        }
    }
}