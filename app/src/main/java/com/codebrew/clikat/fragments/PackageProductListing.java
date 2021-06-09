package com.codebrew.clikat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.adapters.PackageListingAdapter;
import com.codebrew.clikat.databinding.FragmentPackagetListingBinding;
import com.codebrew.clikat.modal.LocationUser;
import com.codebrew.clikat.modal.other.PackageProductListModel;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackageProductListing extends Fragment {
    private RecyclerView recyclerView;
    private View btnNoData;
    private int categoryId;
    private Integer supplierBranchId;
    private LocationUser locationUser;
    private PackageProductListModel exampleAllPackages;
    private PackageListingAdapter mAdapter;

    List<PackageProductListModel.DataBean.ListBean> data = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPackagetListingBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_packaget_listing, container, false);
        binding.setColor(Configurations.colors);
        binding.setDrawables(Configurations.drawables);
        binding.setStrings(Configurations.strings);
        View view = binding.getRoot();
        btnNoData = view.findViewById(R.id.noData);
        recyclerView = view.findViewById(R.id.recyclerview);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryId = ((MainActivity) getActivity()).categoryId;
        supplierBranchId = ((MainActivity) getActivity()).supplierBranchId;
        locationUser = Prefs.with(getActivity()).getObject(DataNames.LocationUser, LocationUser.class);
        // apiAllPackages(categoryId, languageId, supplierBranchId, locationUser.getAreaID());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (exampleAllPackages != null) {
            for (int i = 0; i < data.size(); i++) {
                Integer productId = data.get(i).getPackage_id();
                data.get(i).setQuantity(StaticFunction.INSTANCE.getCartQuantity(getActivity(), productId));
            }
            setdata(data);
        } else {
            if (StaticFunction.INSTANCE.isInternetConnected(getActivity())) {
             //   apiAllPackages(categoryId, supplierBranchId, locationUser.getAreaID());
            } else {
                StaticFunction.INSTANCE.showNoInternetDialog(getActivity());
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        String title = bundle.getString("title", getResources().getString(R.string.packages));
        ((MainActivity) getActivity()).tvTitleMain.setText(title);
        String image = Prefs.with(getActivity()).getString(DataNames.SUPPLIER_LOGO, "");
        final String supplierI = Prefs.with(getActivity()).getString(DataNames.SUPPLIER_LOGO_ID, "");
        final String supplierName = Prefs.with(getActivity()).getString(DataNames.SUPPLIER_LOGO_NAME, "");
        final String branchId = Prefs.with(getActivity()).getString(DataNames.SUPPLIER_LOGO_BRANCH_ID, "");
        ((MainActivity) getActivity()).setSupplierImage(true, image, Integer.parseInt(supplierI), supplierName, Integer.parseInt(branchId)
                , categoryId);
        if (exampleAllPackages != null) {
            for (int i = 0; i < exampleAllPackages.getData().getList().size(); i++) {
                Integer productId = data.get(i).getPackage_id();
                data.get(i).setQuantity(StaticFunction.INSTANCE.getCartQuantity(getActivity(), productId));
            }
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
        ((MainActivity) getActivity()).subCategoryIcon(true);
    }

    private void apiAllPackages(final int categoryId, int supplierBranchId, int areaId) {

        final ProgressBarDialog barDialog = new ProgressBarDialog(getActivity());
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("areaId", "" + areaId);
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(getActivity()));
        hashMap.put("categoryId", "" + categoryId);
        hashMap.put("supplierBranchId", "" + supplierBranchId);


        Call<PackageProductListModel> call = RestClient.getModalApiService(getActivity()).
                getAllPackages(hashMap);

        call.enqueue(new Callback<PackageProductListModel>() {
            @Override
            public void onResponse(Call<PackageProductListModel> call,
                                   Response<PackageProductListModel> response) {


                if (response.code() == 200) {
                    exampleAllPackages = response.body();
                    List<PackageProductListModel.DataBean.ListBean> data = exampleAllPackages.getData().getList();

                    for (int i = 0; i < data.size(); i++) {
                        Integer packageId = data.get(i).getPackage_id();
                        data.get(i).setQuantity(StaticFunction.INSTANCE.getCartQuantity(getActivity(), packageId));
                        data.get(i).setNetPrice((data.get(i).getPrice()));

                    }

                    Prefs.with(getActivity()).save(DataNames.PACKAGES, exampleAllPackages.getData());
                    barDialog.dismiss();
                    setdata(exampleAllPackages.getData().getList());
                } else {
                    barDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<PackageProductListModel> call, Throwable t) {
                barDialog.dismiss();
            }


        });


    }

    private void setdata(List<PackageProductListModel.DataBean.ListBean> data) {
        this.data = data;
        if (data.size() == 0) {
            btnNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            btnNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new PackageListingAdapter(getActivity(), this.data);
            recyclerView.setAdapter(mAdapter);
        }

    }

}