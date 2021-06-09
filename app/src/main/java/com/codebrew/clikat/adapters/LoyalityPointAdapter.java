package com.codebrew.clikat.adapters;

import android.content.Context;
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
import com.codebrew.clikat.databinding.ItemFavBinding;
import com.codebrew.clikat.fragments.LoyalityPointFragment;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ProductLoyalityPoints;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by cbl80 on 16/5/16.
 */
public class LoyalityPointAdapter extends RecyclerView.Adapter<LoyalityPointAdapter.ViewHolder> {
    private final LoyalityPointFragment loyalityPointFragment;
    private Integer totalLoyalityPoints;
    private Context mContext;
    private List<ProductLoyalityPoints> list;
    private List<ProductLoyalityPoints> listAdded = new ArrayList<>();
    public int selectedSupplierBranchId = 0;
    private Button btnReedeem;

    public LoyalityPointAdapter(Context context, List<ProductLoyalityPoints> product, Integer totalLoyalityPoints, Button btnReedeem) {
        this.mContext = context;
        list = product;
        this.btnReedeem = btnReedeem;
        this.totalLoyalityPoints = totalLoyalityPoints;
        loyalityPointFragment = (LoyalityPointFragment) ((MainActivity) mContext).getSupportFragmentManager().findFragmentByTag("loyalityPoints");

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFavBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_fav, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        StaticFunction.INSTANCE.loadImage(list.get(position).getImagePath(),holder.sdvProduct,false);

       /* Glide.with(mContext)
                .load(list.get(position).getImagePath())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_product)
                        .centerCrop().override(150,150))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.sdvProduct);*/
        holder.tvProductName.setText(list.get(position).getName());
        holder.tvProductSupplier.setText(list.get(position).getSupplierName());
        holder.tvPrice.setText(list.get(position).getLoyaltyPoints() + " " + mContext.getString(R.string.points));

        if (list.get(position).getTick() != null && list.get(position).getTick()) {
            holder.ivTick.setVisibility(View.VISIBLE);
        } else {
            holder.ivTick.setVisibility(View.GONE);
        }
        if (getRedeemPointList().size() == 0) {
            btnReedeem.setVisibility(View.GONE);
        } else {
            btnReedeem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sdvProduct;
        TextView tvProductName;
        TextView tvProductSupplier;
        ImageView ivTick;
        TextView tvPrice;


        public ViewHolder(View itemView) {
            super(itemView);

            sdvProduct=itemView.findViewById(R.id.sdvProduct);
            tvProductName=itemView.findViewById(R.id.tvProductName);
            tvProductSupplier=itemView.findViewById(R.id.tvProductSupplier);
            ivTick=itemView.findViewById(R.id.ivTick);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);


   /*         tvProductName.setTypeface(AppGlobal.regular);
            tvProductSupplier.setTypeface(AppGlobal.regular);
            tvPrice.setTypeface(AppGlobal.regular);*/
            itemView.setOnClickListener(v -> {


                if (list.get(getAdapterPosition()).getSupplierBranchId() == selectedSupplierBranchId || selectedSupplierBranchId == 0 || getRedeemPointList().size() == 0) {
                    if (list.get(getAdapterPosition()).getTick() != null && list.get(getAdapterPosition()).getTick()) {
                        list.get(getAdapterPosition()).setTick(false);
                        totalLoyalityPoints = totalLoyalityPoints + list.get(getAdapterPosition()).getLoyaltyPoints();

                       // loyalityPointFragment.tvLoyalityPoints.setText(totalLoyalityPoints + "");

                    } else {
                        totalLoyalityPoints = totalLoyalityPoints - list.get(getAdapterPosition()).getLoyaltyPoints();

                        if (totalLoyalityPoints >= 0) {
                            selectedSupplierBranchId = list.get(getAdapterPosition()).getSupplierBranchId();
                            list.get(getAdapterPosition()).setTick(true);
                        } else {
                           // Snackbar.make(loyalityPointFragment.btnReedeem, mContext.getString(R.string.insufficientPoints), Snackbar.LENGTH_LONG).show();
                            totalLoyalityPoints = totalLoyalityPoints + list.get(getAdapterPosition()).getLoyaltyPoints();

                        }

                      //  loyalityPointFragment.tvLoyalityPoints.setText(totalLoyalityPoints + "");

                    }

                    notifyItemChanged(getAdapterPosition());
                } else {
                  //  StaticFunction.dialogue(mContext, mContext.getResources().getString(R.string.selectProductFromSameSupplier), "", false, -1);
                }
            });
        }
    }

    public List<ProductLoyalityPoints> getRedeemPointList() {

        listAdded.clear();
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getTick() != null && list.get(i).getTick()) {
                listAdded.add(list.get(i));
            }
        }
        return listAdded;
    }
}
