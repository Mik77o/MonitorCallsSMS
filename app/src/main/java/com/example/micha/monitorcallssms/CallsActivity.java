package com.example.micha.monitorcallssms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Klasa odpowiedzialna za monitorowanie połączeń
 * i obsługę statystyk rozmów
 */
public class CallsActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*wywołanie konstruktora*/
        super.onCreate(savedInstanceState);
        /*ustawienie widoku dla acitvity_calls określonego w activity_calls.xml*/
        setContentView(R.layout.activity_calls);
        /*kontekst bieżącego stanu obiektu*/
        Context con = getBaseContext();
        /*zmienna typu final związana z TextView wyświetlającym statystyki połączeń*/
        final TextView CallHistory = (TextView) findViewById(R.id.TextView1);
        /*wywołanie funkcji na rzecz wyżej utworzonej zmiennej do wyświetlenia statystyk*/
        CallHistory.setText(getCallDetails(con));
    }

    /**
     * Funkcja obsługująca szczegóły połączeń telefonicznych
     * @param context bieżący kontekst w activity
     * @return funkcja zwraca utworzony ciąg statystyk połączeń
     */
    public String getCallDetails(Context context) {
        /*zmienne pomocnicze do zliczania 3 rodzajów połączeń:wychodzących, przychodzących, nieodebranych*/
        int count_wykonano = 0;
        int count_odebrano = 0;
        int count_nieodebrano = 0;

        /*bufor łańcuchowy jest jak ciąg, ale można go zmodyfikować.
         W dowolnym momencie zawiera określoną sekwencję znaków, ale długość
         i treść sekwencji można zmienić za pomocą niektórych wywołań metod.
         Będzie przechowywał informację o statystykach
         */
        StringBuffer stringBuffer = new StringBuffer();


        /*sprawdzenie wersji SDK, sprawdzenie uprawnień do czytania kontaktów i call logs*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);

        /* Cursor umożliwia dostęp do zestawu wyników zwróconych przez zapytanie baz danych*/
        /*Funkcja getContentReslover zwraca instancję ContentResolver dla pakietu aplikacji.*/
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        /*Dzięki klasie CallLog mamy dostęp do parametrów połączeń: numeru telefon, typu połączenia, czasu trwania itp. */
        /*Przypisanie wartości zwracanych przy pomocy getColumnIndex do zmiennych typu int */
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int person = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        /* w pętli pobierane są kolejne wartości, zostają zamienione na typ String w celu wizualizacji*/
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            int callDuration = cursor.getInt(duration);
            String personname = cursor.getString(person);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            /* Instrukcja switch do przypisania odpowiedniego rodzaju połączenia:
             OUTGOING_TYPE, ICOMING_TYPE, MISSED_TYPE i zliczania ilości*/
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "WYCHODZĄCE";
                    count_wykonano++;
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "PRZYCHODZĄCE";
                    count_odebrano++;
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "NIEODEBRANE";
                    count_nieodebrano++;
                    break;
            }
            String callDuration1 = getFormattedTimeString(callDuration);
            /*dołączanie Stringu do bufora przy pomocy funkcji append*/
            stringBuffer.append("\n" + personname + "\nNumer telefonu: " + phNumber + " \nTyp połączenia: "
                    + dir + " \nData połączenia: " + callDayTime
                    + " \nCzas trwania rozmowy: " + callDuration1);
            stringBuffer.append("\n----------------------------------------------");
        }
        /*wyświetlenie informacji o ilości połączeń danego rodzaju*/
        final Toast tim1 = Toast.makeText(this, "\nOdebrano " + count_odebrano + " połączeń" + "\nWykonano " + count_wykonano + " połączeń" + "\nNieodebrano " + count_nieodebrano + " połączeń", Toast.LENGTH_LONG);
        tim1.show();

        /*timer odliczający czas wyświetlania informacji z Toast*/
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                tim1.show();
            }

            public void onFinish() {
                tim1.show();
            }

        }.start();
        cursor.close();
        return stringBuffer.toString();

    }

    /**
     * Funkcja formatująca czas trwania rozmowy
     * @param timeInSeconds czas w sekundach związany z czasem trwania rozmowy
     * @return funkcja zwraca czas rozmowy jako String w celu wygodnego wyświetlania w activity
     */
    public static String getFormattedTimeString(long timeInSeconds) {
        String timeStr = new String();
        long sec_term = 1;
        long min_term = 60 * sec_term;
        long hour_term = 60 * min_term;
        long result = Math.abs(timeInSeconds);

        int hour = (int) (result / hour_term);
        result = result % hour_term;
        int min = (int) (result / min_term);
        result = result % min_term;
        int sec = (int) (result / sec_term);

        if (timeInSeconds <= 0) {
            timeStr = "0s";
        }
        if (hour > 0) {
            timeStr += hour + "h ";
        }
        if (min > 0) {
            timeStr += min + "m ";
        }
        if (sec > 0) {
            timeStr += sec + "s ";
        }
        return timeStr;
    }

}


