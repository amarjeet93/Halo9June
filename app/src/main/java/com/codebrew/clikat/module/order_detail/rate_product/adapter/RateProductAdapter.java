package com.codebrew.clikat.module.order_detail.rate_product.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemRateProductBinding;
import com.codebrew.clikat.modal.other.ProductDataBean;
import com.codebrew.clikat.data.model.others.RateProductListModel;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.customviews.ClikatTextView;

import java.util.List;


public class RateProductAdapter extends RecyclerView.Adapter<RateProductAdapter.ViewHolder> {



    private List<RateProductListModel> rateProductList;

    private RateCallback mCallback;
    private Context context;


    public RateProductAdapter(List<RateProductListModel> rateProductList) {
        this.rateProductList = rateProductList;
    }

    public void settingCallback(RateCallback mCallback)
    {
        this.mCallback=mCallback;
    }

    @NonNull
    @Override
    public RateProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context=viewGroup.getContext();

        ItemRateProductBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.item_rate_product, viewGroup, false);
        binding.setColor(Configurations.colors);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RateProductAdapter.ViewHolder viewHolder, int i) {

        int pos = viewHolder.getAdapterPosition();

        viewHolder.tvProdName.setText(rateProductList.get(pos).getProdName());

        viewHolder.tvProdSupplier.setText(context.getString(R.string.supplier_tag,rateProductList.get(pos).getSupplierName()));

        StaticFunction.INSTANCE.loadImage(rateProductList.get(pos).getProdImage(),viewHolder.ivProdImage, false);

       // Glide.with(context).load(rateProductList.get(pos).getProdImage()).into(viewHolder.ivProdImage);


        viewHolder.rbReview.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> rateProductList.get(pos).setValue(String.valueOf( rating)));

    }

    @Override
    public int getItemCount() {
        return rateProductList.size();
    }

    public interface RateCallback{

        void rateProduct(int position,RateProductListModel rateProduct);
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProdImage;
        ClikatTextView tvProdName;
        ClikatTextView tvProdSupplier;
        RatingBar rbReview;
        EditText edHead;
        EditText edDescription;
        Button btnRateProd;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProdImage=itemView.findViewById(R.id.iv_prod_image);
            tvProdName=itemView.findViewById(R.id.tv_prod_name);
            tvProdSupplier=itemView.findViewById(R.id.tv_prod_supplier);
            rbReview=itemView.findViewById(R.id.rb_review);
            edHead=itemView.findViewById(R.id.ed_head);
            edDescription=itemView.findViewById(R.id.ed_description);
            btnRateProd=itemView.findViewById(R.id.btn_rate_prod);

            btnRateProd.setOnClickListener(view -> {
                rateProductList.get(getAdapterPosition()).setTitle(edHead.getText().toString());
                rateProductList.get(getAdapterPosition()).setReviews(edDescription.getText().toString());
                rateProductList.get(getAdapterPosition()).setValue( String.valueOf(rbReview.getRating()));
                mCallback.rateProduct(getAdapterPosition(),rateProductList.get(getAdapterPosition()));
            });
        }

    }

}
