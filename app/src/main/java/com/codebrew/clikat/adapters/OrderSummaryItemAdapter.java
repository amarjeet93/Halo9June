package com.codebrew.clikat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.OrderItemBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CartInfo;
import com.codebrew.clikat.modal.CartList;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;


/*
 * Created by cbl80 on 12/5/16.
 */
public class OrderSummaryItemAdapter extends RecyclerView.Adapter<OrderSummaryItemAdapter.View_holder> {
    private final List<CartInfo> list;
    private Context mContext;
    private String preSubCatName = "";

    public OrderSummaryItemAdapter(Context context, CartList cartList) {
        this.mContext = context;
        list = cartList.getCartInfos();
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        OrderItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.order_item, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {

        holder.tvItemName.setText(list.get(position).getProductName());
        holder.tvSize.setText("x" + list.get(position).getQuantity());
        holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext) + " " + list.get(position).getQuantity() * list.get(position).getPrice());
        holder.tvSubcategoryName.setText(list.get(position).getSubCategoryName());
        if ((Prefs.with(mContext).getInt(DataNames.FLOW_STROE,0))== DataNames.FLOW_LAUNDRY) {
            if (preSubCatName.equals(list.get(position).getSubCategoryName())) {
                holder.tvSubcategoryName.setVisibility(View.GONE);
            } else {
                holder.tvSubcategoryName.setVisibility(View.VISIBLE);
                preSubCatName = list.get(position).getSubCategoryName();
            }
        } else {
            holder.tvSubcategoryName.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        TextView tvPrice;
        TextView tvItemName;
        TextView tvSize;
        TextView tvSubcategoryName;

        public View_holder(View itemView) {
            super(itemView);

            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvItemName=itemView.findViewById(R.id.tvItemName);
            tvSize=itemView.findViewById(R.id.tvSize);
            tvSubcategoryName=itemView.findViewById(R.id.tvSubcategoryName);

/*            tvPrice.setTypeface(AppGlobal.regular);
            tvItemName.setTypeface(AppGlobal.regular);
            tvSize.setTypeface(AppGlobal.regular);
            tvSubcategoryName.setTypeface(AppGlobal.semi_bold);*/

        }
    }
}