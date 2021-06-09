package com.codebrew.clikat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.adapters.NotificationAdapter;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.Notification;
import com.codebrew.clikat.modal.PojoNotification;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ClikatConstants;
import com.codebrew.clikat.utils.ConnectionDetector;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by cbl80 on 4/5/16.
 */
public class NotificationFragment extends BaseOrderFrag {
    private List<Notification> notif=new ArrayList<>();
    private NotificationAdapter mAdapter;
    private ProgressBarDialog barDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IntentFilter filter = new IntentFilter("Broadcastnotification");

        MyReceiver receiver = new MyReceiver();
        getActivity().registerReceiver(receiver, filter);
        barDialog = new ProgressBarDialog(getActivity());
        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // callback for drag-n-drop, false to skip this feature
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // callback for swipe to dismiss, removing item from data and adapter

//                StaticFunction.saveCart(getActivity(),cartList);
                if (StaticFunction.INSTANCE.isInternetConnected(getActivity())) {
                    clearSingleNoificationApi(notif.get(viewHolder.getAdapterPosition()).getId());
                    notif.remove(viewHolder.getAdapterPosition());
                    mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    mAdapter.notifyItemChanged(notif.size());
                    if (mAdapter.getItemCount() == 0) {
                    //    noData.setVisibility(View.VISIBLE);
                      //  rvOrders.setVisibility(View.GONE);
                    }
                } else {
                    StaticFunction.INSTANCE.showNoInternetDialog(getActivity());
                }

            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

        });
       // swipeToDismissTouchHelper.attachToRecyclerView(rvOrders);

    }

    private void clearAllNotiApi() {

        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessToken", StaticFunction.INSTANCE.getAccesstoken(getActivity()));

        Call<ExampleCommon> notificationCall = RestClient.getModalApiService(getActivity()).clearAllNotification(hashMap);


        notificationCall.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                barDialog.dismiss();
                if (response.code() == 200) {
                    notif.clear();
                    mAdapter.notifyDataSetChanged();
                   // noData.setVisibility(View.VISIBLE);
                   // rvOrders.setVisibility(View.GONE);
                    ExampleCommon pojoPromotion = response.body();
                    GeneralFunctions.showSnackBar(getView(), pojoPromotion.getMessage(), getActivity());
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                barDialog.dismiss();
            }
        });
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                clearAllNotiApi();
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
       // ((MainActivity) getActivity()).setNotiIcon();
       // ((MainActivity) getActivity()).tbShare.setVisibility(View.GONE);
        //((MainActivity)getActivity()).setSupplierImage(false,"",0,"",0,0);
        mAdapter=new NotificationAdapter(getActivity(),notif);
        setAdapter(mAdapter, getResources().getString(R.string.notifications), getString(R.string.no_notification));
        if (notif != null && notif.size()!=0) {
            setdata();
        } else {
            if (StaticFunction.INSTANCE.isInternetConnected(getActivity())) {
                getNoificationApi();
            } else {
                StaticFunction.INSTANCE.showNoInternetDialog(getActivity());
            }
        }
    }


    private void getNoificationApi() {
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessToken", StaticFunction.INSTANCE.getAccesstoken(getActivity()));

        Call<PojoNotification> notificationCall = RestClient.getModalApiService(getActivity()).callNotification(hashMap);


        notificationCall.enqueue(new Callback<PojoNotification>() {
            @Override
            public void onResponse(Call<PojoNotification> call, Response<PojoNotification> response) {
                barDialog.dismiss();
                if (response.code() == 200) {
                    PojoNotification pojoPromotion = response.body();
                    if (pojoPromotion.getStatus() == ClikatConstants.STATUS_SUCCESS) {
                        notif = pojoPromotion.getData().getNotification();
                        setdata();
                    } else if (pojoPromotion.getStatus() == ClikatConstants.STATUS_INVALID_TOKEN)
                    {
                        new ConnectionDetector(getActivity()).loginExpiredDialog();
                    }else{
                        GeneralFunctions.showSnackBar(getView(), pojoPromotion.getMessage(), getActivity());
                    }
                }
            }

            @Override
            public void onFailure(Call<PojoNotification> call, Throwable t) {
                barDialog.dismiss();
            }
        });
    }

    private void clearSingleNoificationApi(int id) {
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap <>();
        hashMap.put("accessToken", StaticFunction.INSTANCE.getAccesstoken(getActivity()));
        hashMap.put("notificationId", "" + id);

        Call<ExampleCommon> notificationCall = RestClient.getModalApiService(getActivity()).clearOneNotification(hashMap);


        notificationCall.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                barDialog.dismiss();
                if (response.code() == 200) {
                    ExampleCommon pojoPromotion = response.body();
                    GeneralFunctions.showSnackBar(getView(), pojoPromotion.getMessage(), getActivity());
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                barDialog.dismiss();
            }
        });
    }

    private void setdata() {
        mAdapter = new NotificationAdapter(getActivity(), notif);
        setAdapter(mAdapter, getResources().getString(R.string.notifications), getString(R.string.no_notification));
    }
}
