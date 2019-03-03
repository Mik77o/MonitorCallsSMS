package com.example.micha.monitorcallssms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Klasa odpowiedzialna za monitorowanie smsów
 * i obsługę statystyk wiadomości
 */
public class SMSActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_SMS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context cont = getBaseContext();
        super.onCreate(savedInstanceState);
        /*ustawienie widoku dla acitvity_calls określonego w activity_sms.xml*/
        setContentView(R.layout.activity_sms);
        final TextView SMSHistory = (TextView) findViewById(R.id.TextView2);
        SMSHistory.setText(getSMSDetails(cont));
    }

    /**
     * Funkcja obsługująca szczegóły wiadomości SMS
     *
     * @param context bierzący kontekst w acitvity
     * @return funkcja zwraca utworzony ciąg statystyk dotyczących wiadomości
     */
    public String getSMSDetails(Context context) {
        /*utworzenie obiektu bufora*/
        StringBuffer stringBuff = new StringBuffer();
        /* zmienne pomocnicze do zliczania wysłanych i odebranych wiadomości*/
        int count_received = 0;
        int count_sent = 0;

        /*sprawdzenie wersji SDK, sprawdzenie uprawnień do czytania kontaktów i smsów*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_SMS);
        /* Cursor umożliwia dostęp do zestawu wyników zwróconych przez zapytanie baz danych*/
        /*Funkcja getContentReslover zwraca instancję ContentResolver dla pakietu aplikacji.*/
        Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC");
        /*Telephony.sms umożliwia dostęp do informacji dotyczących wiadomości sms*/
        int type = cursor.getColumnIndex(Telephony.Sms.TYPE);
        int content = cursor.getColumnIndex(Telephony.Sms.BODY);
        int date = cursor.getColumnIndex(Telephony.Sms.DATE);
        int phNumber = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
        /* w pętli pobierane są kolejne wartości, zostają zamienione na typ String w celu wizualizacji*/
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

            /*instrukcja switch do określenia rodzaju wiadomości: MESSAGE_TYPE_INBOX lub MESSAGE_TYPE_SENT*/
            switch (typeCode) {
                case Telephony.Sms.MESSAGE_TYPE_INBOX:
                    direction = "ODEBRANA";
                    count_received++;
                    break;

                case Telephony.Sms.MESSAGE_TYPE_SENT:
                    direction = "WYSLANA";
                    count_sent++;
                    break;
            }
            /*dołączanie Stringu do bufora przy pomocy funkcji append*/
            stringBuff.append("\n" + namestr + "\nNumer telefonu: " + number + " \nTreść: "
                    + body + " \nData: " + SMSDate
                    + "\nTyp wiadomości: " + direction);
            stringBuff.append("\n----------------------------------------------");
        }
        /*wyświetlenie informacji o ilości odebranych i wysłanych smsów*/
        final Toast tim1 = Toast.makeText(this, "\nOdebrano " + count_received + " smsów" + "\nWysłano " + count_sent + " smsów", Toast.LENGTH_LONG);
        tim1.show();
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                tim1.show();
            }

            public void onFinish() {
                tim1.show();
            }

        }.start();
        cursor.close();
        return stringBuff.toString();

    }

    /**
     * Funkcja zamieniająca numer telefonu na odpowiadającą mu nazwę kontaktu, o ile to możliwe
     *
     * @param number numer telefonu
     * @return funkcja zwraca nazwę kontaktu
     */
    public String getContactDisplayNameByNumber(String number) {
        /*użycie następującego identyfikatora URI do odczytania numeru*/
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        /*w przypadku braku nazwy kontaktu pojawia się ten napis w TextView*/
        String name = "Brak w liście kontaktów";
        ContentResolver contentRes = this.getContentResolver();
        Cursor contact_lookup = contentRes.query(uri, new String[]{BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);


        if (contact_lookup != null && contact_lookup.getCount() > 0) {
            contact_lookup.moveToNext();
            /* przypisanie nazwy kontaktu do zmiennej typu String, funkcja zwraca wartość tej zmiennej*/
            name = contact_lookup.getString(contact_lookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
        }
        if (contact_lookup != null)
            contact_lookup.close();
        return name;
    }


}




