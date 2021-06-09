package com.codebrew.clikat.module.product_detail.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemFilterCheckcolorBinding;
import com.codebrew.clikat.databinding.ItemFilterChecksizeBinding;
import com.codebrew.clikat.modal.other.ProductDetailModel;
import com.codebrew.clikat.modal.other.VariantValuesBean;
import com.codebrew.clikat.module.product_detail.ProductDetails;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


public class ProductVarientListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final int SIZE = 1;
    private final int COLOR = 2;
    private String viewType;


    private List<VariantValuesBean> varientList = new ArrayList<>();
    private List<VariantValuesBean> varientFilteredList = new ArrayList<>();

    private FilterVarientCallback mVarientCallback;

    private int mposition;

    private boolean filterStatus;


    public void settingCallback(FilterVarientCallback mVarientCallback, String viewType, List<VariantValuesBean> mChecklist, int mposition) {

        varientFilteredList.clear();
        varientList.clear();
        varientList.addAll(mChecklist);

        this.mVarientCallback = mVarientCallback;
        this.viewType = viewType;

        this.varientFilteredList.addAll(varientList);

        this.mposition=mposition;

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (i == SIZE) {
            ItemFilterChecksizeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                    R.layout.item_filter_checksize, viewGroup, false);
            binding.setColor(Configurations.colors);
            return new ViewHolder(binding.getRoot());
        } else {
            ItemFilterCheckcolorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                    R.layout.item_filter_checkcolor, viewGroup, false);
            binding.setColor(Configurations.colors);
            return new ColorViewHolder(binding.getRoot());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        int pos = viewHolder.getAdapterPosition();


        if (viewHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) viewHolder;

            holder.tvName.setText(varientFilteredList.get(pos).getVariant_value());

            if (ProductDetails.Companion.getFilterData().get(mposition)==varientFilteredList.get(pos).getUnid()) {

                holder.tvName.setBackgroundColor(Color.parseColor(Configurations.colors.primaryColor));
                holder.tvName.setTextColor(Color.parseColor(Configurations.colors.appBackground));
                //checked
            } else {
                holder.tvName.setBackgroundColor(Color.parseColor(Configurations.colors.appBackground));
              //  holder.tvName.setStrokeWidth(2);
                holder.tvName.setStrokeColor(ColorStateList.valueOf(Color.parseColor(Configurations.colors.primaryColor)));
                holder.tvName.setTextColor(Color.parseColor(Configurations.colors.textHead));
                //unchecked
            }

        } else {
            ColorViewHolder colorViewHolder = (ColorViewHolder) viewHolder;

            colorViewHolder.tvShape.setBackground(StaticFunction.INSTANCE.varientColor(varientFilteredList.get(pos).getVariant_value(), Configurations.colors.primaryColor, GradientDrawable.RADIAL_GRADIENT));

            if (ProductDetails.Companion.getFilterData().get(mposition)==varientFilteredList.get(pos).getUnid())  {
                colorViewHolder.itemLayout.setBackground(StaticFunction.INSTANCE.varientColor(Configurations.colors.appBackground, Configurations.colors.primaryColor, GradientDrawable.RECTANGLE));
                //checked
            } else {
                colorViewHolder.itemLayout.setBackground(StaticFunction.INSTANCE.varientColor(Configurations.colors.appBackground, Configurations.colors.textSubhead, GradientDrawable.RECTANGLE));
                //unchecked
            }

            if(filterStatus && varientFilteredList.get(pos).isVisiblity_status())
            {
                colorViewHolder.itemLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                colorViewHolder.itemLayout.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return varientFilteredList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (viewType.equals("color"))
            return COLOR;
        else
            return SIZE;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<VariantValuesBean> filteredListNew = new ArrayList<>();
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredListNew.clear();
                    filteredListNew.addAll(varientList);
                } else {
                    List<VariantValuesBean> filteredList = new ArrayList<>();
                    for (VariantValuesBean row : varientList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match

                        if (row.getFilterstatus() != 1 && charString.contains(String.valueOf(row.getProduct_id()))) {
                            row.setFilter_product_id(String.valueOf(row.getProduct_id()));
                            row.setVisiblity_status(charString.contains(String.valueOf(row.getProduct_id())));
                            filteredList.add(row);
                        }

                    }
                    filteredListNew.clear();
                    if (filteredList.isEmpty()) {
                        filteredListNew.addAll(varientList);
                    }
                    else
                        filteredListNew.addAll(filteredList);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredListNew;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                varientFilteredList.clear();
                varientFilteredList.addAll(((List<VariantValuesBean>) filterResults.values));
                filterStatus=true;

                notifyDataSetChanged();
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        MaterialButton tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tv_name);

            tvName.setOnClickListener(v -> {
                ProductDetails.Companion.getFilterData().put(mposition,varientFilteredList.get(getAdapterPosition()).getUnid());

                if (varientFilteredList.get(getAdapterPosition()).getFilter_product_id()!=null)
                    mVarientCallback.onFilterVarient(String.valueOf(varientFilteredList.get(getAdapterPosition()).getFilter_product_id()),mposition);
                else
                    mVarientCallback.onFilterVarient(String.valueOf(varientFilteredList.get(getAdapterPosition()).getProduct_id()),mposition);

            });
        }
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {

        TextView tvShape;
        ConstraintLayout itemLayout;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);

            tvShape=itemView.findViewById(R.id.tv_shape);
            itemLayout=itemView.findViewById(R.id.itemLayout);

            itemLayout.setOnClickListener(v -> {
                ProductDetails.Companion.getFilterData().put(mposition,varientFilteredList.get(getAdapterPosition()).getUnid());

                if (varientFilteredList.get(getAdapterPosition()).getFilter_product_id()!=null)
                    mVarientCallback.onFilterVarient(String.valueOf(varientFilteredList.get(getAdapterPosition()).getFilter_product_id()),mposition);
                else
                    mVarientCallback.onFilterVarient(String.valueOf(varientFilteredList.get(getAdapterPosition()).getProduct_id()),mposition);
            });
        }
    }


    public interface FilterVarientCallback {
        void onFilterVarient(String varientId,int adpaterPosition);
    }
}
