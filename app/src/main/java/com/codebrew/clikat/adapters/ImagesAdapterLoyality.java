package com.codebrew.clikat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.modal.Product_;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;

import java.util.List;

/*
 * Created by cbl80 on 2/9/16.
 */
public class ImagesAdapterLoyality extends RecyclerView.Adapter<ImagesAdapterLoyality.View_holder> {

    private Context mContext;
    private List<Product_> list;

    public ImagesAdapterLoyality(Context context, List<Product_> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_image_square,parent,false);
        return new View_holder(view);
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        int dimen= (int) (GeneralFunctions.getScreenWidth(mContext)/3.3);
        holder.itemView.getLayoutParams().width=dimen;
        holder.itemView.getLayoutParams().height=dimen;


        StaticFunction.INSTANCE.loadImage(list.get(position).getImage_path(),holder.sdvProduct,false);

        /*Glide.with(mContext)
                .load(list.get(position).getImage_path())
                .apply(new RequestOptions()
                        .override(160,160).fitCenter())
                .into(holder.sdvProduct);*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        ImageView sdvProduct;
        public View_holder(View itemView) {
            super(itemView);
            sdvProduct=itemView.findViewById(R.id.sdvProduct);
        }
    }
}