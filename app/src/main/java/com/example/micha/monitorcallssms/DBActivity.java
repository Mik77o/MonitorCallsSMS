package com.example.micha.monitorcallssms;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Klasa związana z obsługą bazy danych SQLite
 * zawierająca metody do dodawania danych i ich usuwania
 * oraz do wyświetlania zawartości bazy: statystyki rozmów
 * i wiadomości
 */
public class DBActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 100;

    /*deklaracje obiektów bazodanowych do utworzenia bazy danych*/
    DatabaseHelper myDb;
    DatabaseHelper2 myDb_s;
    /*definicje zmiennych powiązanych z przyciskami do zarządzania bazą danych*/
    Button btnDelete, btnDelete_s, btnviewAll, btnviewAll_s;

    /*zmienne statyczne typu private final dotyczące uprawnień niezbędnych do prawidłowego działania aplikacji*/
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*wywołanie konstruktora*/
        super.onCreate(savedInstanceState);
        /*ustawienie widoku dla acitvity_db określonego w activity_calls.xml*/
        setContentView(R.layout.activity_db);
        /*pobranie akutalnego kontekstu*/
        Context con = getBaseContext();
        /*zabezpieczenie przed zmianą orientacji okna*/
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        /*definicje obiektów baz danych*/
        myDb = new DatabaseHelper(this);
        myDb_s = new DatabaseHelper2(this);
        /*powiązanie zmiennych dotyczących przycisków z layout'em*/
        btnviewAll = (Button) findViewById(R.id.button_viewAll);
        btnDelete = (Button) findViewById(R.id.button2);
        btnviewAll_s = (Button) findViewById(R.id.button3);
        btnDelete_s = (Button) findViewById(R.id.button4);
        TextView text = (TextView) findViewById(R.id.textView6);

        /*kod odpowiedzialny za wyświetlenie czasu i daty w DBActivity*/
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        text.setText(formattedDate);

        /*wywołanie funkcji obsługujących bazę danych*/
        AddData(con);
        AddData_sms(con);
        myDb.deleteDuplicates();
        viewAll();
        myDb_s.deleteDuplicates();
        viewAll_s();
        DeleteAll();
        DeleteAll_s();
    }

    /**
     * Funkcja dodająca historię połączeń do bazy danych
     *
     * @param cont aktualny kontekst
     */
    public void AddData(Context cont) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);

        Cursor cursor = cont.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int person = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            int callDuration = cursor.getInt(duration);
            String personname = cursor.getString(person);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "WYCHODZĄCE";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "PRZYCHODZĄCE";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "NIEODEBRANE";
                    break;
            }
            String callDuration1 = getFormattedTimeString(callDuration);
            myDb.insertData(phNumber, personname, dir, callDuration1, callDayTime);

        }
        cursor.close();
    }

    /**
     * Funkcja wyświetlająca całą historię połączeń
     */
    public void viewAll() {
        btnviewAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*pobranie całej zawartości bazy danych do zmiennej typu Cursor*/
                        Cursor res = myDb.getAllData();
                        if (res.getCount() == 0) {
                            // show message
                            showMessage("Error", "Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("Nr_tel :" + res.getString(1) + "\n");
                            buffer.append("Person :" + res.getString(2) + "\n");
                            buffer.append("Typ :" + res.getString(3) + "\n");
                            buffer.append("Czas_trwania :" + res.getString(4) + "\n");
                            buffer.append("Data :" + res.getString(5) + "\n\n");
                        }

                        // Show all data
                        showMessage("Historia połączeń", buffer.toString());

                    }
                }
        );

    }

    /**
     * Funkcja usuwająca całą zawartość bazy danych dotyczącej historii połączeń
     */
    public void DeleteAll() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Wprowadzenie AlertDialog do ostrzegania przed usuwaniem całej zawartości bazy danych*/
                        new AlertDialog.Builder(DBActivity.this).setTitle("Potwierdzenie usuwania")
                                .setMessage("Jesteś pewny, że chcesz usunąć historię połączeń?")
                                .setPositiveButton("TAK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                myDb.deleteAll();
                                                dialog.dismiss();
                                                Toast.makeText(DBActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                .setNegativeButton("NIE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();

                    }
                }
        );
    }

    /**
     * Funkcja dodająca historię sms do bazy danych
     *
     * @param context aktualny kontekst
     */
    public void AddData_sms(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_SMS);
        Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC");
        int type = cursor.getColumnIndex(Telephony.Sms.TYPE);
        int content = cursor.getColumnIndex(Telephony.Sms.BODY);
        int date = cursor.getColumnIndex(Telephony.Sms.DATE);
        int phNumber = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
        while (cursor.moveToNext()) {

            String smsType = cursor.getString(type);
            String body = cursor.getString(content);
            String number = cursor.getString(phNumber);
            String datesms = cursor.getString(date);
            String person = cursor.getString(phNumber);
            String namestr = getContactDisplayNameByNumber(person);
            Date SMSDate = new Date(Long.valueOf(datesms));
            int typeCode = Integer.parseInt(smsType);
            String direction = null;
            switch (typeCode) {
                case Telephony.Sms.MESSAGE_TYPE_INBOX:
                    direction = "ODEBRANA";
                    break;

                case Telephony.Sms.MESSAGE_TYPE_SENT:
                    direction = "WYSLANA";
                    break;
            }
            /*Dodanie  nowego rekordu do bazy danych*/
            myDb_s.insertData(namestr, number, body, SMSDate, direction);
        }
        cursor.close();
    }

    /**
     * Funkcja wyświetlająca całą historię sms
     */
    public void viewAll_s() {
        /*dodanie słuchacza zdarzeń reagującego na akcję przycisku*/
        btnviewAll_s.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb_s.getAllData();
                        if (res.getCount() == 0) {
                            // show message
                            showMessage("Error", "Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("Autor :" + res.getString(1) + "\n");
                            buffer.append("Nr_tel :" + res.getString(2) + "\n");
                            buffer.append("Treść :" + res.getString(3) + "\n");
                            buffer.append("Data :" + res.getString(4) + "\n");
                            buffer.append("Typ :" + res.getString(5) + "\n\n");
                        }

                        // Show all data
                        showMessage("Historia SMS", buffer.toString());

                    }
                }
        );

    }

    /**
     * Funkcja usuwająca całą zawartość bazy danych dotyczącej historii sms
     */
    public void DeleteAll_s() {
        btnDelete_s.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(DBActivity.this).setTitle("Potwierdzenie usuwania")
                                .setMessage("Jesteś pewny, że chcesz usunąć historię sms?")
                                .setPositiveButton("TAK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                myDb_s.deleteAll();
                                                dialog.dismiss();
                                                Toast.makeText(DBActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                .setNegativeButton("NIE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();

                    }
                }
        );
    }

    /**
     * Funkcja zmieniająca numer telefonu na odpoowiadającą mu nazwę kontaktu
     *
     * @param number numer telefonu
     * @return funkcja zwraca nazwę kontaktu na podstawie numeru telefonu, o ile nazwa kontatku jest zapisana w tel
     */
    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "Brak w liście kontaktów";
        ContentResolver contentResolver = this.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);


        if (contactLookup != null && contactLookup.getCount() > 0) {
            contactLookup.moveToNext();
            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
        }
        if (contactLookup != null)
            contactLookup.close();
        return name;
    }


    /**
     * Funkcja pomocnicza do prezentowania historii połączeń i smsów
     *
     * @param title   tytuł komunikatu
     * @param Message wyświetlana wiadomość
     */
    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    /**
     * Funkcja formatująca czas trwania rozmowy
     *
     * @param timeInSec
     * @return
     */
    public static String getFormattedTimeString(long timeInSec) {
        String timeStr = new String();
        long sec_term = 1;
        long min_term = 60 * sec_term;
        long hour_term = 60 * min_term;
        long result = Math.abs(timeInSec);

        int hour = (int) (result / hour_term);
        result = result % hour_term;
        int minute = (int) (result / min_term);
        result = result % min_term;
        int seconds = (int) (result / sec_term);

        if (timeInSec <= 0) {
            timeStr = "0s";
        }
        if (hour > 0) {
            timeStr += hour + "h ";
        }
        if (minute > 0) {
            timeStr += minute + "m ";
        }
        if (seconds > 0) {
            timeStr += seconds + "s ";
        }
        return timeStr;
    }


}