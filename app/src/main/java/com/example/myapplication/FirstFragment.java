package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.annotation.SuppressLint;

import java.util.concurrent.atomic.AtomicBoolean;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.view.SurfaceView;

import com.example.myapplication.ImageProcessing;

import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {
    private static int gx;
    private static int j;

    private static double flag = 1;

    private String title = "pulse";

    private int addX = -1;


    //Android手机预览控件
    //预览设置信息
    private static SurfaceHolder previewHolder = null;
    //Android手机相机句柄
    private static Camera camera = null;
    //private static View image = null;
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    public static enum TYPE {
        GREEN, RED
    };
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    //设置默认类型
    private static TYPE currentType = TYPE.GREEN;
    //获取当前类型
    public static TYPE getCurrent() {
        return currentType;
    }
    //心跳下标值
    private static int beatsIndex = 0;
    //心跳数组的大小
    private static final int beatsArraySize = 3;
    //心跳数组
    private static final int[] beatsArray = new int[beatsArraySize];
    //心跳脉冲
    private static double beats = 0;
    //开始时间
    private static long startTime = 0;

    private static TextView mTV_Avg_Pixel_Values = null;

    private static SurfaceView preview = null;

    //private static View image = null;
    private static TextView mTV_Heart_Rate = null;
    private static TextView mTV_pulse = null;



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.heartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }


}