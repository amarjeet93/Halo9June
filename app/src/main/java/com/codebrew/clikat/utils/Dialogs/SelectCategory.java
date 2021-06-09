package com.codebrew.clikat.utils.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.adapters.SelectCategoryAdapter;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.CategoryFavourites;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Created by cbl80 on 9/5/16.
 */
public class SelectCategory extends Dialog   {
    private final List<CategoryFavourites> categoryList;
    private Context context;
    private RecyclerView recyclerView;

    public SelectCategory(Context context, List<CategoryFavourites> category)
    {
        super(context, R.style.TransparentDilaog);
        this.context = context;
        this.categoryList =category;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_category);
        CardView cvView= findViewById(R.id.cvView);
        //cvView.getLayoutParams().width= GeneralFunctions.getScreenWidth(context);
        recyclerView = findViewById(R.id.rvParent_home);
        TextView tvTitle= findViewById(R.id.tvTitle);
      //  tvTitle.setTypeface(AppGlobal.regular);
        ImageView ivCross= findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dismiss());
        intialize();
    }

    private void intialize()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SelectCategoryAdapter(context,this, categoryList));
    }


}
