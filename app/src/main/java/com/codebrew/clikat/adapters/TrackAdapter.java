package com.codebrew.clikat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.data.constants.AppConstants;
import com.codebrew.clikat.databinding.ItemOrderBinding;
import com.codebrew.clikat.fragments.TrackOrdersFargment;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.other.TrackOrderList;
import com.codebrew.clikat.module.order_detail.OrderDetailActivity;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.configurations.TextConfig;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/*
 * Created by cbl80 on 20/4/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.View_holder> {
    private Context mContext;
    private List<TrackOrderList.DataBean.OrderListBean> list;
    public int selectedOrderId;
    public int selectedPostion;
    private TextConfig textConfig=Configurations.strings;

    private int tabUnselected= Color.parseColor(Configurations.colors.tabUnSelected);

    public TrackAdapter(Context context, List<TrackOrderList.DataBean.OrderListBean> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemOrderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_order, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        TrackOrderList.DataBean.OrderListBean orderHistory = list.get(position);
        holder.rvImages.setAdapter(new ImagesAdapter( list.get(position).getProduct(),mContext));
        String s = (orderHistory.getNet_amount() + orderHistory.getHandling_admin() + orderHistory.getDelivery_charges() + orderHistory.getHandling_supplier()) + " " + AppConstants.Companion.getCURRENCY_SYMBOL() + " . " + orderHistory.getProduct().size() + " " + mContext.getString(R.string.items);
        holder.tvPrice.setText(s);


        String dd = orderHistory.getCreated_on().replace("T", " ");
        dd = dd.replace("Z", "");
        SpannableString deliveryDate = SpannableString.valueOf("");
        try {
            deliveryDate = setColor(mContext.getString(R.string.placed_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvPlaced.setText(deliveryDate);


        SpannableString order = setColor(mContext.getString(R.string.order_no,textConfig.order) + "\n" + orderHistory.getOrder_id());
        holder.tvOrderNo.setText(order);


        dd = orderHistory.getService_date().replace("T", " ");
        dd = dd.replace("Z", "");

        deliveryDate = SpannableString.valueOf("");
        try {
            deliveryDate = setColor(mContext.getString(R.string.expected_delivery_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDeliveryDate.setText(deliveryDate);


        holder.tvStatus.setText(StaticFunction.INSTANCE.statusProduct(list.get(position).getStatus(), 0,-1, mContext, ""));


        if (orderHistory.getStatus() == DataNames.TRACKING_ON) {
            holder.tvOrder.setVisibility(View.GONE);
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.light_green));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            holder.tvOrder.setVisibility(View.VISIBLE);
            holder.tvOrder.setBackgroundResource(R.drawable.yellow_rec);
            holder.tvOrder.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            holder.tvOrder.setText(mContext.getResources().getString(R.string.track));
        }

    }


    private SpannableString setColor(String string) {
        SpannableString newString = new SpannableString(string);
        int index = string.indexOf("\n") + 1;
        newString.setSpan(new ForegroundColorSpan(tabUnselected), index, string.length(),
                0);
        return newString;
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        RecyclerView rvImages;
        TextView tvStatus;
        TextView tvPrice;
        TextView tvPlaced;
        TextView tvOrderNo;
        TextView tvDeliveryDate;
        TextView tvOrder;

        public View_holder(View itemView) {
            super(itemView);

            rvImages=itemView.findViewById(R.id.rvImages);
            tvStatus=itemView.findViewById(R.id.tvStatus);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvPlaced=itemView.findViewById(R.id.tvPlaced);
            tvOrderNo=itemView.findViewById(R.id.tvOrderNo);
            tvDeliveryDate=itemView.findViewById(R.id.tvDeliveryDate);
            tvOrder=itemView.findViewById(R.id.tvOrder);


            rvImages.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

/*            tvStatus.setTypeface(AppGlobal.semi_bold);
            tvPrice.setTypeface(AppGlobal.semi_bold);
            tvPlaced.setTypeface(AppGlobal.regular);
            tvOrderNo.setTypeface(AppGlobal.regular);
            tvDeliveryDate.setTypeface(AppGlobal.regular);
            tvOrder.setTypeface(AppGlobal.semi_bold);*/


            tvOrder.setOnClickListener(v -> {

                selectedOrderId = list.get(getAdapterPosition()).getOrder_id();
                selectedPostion = getAdapterPosition();
                TrackOrdersFargment trackOrdersFargment = (TrackOrdersFargment) ((MainActivity) mContext).getSupportFragmentManager().findFragmentByTag("order");
                trackOrdersFargment.apiTrackOrder();
            });

            itemView.setOnClickListener(v -> {

                TrackOrderList.DataBean.OrderListBean history2 = list.get(getAdapterPosition());
                history2.setDeliveredOn(history2.getService_date());

                ArrayList<Integer> orderId=new ArrayList<>();

                orderId.add(history2.getOrder_id());

                Prefs.with(mContext).save(DataNames.ORDER_DETAIL, history2);
                mContext.startActivity(new Intent(mContext, OrderDetailActivity.class)
                        .putExtra(DataNames.REORDER_BUTTON, false).putIntegerArrayListExtra("orderId",orderId));
            });


        }
    }
}
