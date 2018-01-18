package com.mega.sos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentContactSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "EMERContactSettingActivity";
    private TextView mCallContact, mSmsContactOne, mSmsContactTwo, mSmsContactThree;
    private final int PICK_SMS_CONTACT_CODE_1 = 1;
    private final int PICK_SMS_CONTACT_CODE_2 = 2;
    private final int PICK_SMS_CONTACT_CODE_3 = 3;
    private SharedPreferences sp = null;
    private Context mContext;

    private View mNitifitylayout1, mNitifitylayout2;
    private TextView mStartTv, mNextTv;
    private boolean isFirstStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_contact_setting);
        final android.support.v7.app.ActionBar ab = getSupportActionBar();
       // ab.setHomeAsUpIndicator(R.mipmap.ic_actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        mContext = this;
        mSmsContactOne = (TextView) findViewById(R.id.current_contact_one);
        mSmsContactOne.setOnClickListener(this);

        mSmsContactTwo = (TextView) findViewById(R.id.current_contact_two);
        mSmsContactTwo.setOnClickListener(this);

        mSmsContactThree = (TextView) findViewById(R.id.current_contact_three);
        mSmsContactThree.setOnClickListener(this);

        mNitifitylayout1 = findViewById(R.id.register_layout);
        mNitifitylayout1.setVisibility(View.GONE);
        mNitifitylayout2 = findViewById(R.id.notification_layout);
        mNitifitylayout2.setVisibility(View.GONE);
        mStartTv = (TextView) findViewById(R.id.start_tv) ;
        mStartTv.setOnClickListener(this);
        mNextTv = (TextView) findViewById(R.id.next_tv);
        mNextTv.setOnClickListener(this);

        isFirstStart = Settings.Global.getInt(mContext.getContentResolver(),
                SosUtil.COM_MEGA_SOS_FIRST_START, 0) == 0;
        if(isFirstStart){
            mNitifitylayout1.setVisibility(View.VISIBLE);
        }
        checkReadPermission();
        getDataFromSp();
    }

    private void getDataFromSp(){
        sp = getSharedPreferences(Utils.SHARE_ANEM,  Context.MODE_PRIVATE);

        String smsContactone = sp.getString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME1, null);
        if(smsContactone == null){
            mSmsContactOne.setText("");
            setButtonBackground(mSmsContactOne);
        }else {
            mSmsContactOne.setText(smsContactone);
            resetButtonBackground(mSmsContactOne);
        }
        String smsContacttwo = sp.getString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME2, null);
        if(smsContacttwo == null){
            mSmsContactTwo.setText("");
            setButtonBackground(mSmsContactTwo);
        }else {
            mSmsContactTwo.setText(smsContacttwo);
            resetButtonBackground(mSmsContactTwo);
        }
        String smsContactthree = sp.getString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME3, null);
        if(smsContactthree == null){
            mSmsContactThree.setText("");
            setButtonBackground(mSmsContactThree);
        }else {
            mSmsContactThree.setText(smsContactthree);
            resetButtonBackground(mSmsContactThree);
        }
    }

    private void setButtonBackground(View view){
       // view.setBackground(mContext.getResources().getDrawable(R.drawable.add_contacts_btn_bg));
        view.setBackgroundResource(R.drawable.add_contacts_btn_bg);
    }
    private void resetButtonBackground(View view){
        view.setBackgroundResource(R.drawable.contact_bg_normal);
        //view.setPadding(0, 0,0, 0);
    }

    private void checkReadPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkReadContactsPermission = checkSelfPermission( Manifest.permission.READ_CONTACTS);
            Log.i("MainActivity","checkperdmissons ");
            if (checkReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i("    ","requestPermissions");
                requestPermissions( new String[]{Manifest.permission.CALL_PHONE,
                        Manifest.permission.WRITE_CONTACTS ,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION ,
                        Manifest.permission.READ_SMS}, 1);
            }

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // ContentProvider展示数据类似一个单个数据库表
            // ContentResolver实例带的方法可实现找到指定的ContentProvider并获取到ContentProvider的数据
            ContentResolver reContentResolverol = getContentResolver();
            // URI,每个ContentProvider定义一个唯一的公开的URI,用于指定到它的数据集
            Uri contactData = data.getData();
            // 查询就是输入URI等参数,其中URI是必须的,其他是可选的,如果系统能找到URI对应的ContentProvider将返回一个Cursor对象.
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            // 获得DATA表中的名字
            String username = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            // 条件为联系人ID
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            // 获得DATA表中的电话号码，条件为联系人ID,因为手机号码可能会有多个
            Cursor phone = reContentResolverol.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                            + contactId, null, null);
            String usernumber = null;
            while (phone.moveToNext()) {
                usernumber = phone
                        .getString(phone
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            //Log.i(TAG,"chen username = "+username+"  number = "+usernumber);
            if(usernumber == null){
                Toast.makeText(this,"请重新选择一个有号码的联系人",Toast.LENGTH_SHORT).show();
                return;
            }
            if(requestCode == PICK_SMS_CONTACT_CODE_1 || requestCode == PICK_SMS_CONTACT_CODE_2
                    || requestCode == PICK_SMS_CONTACT_CODE_3){
                String sms1 = mSmsContactOne.getText().toString();
                String sms2 = mSmsContactTwo.getText().toString();
                String sms3 = mSmsContactThree.getText().toString();
                if(username.equals(sms1) || username.equals(sms2) ||
                        username.equals(sms3)){
                    showAllReadyHaveDialog();
                    return;
                }

            }

            SharedPreferences.Editor ed = sp.edit();
            switch (requestCode){

                case PICK_SMS_CONTACT_CODE_1:
                    mSmsContactOne.setText(username);
                    resetButtonBackground(mSmsContactOne);
//                    mSmsContactOne.setBackground(mContext.getDrawable(R.drawable.call_cotact_bg_normal));
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME1, username);
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER1, usernumber);
                    break;
                case PICK_SMS_CONTACT_CODE_2:
                    mSmsContactTwo.setText(username);
                    resetButtonBackground(mSmsContactTwo);
//                    mSmsContactTwo.setBackground(mContext.getDrawable(R.drawable.call_cotact_bg_normal));
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME2, username);
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER2, usernumber);
                    break;
                case PICK_SMS_CONTACT_CODE_3:
                    mSmsContactThree.setText(username);
                    resetButtonBackground(mSmsContactThree);
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NAME3, username);
                    ed.putString(SosUtil.SHARESP_CURR_SMS_CONTACT_NUMBER3, usernumber);
                    break;
            }
           // ed.commit();
            ed.apply();
            if(isFirstStart){
                mNitifitylayout2.setVisibility(View.VISIBLE);
            }
    }
}

    private void showAllReadyHaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.already_contact_text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showChangeDialog(final int code){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.change_contact_text);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivityForResult(code);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){

            case R.id.current_contact_one:
                if(mSmsContactOne.getText() != null && mSmsContactOne.getText().length()>1){
                    showChangeDialog(PICK_SMS_CONTACT_CODE_1);
                }else {
                    startActivityForResult(PICK_SMS_CONTACT_CODE_1);
                }
                break;
            case R.id.current_contact_two:
                if(mSmsContactTwo.getText() != null && mSmsContactTwo.getText().length()>1){
                    showChangeDialog(PICK_SMS_CONTACT_CODE_2);
                }else {
                    startActivityForResult(PICK_SMS_CONTACT_CODE_2);
                }
                break;
            case R.id.current_contact_three:
                if(mSmsContactThree.getText() != null && mSmsContactThree.getText().length()>1){
                    showChangeDialog(PICK_SMS_CONTACT_CODE_3);
                }else {
                    startActivityForResult(PICK_SMS_CONTACT_CODE_3);
                }
                break;
            case R.id.start_tv:
                mNitifitylayout1.setVisibility(View.GONE);
                break;
            case R.id.next_tv:
                mNitifitylayout2.setVisibility(View.GONE);
                Intent intent = new Intent(this, EMERContactSettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private void startActivityForResult(int code){
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI), code);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
