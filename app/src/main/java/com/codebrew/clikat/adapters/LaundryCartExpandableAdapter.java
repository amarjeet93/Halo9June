package com.codebrew.clikat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemLaundryOrderClothBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.LaundryProduct;
import com.codebrew.clikat.utils.StaticFunction;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by cbl80 on 5/5/16.
 */
public class LaundryCartExpandableAdapter extends RecyclerView.Adapter<LaundryCartExpandableAdapter.ViewHolder> {
    private Context mContext;
    private List<LaundryProduct> products = new ArrayList<>();

    private LaundaryCallback mCallback;

    public LaundryCartExpandableAdapter(Context context, List<LaundryProduct> product) {
        this.mContext = context;
        this.products.clear();
        this.products.addAll(product);

    }

    public void settingCallback(LaundaryCallback mCallback)
    {
        this.mCallback=mCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ItemLaundryOrderClothBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_laundry_order_cloth, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LaundryProduct product = products.get(position);
        holder.tvClothes.setText(product.getName());
        float pri;
        if (product.getQuantity() == null) {
            holder.tvCount.setText("" + 0);
            pri = product.getNetPrice();
            product.setQuantity(0);
        } else {
            holder.tvCount.setText("" + product.getQuantity());
            pri = product.getNetPrice();
        }
        holder.tvPrice.setText(StaticFunction.INSTANCE.getCurrency(mContext)+" " + pri);


        StaticFunction.INSTANCE.loadImage(product.getImage_path(),holder.ivProduct,false);

     /*   Glide.with(mContext)
                .load(product.getImage_path())
                .apply(new RequestOptions()
                        .override(90,90)
                        .fitCenter())
                .into(holder.ivProduct);*/
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClothes;
        TextView tvPrice;
        TextView tvCount;
        ImageView ivMinus;
        ImageView ivPlus;
        ImageView ivProduct;


        public ViewHolder(View itemView) {
            super(itemView);

            tvClothes=itemView.findViewById(R.id.tvClothes);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvCount=itemView.findViewById(R.id.tvCount);
            ivMinus=itemView.findViewById(R.id.ivMinus);
            ivPlus=itemView.findViewById(R.id.ivPlus);
            ivProduct=itemView.findViewById(R.id.ivProduct);

  /*          tvClothes.setTypeface(AppGlobal.regular);
            tvPrice.setTypeface(AppGlobal.regular);
            tvCount.setTypeface(AppGlobal.regular);*/

            ivMinus.setOnClickListener(v -> mCallback.removeProduct(getAdapterPosition()));
            ivPlus.setOnClickListener(v -> mCallback.addProduct(getAdapterPosition()));
        }

    }

    public interface LaundaryCallback{

        void addProduct(int position);
        void removeProduct(int position);
    }



    public LaundryProduct removeItem(int position) {
        final LaundryProduct model = products.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, LaundryProduct model) {
        products.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final LaundryProduct model = products.remove(fromPosition);
        products.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<LaundryProduct> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<LaundryProduct> newModels) {
        for (int i = products.size() - 1; i >= 0; i--) {
            final LaundryProduct model = products.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<LaundryProduct> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final LaundryProduct model = newModels.get(i);
            if (!products.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<LaundryProduct> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final LaundryProduct model = newModels.get(toPosition);
            final int fromPosition = products.indexOf(model);
            if (fromPosition >= 0) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

}
