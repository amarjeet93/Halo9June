package com.codebrew.clikat.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.data.AppDataType;
import com.codebrew.clikat.databinding.IngredientViewBinding;
import com.codebrew.clikat.fragments.PackageSupplierListing;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CategoryFavourites;
import com.codebrew.clikat.modal.other.SettingModel;
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailFrag;
import com.codebrew.clikat.module.supplier_detail.SupplierDetailFragment;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.Dialogs.SelectCategory;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.Arrays;
import java.util.List;

/*
 * Created by cbl80 on 4/5/16.
 */
public class SelectCategoryAdapter extends RecyclerView.Adapter<SelectCategoryAdapter.ViewHolder> {
    private final SelectCategory dialog;
    private Context mContext;
    private List<CategoryFavourites> categoryList;

    public SelectCategoryAdapter(Context context, SelectCategory dialog, List<CategoryFavourites> categoryList) {
        this.dialog = dialog;
        this.mContext = context;
        this.categoryList = categoryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        IngredientViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ingredient_view, parent, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);

        return new ViewHolder(binding.getRoot());
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CategoryFavourites dataEnglish = categoryList.get(position);

        holder.tvName.setText(dataEnglish.getName());

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;


        public ViewHolder(View itemView) {
            super(itemView);

            tvName=itemView.findViewById(R.id.ingredient_textview);
       /*     tvName.setTypeface(AppGlobal.regular);*/


            itemView.setOnClickListener(v -> {

                dialog.dismiss();
                List<String> items = Arrays.asList(categoryList.get(getAdapterPosition()).getCategory_flow().split(">"));

                if (items.get(1).contains("PackageSL")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", categoryList.get(getAdapterPosition()).getName());
                    bundle.putInt(DataNames.DATA_NAMES_ORDER, categoryList.get(getAdapterPosition())
                            .getOrder());
                    ((MainActivity) mContext).flow_1 = DataNames.FLOW_PACKAGE;
                    ((MainActivity) mContext).placementPosition = DataNames.NO;

                    bundle.putInt("placement", DataNames.NO); // 1 for yes 0 for no
                    bundle.putInt("categoryId", categoryList.get(getAdapterPosition()).getCategory_id());
                    PackageSupplierListing packageFlowProductListing = new PackageSupplierListing();
                    packageFlowProductListing.setArguments(bundle);

                    ((MainActivity) mContext).
                            pushFragments(DataNames.TAB1, packageFlowProductListing,
                                    true, true, "", true);
                } else {
                    ((MainActivity) mContext).categoryId = categoryList.get(getAdapterPosition()).getCategory_id();
                    ((MainActivity) mContext).flow_1 = DataNames.FLOW_SUPPLIER_FIRST;

                    Bundle bundle = new Bundle();
                    bundle.putInt("categoryId", categoryList.get(getAdapterPosition()).getCategory_id());
                    bundle.putString("title", categoryList.get(getAdapterPosition()).getName());
                    bundle.putInt(DataNames.DATA_NAMES_ORDER, categoryList.get(getAdapterPosition()).getOrder());

                    SettingModel.DataBean.ScreenFlowBean screenFlowBean = Prefs.with(mContext).getObject(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean.class);
                    Fragment fragment;
                    if ( screenFlowBean.getApp_type() == AppDataType.Food.getType()) {
                        fragment = new RestaurantDetailFrag();
                    } else {
                        fragment = new SupplierDetailFragment();
                    }
                    fragment.setArguments(bundle);
                    ((MainActivity) mContext).
                            pushFragments(DataNames.TAB1,
                                    fragment,
                                    true, true, "supplierDetail", true);


                }
                Prefs.with(mContext).save(DataNames.FLOW_STROE, ((MainActivity) mContext).flow_1);
                Prefs.with(mContext).save(DataNames.CATEGORY_ID, String.valueOf(categoryList.get(getAdapterPosition())
                        .getCategory_id()));
            });

        }

    }
}
