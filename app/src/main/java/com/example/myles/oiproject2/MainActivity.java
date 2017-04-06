package com.example.myles.oiproject2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText medtName;
    private EditText medtNumber;
    private EditText medtMessage;
    private Button mbtnAdd;
    private SmsManager smsMgr;
    private String name;
    private Cursor cursor;
    private final int REQUEST_CODE = 99;
    private String contactID;

    private static final int PICK_CONTACT = 1;

    private static final int READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mbtnAdd = (Button) findViewById(R.id.btnAdd);
        mbtnAdd.setOnClickListener(this);

        medtName = (EditText) findViewById(R.id.edtName);
        medtNumber = (EditText) findViewById(R.id.edtNumber);
        medtMessage = (EditText) findViewById(R.id.edtMessage);


        getPermissionToReadUserContacts();


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 123); //supposed to be both in the new string array
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS);
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS);
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){

            }
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SEND Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SEND Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case READ_CONTACTS:
                boolean showRationale = true;

                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read contacts permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    showRationale = shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS);

                    if (!showRationale) {
                        Toast.makeText(this, "Read contacts permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == mbtnAdd.getId()){
            smsMgr = SmsManager.getDefault();
            //smsMgr.sendTextMessage("8168126045", null,"OI: " + medtMessage.getText().toString(), null, null);
            //medtMessage.setText("");
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data){
        super.onActivityResult(reqCode, resultCode, data);

        Uri contactData = data.getData();
        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(contactData,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        //Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            medtNumber.setText(contactNumber);
        }

        medtName.setText(getContactName(this, contactNumber));
        cursorPhone.close();
        //Log.d(TAG, "Contact Phone Number: " + contactNumber);
    }

    //when pulling out image, you are getting a reference to a location as to where the image is stored on the phone
    //gets the URI for the image if use custom adapter, embed imageview

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}
