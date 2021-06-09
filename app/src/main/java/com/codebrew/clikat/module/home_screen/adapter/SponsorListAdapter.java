package com.codebrew.clikat.module.home_screen.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.data.AppDataType;
import com.codebrew.clikat.databinding.ItemSponsorBinding;
import com.codebrew.clikat.databinding.ItemSponsorVerticalBinding;
import com.codebrew.clikat.modal.other.SettingModel;
import com.codebrew.clikat.modal.other.SupplierInArabicBean;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;
import java.util.Objects;


public class SponsorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private SponsorDetail mCallback;

    private List<SupplierInArabicBean> sponsorList;
    private Context mContext;
    private int mScreenType;
 private SettingModel.DataBean.SettingData clientInfom;
    private static int TYPE_HOR = 1;
    private static int TYPE_VERT = 2;
    private Integer  parentPos;

    SponsorListAdapter(List<SupplierInArabicBean> sponsorList, int mScreenType, SettingModel.DataBean.SettingData clientInfom,
                       Integer parentPos) {
        this.sponsorList = sponsorList;
        this.mScreenType = mScreenType;
        this.clientInfom=clientInfom;
        this.parentPos=parentPos;
    }

    public void settingCallback(SponsorDetail mCallback) {
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        if (viewType == TYPE_HOR) {
            ItemSponsorBinding bindingHor = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_sponsor, viewGroup, false);
            bindingHor.setColor(Configurations.colors);
            return new HorizonatlViewHolder(bindingHor);

        } else {
            ItemSponsorVerticalBinding bindingVert = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_sponsor_vertical, viewGroup, false);
            bindingVert.setColor(Configurations.colors);
            return new VerticalViewHolder(bindingVert);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int pos = holder.getAdapterPosition();
        if (getItemViewType(position) == TYPE_VERT) {
            VerticalViewHolder vertViewHolder = (VerticalViewHolder) holder;
            vertViewHolder.onBindVert(sponsorList.get(holder.getAdapterPosition()));
            if(mScreenType!=AppDataType.Food.getType() && clientInfom!=null &&
                    Objects.equals(clientInfom.is_supplier_wishlist(), "1")){
                vertViewHolder.ivWishlist.setVisibility(View.VISIBLE);
            }else{
                vertViewHolder.ivWishlist.setVisibility(View.GONE);
            }

            if(Objects.equals(sponsorList.get(pos).getFavourite() , 1)){
                vertViewHolder.ivWishlist.setImageResource(R.drawable.ic_favourite);
            }else{
                vertViewHolder.ivWishlist.setImageResource(R.drawable.ic_unfavorite);
            }

            vertViewHolder.itemView.setOnClickListener(v -> {
                //  viewHolder.itemView.setEnabled(false);
                mCallback.onSponsorDetail(sponsorList.get(pos));
            });

            vertViewHolder.ivWishlist.setOnClickListener(v -> {
                //  viewHolder.itemView.setEnabled(false);
                mCallback.onSponsorWishList(sponsorList.get(pos),parentPos);
            });

        } else {
            HorizonatlViewHolder horViewHolder = (HorizonatlViewHolder) holder;

            StaticFunction.INSTANCE.loadImage(sponsorList.get(pos).getLogo(), horViewHolder.ivProduct, false);
           // StaticFunction.INSTANCE.loadImage(sponsorList.get(pos).getLogo(), horViewHolder.ivImage, false);

            horViewHolder.grpSupplier.setVisibility(mScreenType == AppDataType.Ecom.getType() || mScreenType == 5 ? View.VISIBLE : View.GONE);

            if (mScreenType == AppDataType.Ecom.getType() || mScreenType == 5) {
                horViewHolder.rbSponsor.setRating(Float.valueOf(sponsorList.get(pos).getRating()));
                horViewHolder.tvRating.setText(mContext.getString(R.string.reviews_text, sponsorList.get(pos).getTotal_reviews()));
                horViewHolder.tvSponsorLocation.setText(sponsorList.get(pos).getAddress());

                StaticFunction.INSTANCE.loadImage(sponsorList.get(pos).getLogo(), horViewHolder.ivProduct, false);
            }
            if(mScreenType!=AppDataType.Food.getType() && clientInfom!=null &&
                    Objects.equals(clientInfom.is_supplier_wishlist(), "1")){
                horViewHolder.ivWishlist.setVisibility(View.VISIBLE);
            }else{
                horViewHolder.ivWishlist.setVisibility(View.GONE);
            }

            if(Objects.equals(sponsorList.get(pos).getFavourite() , 1)){
                horViewHolder.ivWishlist.setImageResource(R.drawable.ic_favourite);
            }else{
                horViewHolder.ivWishlist.setImageResource(R.drawable.ic_unfavorite);
            }

            horViewHolder.tvSponsorName.setText(sponsorList.get(pos).getName());
            horViewHolder.tvBottomSpsrName.setText(sponsorList.get(pos).getName());

            // viewHolder.itemView.setEnabled(true);

            horViewHolder.itemView.setOnClickListener(v -> {
                //  viewHolder.itemView.setEnabled(false);
                mCallback.onSponsorDetail(sponsorList.get(pos));
            });

            horViewHolder.ivWishlist.setOnClickListener(v -> {
                //  viewHolder.itemView.setEnabled(false);
                mCallback.onSponsorWishList(sponsorList.get(pos),parentPos);
            });

            if (mScreenType > AppDataType.Custom.getType()) {
                horViewHolder.tvBottomSpsrName.setVisibility(View.VISIBLE);
                horViewHolder.clMain.setVisibility(View.GONE);
                horViewHolder.ivImage.setVisibility(View.VISIBLE);
            }
            else {
                horViewHolder.clMain.setVisibility(View.VISIBLE);
                horViewHolder.tvBottomSpsrName.setVisibility(View.GONE);
                horViewHolder.ivImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return sponsorList.size();
    }

    public interface SponsorDetail {
        void onSponsorDetail(SupplierInArabicBean supplier);
        void onSponsorWishList(SupplierInArabicBean supplier,Integer parentPos);
    }


    @Override
    public int getItemViewType(int position) {
        if (mScreenType == AppDataType.HomeServ.getType() || mScreenType == AppDataType.Beauty.getType()) {
            return TYPE_VERT;
        } else {
            return TYPE_HOR;

        }
    }

    public class HorizonatlViewHolder extends RecyclerView.ViewHolder {


        private ItemSponsorBinding bindingHor;
        private View itemView;
        RoundedImageView ivImage;
        ImageView ivProduct;
        TextView tvSponsorName;
        TextView tvRating;
        RatingBar rbSponsor;
        TextView tvSponsorLocation;
        Group grpSupplier;
        TextView tvBottomSpsrName;
        ImageView ivWishlist;
        CardView clMain;

        public HorizonatlViewHolder(@NonNull ItemSponsorBinding bindingHor) {
            super(bindingHor.getRoot());
            this.bindingHor = bindingHor;

            itemView = bindingHor.getRoot();

            ivProduct = itemView.findViewById(R.id.iv_supplier);
            tvSponsorName = itemView.findViewById(R.id.tv_sponsor_name);
            tvRating = itemView.findViewById(R.id.tv_rating);
            rbSponsor = itemView.findViewById(R.id.rb_rating);
            tvSponsorLocation = itemView.findViewById(R.id.tv_sponsor_adrs);
            tvBottomSpsrName = itemView.findViewById(R.id.tvBottomSpsrName);
            grpSupplier = itemView.findViewById(R.id.gp_suplr);
            clMain=itemView.findViewById(R.id.clMain);
            ivWishlist = itemView.findViewById(R.id.iv_wishlist);
            ivImage=itemView.findViewById(R.id.ivSupplierCustom);
        }
    }

    public class VerticalViewHolder extends RecyclerView.ViewHolder {

        private ItemSponsorVerticalBinding bindingVert;
        private View itemView;
        ImageView sdvImage;
        TextView name;
        TextView location;
        TextView rating;
        ImageView ivWishlist;

        public VerticalViewHolder(@NonNull ItemSponsorVerticalBinding bindingVert) {
            super(bindingVert.getRoot());
            this.bindingVert = bindingVert;
            itemView = bindingVert.getRoot();

            ivWishlist = itemView.findViewById(R.id.iv_wishlist);
            sdvImage = itemView.findViewById(R.id.sdvImage);
            name = itemView.findViewById(R.id.tvName);
            location = itemView.findViewById(R.id.tvSupplierloc);
            rating = itemView.findViewById(R.id.tv_rating);
        }

        private void onBindVert(SupplierInArabicBean list) {
            bindingVert.setSponsorlist(list);
        }
    }


}
