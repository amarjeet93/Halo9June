package com.codebrew.clikat.module.subcategory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemSubcategoryBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.other.SubCategoryData;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;


/*
 * Created by Harman Sandhu .
 */
public class SubCategorySelectAdapter extends RecyclerView.Adapter<SubCategorySelectAdapter.ViewHolder> {

    private List<SubCategoryData> list;

    private SubCategoryCallback mCallback;

    public SubCategorySelectAdapter( List<SubCategoryData> list) {
        this.list=list;
    }

    public void settingCallback(SubCategoryCallback mCallback)
    {
        this.mCallback=mCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ItemSubcategoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_subcategory, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        SubCategoryData currentData = list.get(position);
       // holder.tvCategoryDesc.setText(currentData.getDescription());
        holder.tvCategoryName.setText(currentData.getName());


        StaticFunction.INSTANCE.loadImage(currentData.getImage(),holder.simpleDraweeView,false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvCategoryName;
    /*    @BindView(R.id.tvCategoryDesc)
        TextView tvCategoryDesc;*/

        ImageView simpleDraweeView;

        public ViewHolder(View itemView) {
            super(itemView);

            tvCategoryName=itemView.findViewById(R.id.tvCategoryName);
            simpleDraweeView = itemView.findViewById(R.id.sdvProduct);

           // tvCategoryDesc.setTypeface(AppGlobal.regular);
            tvCategoryName.setTypeface(AppGlobal.regular);

            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            mCallback.onSubCategoryDtail(list.get(getAdapterPosition()));
        }
    }

    public interface SubCategoryCallback
    {
        void onSubCategoryDtail(SubCategoryData  dataBean);
    }

}
