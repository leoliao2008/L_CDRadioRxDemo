package com.skycaster.l_cdradiorxdemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;

/**
 * Created by 廖华凯 on 2016/12/20.
 */
public class AlertDialogueUtils {

    private static AlertDialog dialog;

    public static void showDialogue(Activity context,String msg, final Runnable positive, final Runnable negative){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
//        dialog = builder.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                positive.run();
//                dialog.dismiss();
//            }
//        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                negative.run();
//                dialog.dismiss();
//            }
//        }).create();
        LinearLayout rootView= (LinearLayout) View.inflate(context, R.layout.widget_alert_dialogue,null);
        TextView message= (TextView) rootView.findViewById(R.id.widget_alert_dialogue_tv_message);
        Button confirm= (Button) rootView.findViewById(R.id.widget_alert_dialogue_btn_confirm);
        Button cancel= (Button) rootView.findViewById(R.id.widget_alert_dialogue_btn_cancel);
        message.setText(msg);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positive.run();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                negative.run();
                dialog.dismiss();
            }
        });
        builder.setView(rootView);
        dialog=builder.create();
        dialog.show();
    }
}
