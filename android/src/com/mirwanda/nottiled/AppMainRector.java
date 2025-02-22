package com.mirwanda.nottiled;


import android.app.Application;

public class AppMainRector extends Application
{

    private static AppMainRector sInstance;

    public AppMainRector() {
        sInstance = this;
    }



    public static AppMainRector get() {
        return sInstance;
    }

    /*
    public Billing getBilling() {
        return mBilling;
    }

     */
}

