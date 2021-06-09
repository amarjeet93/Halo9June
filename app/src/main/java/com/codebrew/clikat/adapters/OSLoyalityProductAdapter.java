package com.codebrew.clikat.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.OrderItemBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ProductLoyalityPoints;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;

/*
 * Created by cbl80 on 7/6/16.
 */
public class OSLoyalityProductAdapter extends RecyclerView.Adapter<OSLoyalityProductAdapter.View_holder> {
    private final List<ProductLoyalityPoints> list;
    private Context mContext;

    public OSLoyalityProductAdapter(Context context, List<ProductLoyalityPoints> cartList) {
        this.mContext = context;
        list=cartList;
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

        holder.tvItemName.setText(list.get(position).getName());
        holder.tvSize.setText("x"+1);
        holder.tvPrice.setText(""+list.get(position).getLoyaltyPoints()+" "+mContext.getString(R.string.points));
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        TextView tvPrice;
        TextView tvItemName;
        TextView tvSize;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public View_holder(View itemView) {
            super(itemView);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvItemName=itemView.findViewById(R.id.tvItemName);
            tvSize=itemView.findViewById(R.id.tvSize);

   /*         tvPrice.setTypeface(AppGlobal.regular);
            tvItemName.setTypeface(AppGlobal.regular);
            tvSize.setTypeface(AppGlobal.regular);
*/
        }
    }
}