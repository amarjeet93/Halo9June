package com.codebrew.clikat.retrofit;


import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/*
 * Created by Ankit jindal on 19/12/2015.
 */
public class RetrofitUtils {

    public static RequestBody stringToRequestBody(String string) {

        return RequestBody.create(
                MediaType.parse("multipart/form-data"), string);
    }

    public static MultipartBody.Part imageToRequestBody(File file, String fieldName) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name

        return MultipartBody.Part.createFormData(fieldName, file.getName(), requestFile);
    }




}

