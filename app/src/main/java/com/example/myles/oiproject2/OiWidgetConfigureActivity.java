package com.example.myles.oiproject2;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.VectorEnabledTintResources;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The configuration screen for the {@link OiWidget OiWidget} AppWidget.
 */
public class OiWidgetConfigureActivity extends Activity implements View.OnClickListener {

    private static final String PREFS_NAME = "com.example.myles.oiproject2.OiWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private EditText mAppWidgetText;
    private EditText medtNumber;
    private EditText medtName;
    private EditText medtMessage;
    private Button mbtnContact;

    private SmsManager smsMgr;

    private String contactID = "";
    private String content = "";


    private static final int PICK_CONTACT = 1;
    private static final int READ_CONTACTS = 1;


    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = OiWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String info = loadTitlePref(context, mAppWidgetId);
            if(info.isEmpty()){
                info = "";
            }
            else {
                info = info + "," + medtMessage.getText().toString();
            }
            toa(info);
            saveTitlePref(context, mAppWidgetId, info);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            OiWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    private void toa(String info){
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();

    }

    public OiWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);



        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);


        setContentView(R.layout.oi_widget_configure);

        idControls();

        findViewById(R.id.btnContacts).setOnClickListener(this);

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(OiWidgetConfigureActivity.this, mAppWidgetId));
    }

    private void idControls(){
        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        medtMessage = (EditText) findViewById(R.id.edtMessage);
        medtNumber = (EditText) findViewById(R.id.edtNumber);
        mbtnContact = (Button) findViewById(R.id.btnContacts);
        mbtnContact.setOnClickListener(this); //.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        smsMgr = SmsManager.getDefault();
        //smsMgr.sendTextMessage("8168126045", null,"OI: " + medtMessage.getText().toString(), null, null);
        //medtMessage.setText("");
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data)
    {
        super.onActivityResult(reqCode, resultCode, data);

        String id = null;
        String contactNumber = null;
        String name = "Myles";

        if(reqCode == PICK_CONTACT)
        {
            if (resultCode == this.RESULT_OK)
            {
                Uri contactData = data.getData();

                Cursor phoneID = getContentResolver().query(contactData, new String[]{ContactsContract.Contacts._ID}, null, null, null);

                if (phoneID.moveToFirst())
                    id = phoneID.getString(phoneID.getColumnIndex(ContactsContract.Contacts._ID));

                phoneID.close();

                Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                        new String[]{id},
                        null);

                if (cursorPhone.moveToFirst()) {
                    contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }

                cursorPhone.close();

                name = getContactName(this, contactNumber);

                if(content.equals("")) {
                    content += name + "," + contactNumber;
                }
                else{
                    content += "," + name + "," + contactNumber;
                }

                String[] contentArray = content.split(",");
                String names = "";
                String numbers = "";

                for(int i = 0; i < contentArray.length; i+=2){
                    names+=contentArray[i] + ",";
                    numbers+= contentArray[i+1] + ",";
                }

                names = names.substring(0, names.length()-1);
                numbers = numbers.substring(0, numbers.length()-1);

                medtNumber.setText(numbers);
                mAppWidgetText.setText(names);

                saveTitlePref(this, mAppWidgetId, content);
            }
        }
    }

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

    static int loadCounter(Context context, int appWidgetId){
        SharedPreferences pref = context.getSharedPreferences("Counter", 0);
        int count = pref.getInt("Count", 0);
        return count;
    }

    static void saveCounter(Context context, int appWidgetId, int count){
        SharedPreferences.Editor pref = context.getSharedPreferences("Count", 0).edit();
        pref.putInt("Count", count+1);
        pref.commit();

    }
}

