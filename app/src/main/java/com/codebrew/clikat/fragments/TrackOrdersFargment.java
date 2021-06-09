package com.codebrew.clikat.fragments;

import android.os.Bundle;
import android.view.View;


import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.adapters.TrackAdapter;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.PojoSignUp;
import com.codebrew.clikat.modal.other.TrackOrderList;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ConnectionDetector;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.configurations.TextConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Ankit Jindal on 20/4/16.
 */
public class TrackOrdersFargment extends BaseOrderFrag {
    private TrackOrderList exampleOrderHistory;
    private TrackAdapter trackAdapter;
    private List<TrackOrderList.DataBean.OrderListBean> orderHistory2List;

    private TextConfig textConfig = Configurations.strings;

    @Override
    public void onStart() {
        super.onStart();
    }


    private void getTrackOrders() {
        final ProgressBarDialog barDialog = new ProgressBarDialog(getActivity());
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        PojoSignUp signUp = Prefs.with(getActivity()).getObject(DataNames.USER_DATA, PojoSignUp.class);
        hashMap.put("accessToken", signUp.data.access_token);
        hashMap.put("languageId", StaticFunction.INSTANCE.getLanguage(getActivity()) + "");

        Call<TrackOrderList> historyCall = RestClient.getModalApiService(getActivity()).trackOrderList(hashMap);

        historyCall.enqueue(new Callback<TrackOrderList>() {
            @Override
            public void onResponse(Call<TrackOrderList> call, Response<TrackOrderList> response) {
                barDialog.dismiss();
                if (response.code() == 200 && response.body().getStatus() == 200) {
                    exampleOrderHistory = response.body();

                    if (exampleOrderHistory != null && exampleOrderHistory.getData() != null) {


                        orderHistory2List = new ArrayList<>();

                        if (exampleOrderHistory.getData().getOrderList() != null)
                            orderHistory2List.addAll(exampleOrderHistory.getData().getOrderList());

                        trackAdapter = new TrackAdapter(getActivity(), orderHistory2List);
                        //rvOrders.setAdapter(trackAdapter);
                        setdata();


                    }

                } else if (response.body().getStatus() == 401) {
                    new ConnectionDetector(getActivity()).loginExpiredDialog();
                }
            }

            @Override
            public void onFailure(Call<TrackOrderList> call, Throwable t) {
                barDialog.dismiss();
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (exampleOrderHistory != null) {
            setdata();
        } else if (StaticFunction.INSTANCE.isInternetConnected(getActivity())) {
            getTrackOrders();
        } else {
            StaticFunction.INSTANCE.showNoInternetDialog(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).tvTitleMain.setText(getString(R.string.track_my_order, textConfig.order));
        ((MainActivity)getActivity()).tbShare.setVisibility(View.GONE);
        ((MainActivity)getActivity()).seticonsSidepnael();
        ((MainActivity)getActivity()).setSupplierImage(false, "", 0, "", 0, 0);
    }

    private void setdata() {
        setAdapter(trackAdapter, getString(R.string.track_my_order, textConfig.order), getString(R.string.track_noti));
    }


    public void apiTrackOrder() {
        final ProgressBarDialog barDialog = new ProgressBarDialog(getActivity());
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        PojoSignUp pojoLoginData = StaticFunction.INSTANCE.isLoginProperly(getActivity());
        hashMap.put("accessToken", pojoLoginData.data.access_token);
        // hashMap.put("orderId", "" + StaticFunction.getCartId(trackAdapter.selectedOrderId));
        hashMap.put("orderId", "" + trackAdapter.selectedOrderId);
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(getActivity()));
        Call<ExampleCommon> call = RestClient.getModalApiService(getActivity()).
                trackOrder(hashMap);
        call.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                barDialog.dismiss();
                ExampleCommon exampleAllCategories = response.body();
                if (response.code() == 200) {
                    StaticFunction.INSTANCE.sweetDialogueSuccess(getActivity(), getString(R.string.success), exampleAllCategories.getMessage(), false, 101, null, null);
                    orderHistory2List.get(trackAdapter.selectedPostion).setStatus(7);
                    trackAdapter.notifyDataSetChanged();
                } else {
                 //   Snackbar.make(llContainer, exampleAllCategories.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                barDialog.dismiss();
            }
        });

    }


}
