package com.codebrew.clikat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemOrderBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.LoyalityOrders;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.configurations.TextConfig;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by cbl80 on 2/9/16.
 */
public class LoyalityOrderAdapter extends RecyclerView.Adapter<LoyalityOrderAdapter.View_holder> {
    private Context mContext;
    private ArrayList<LoyalityOrders> list;
    public Integer selectedOrderId;
    public int positionSelected;

    private int primaryColor= Color.parseColor(Configurations.colors.primaryColor);
    private int textHead=Color.parseColor(Configurations.colors.textHead);

    private TextConfig textConfig=Configurations.strings;

    public LoyalityOrderAdapter(Context context, List<LoyalityOrders> list1) {
        this.mContext = context;
        list = (ArrayList<LoyalityOrders>) list1;
    }


    public void remove() {
        list.remove(positionSelected);
        notifyItemRemoved(positionSelected);
        notifyItemRangeChanged(positionSelected, list.size());
    }


    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemOrderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_order, parent, false);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        LoyalityOrders orderHistory = list.get(position);
        holder.tvOrderNo.setText(mContext.getString(R.string.order_no,textConfig.order) + "\n" + orderHistory.getOrder_id());
        holder.rvImages.setAdapter(new ImagesAdapterLoyality(mContext, list.get(position).getProduct()));
        String s = orderHistory.getTotal_points() + " " + mContext.getString(R.string.points) + " . " + orderHistory.getProduct().size() + " " + mContext.getString(R.string.items);
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
            deliveryDate = setColor(mContext.getString(R.string.expected_delivered_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDeliveryDate.setText(deliveryDate);

        holder.tvStatus.setText(StaticFunction.INSTANCE.statusProduct(list.get(position).getStatus(), 0,-1, mContext, ""));

        holder.tvStatus.setTextColor(primaryColor);

    }

    private SpannableString setColor(String string) {
        SpannableString newString = new SpannableString(string);
        int index = string.indexOf("\n") + 1;
        newString.setSpan(new ForegroundColorSpan(textHead), index, string.length(),
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
        TextView tvOrder1;
        public CardView cvContainer;

        public View_holder(View itemView) {
            super(itemView);

            rvImages=itemView.findViewById(R.id.rvImages);
            tvStatus=itemView.findViewById(R.id.tvStatus);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvPlaced=itemView.findViewById(R.id.tvPlaced);
            tvOrderNo=itemView.findViewById(R.id.tvOrderNo);
            tvDeliveryDate=itemView.findViewById(R.id.tvDeliveryDate);
            tvOrder1=itemView.findViewById(R.id.tvOrder);
            cvContainer=itemView.findViewById(R.id.cvContainer);


            rvImages.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

  /*          tvStatus.setTypeface(AppGlobal.semi_bold);
            tvPrice.setTypeface(AppGlobal.semi_bold);
            tvPlaced.setTypeface(AppGlobal.regular);
            tvOrderNo.setTypeface(AppGlobal.regular);
            tvDeliveryDate.setTypeface(AppGlobal.regular);*/

            tvOrder1.setVisibility(View.GONE);
        }
    }


}