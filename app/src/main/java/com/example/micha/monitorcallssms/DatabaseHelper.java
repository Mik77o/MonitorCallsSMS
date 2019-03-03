package com.example.micha.monitorcallssms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;

/**
 * Klasa DatabaseHelper odpowiedzialna jest za tworzenie bazy danych,
 * zawiera public static final String pola, które są odpowiednikami kolumn w tabeli.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "History11.db";
    public static final String TABLE_NAME = "calls_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NR_TEL";
    public static final String COL_3 = "PERSON";
    public static final String COL_4 = "TYP";
    public static final String COL_5 = "CZAS_TRWANIA";
    public static final String COL_6 = "DATA";

    /*Konstruktor klasy DatabaseHelper, klasa Context umożliwia dostęp do zasobów i klas specyficznych dla aplikacji,
     a także do wywołań dla operacji na poziomie aplikacji,
    takich jak uruchamianie działań, nadawanie i otrzymywanie intents, to kontekst akutalnego stanu aplikacji/obiektu*/
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db=this.getWritableDatabase();
    }

    /**
     * metoda odpowiedzialna za utworzenie tabeli
     *
     * @param db parametr typu SQLiteDatabase - klasa ta ma metody do:
     *           tworzenia, usuwania, wykonywania poleceń SQL i wykonywania innych typowych zadań zarządzania bazami danych.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /* Funkcja execSQL wykonuje pojedynczą instrukcję SQL,
        która NIE jest SELECT ani żadną inną instrukcją SQL zwracającą dane,
        w tym przypadku tworzona jest tabela*/
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,NR_TEL TEXT,PERSON TEXT,TYP TEXT,CZAS_TRWANIA TEXT,DATA DEFAULT CURRENT_TIMESTAMP)");
    }

    /**
     * Wywoływana, gdy baza danych powinna być "upgrade'owana"
     *
     * @param db         parametr związany z bazą danych
     * @param oldVersion stara wersja bazy danych
     * @param newVersion nowa wersja bazy danych
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Metoda odpowiedzialna za dodawanie rekordów do bazy danych
     *
     * @param nr_tel  numer telefon
     * @param pers    nazwa osoby dzwoniącej
     * @param typ     typ połączenia
     * @param czas_tr czas trwania rozmowy
     * @param data    szczegółowa data rozmowy
     * @return zwraca true or false, w zależności czy rekord został dodany czy nie
     */
    public boolean insertData(String nr_tel, String pers, String typ, String czas_tr, java.sql.Date data) {

        /*Utwórz i / lub otwórz bazę danych, która będzie używana do czytania i pisania*/
        SQLiteDatabase db = this.getWritableDatabase();

        /*Klasa ContentValues służy do przechowywania zestawu wartości, które może przetwarzać ContentResolver
          ContentValues() tworzy pusty zestaw wartości przy użyciu domyślnego rozmiaru początkowego*/
        ContentValues contentValues = new ContentValues();

        /*metoda put dodaje wartość do wstawienia, wszystie są typu String w tym przypadku*/
        contentValues.put(COL_2, nr_tel);
        contentValues.put(COL_3, pers);
        contentValues.put(COL_4, typ);
        contentValues.put(COL_5, czas_tr);

        /*klasa do formatowania i analizowania dat w sposób zależny od ustawień narodowych.
        Pozwala na formatowanie (data → tekst), parsowanie (tekst → data) i normalizację,
        tutaj formatowanie według wzoru yyyy-MM-dd HH:mm:ss i zamiana na String
        */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String data1 = sdf.format(data);
        contentValues.put(COL_6, data1);
        /*Metoda wstawiająca wiersz do bazy danych, jej parametrami są nazwa tabeli
        typu String, parametrSQL nie zezwala na wstawianie całkowicie pustego wiersza bez nazwy co najmniej jednej nazwy kolumny.
        Jeśli podane wartości są puste, nie są znane żadne nazwy kolumn i nie można wstawić pustego wiersza. Jeśli nie zostanie ustawiona wartość null,
        parametr nullColumnHack podaje nazwę kolumny kolumny zerowalnej,
        aby jawnie wstawić wartość NULL w przypadku, gdy wartości są puste,
        ContentValues: ta mapa zawiera początkowe wartości kolumn dla wiersza. Kluczami powinny być nazwy kolumn i wartości kolumn.*/
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    /**
     * Metoda wybierająca wszystkie rekordy z bazy posortowane malejąco po dacie połączenia
     *
     * @return zwraca wartość typu interfejs, który zapewnia losowy dostęp do odczytu
     * i zapisu do zestawu wyników zwróconego przez zapytanie bazy danych.
     */
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        /*funkcja rawQuery wykorzystywana jest do operacji selekcji w tabeli*/
        Cursor res = db.rawQuery("select * from calls_table order by DATA DESC", null);
        return res;
    }

    /**
     * Metoda usuwająca wszystkie rekordy z bazy danych i wywołująca metodę onCreate z parametrem
     * db typu SQLiteDatabase
     */
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Metoda usuwająca duplikaty z bazy danych
     * Usuwane są rekordy, których pole DATA występuję więcej niż jeden raz,
     * to znaczy, w bazie zostaje tylko jeden rekord o takiej samej dacie i czasie
     */
    public void deleteDuplicates() {
        getWritableDatabase().execSQL("delete from calls_table WHERE ID IN (SELECT DISTINCT ID FROM calls_table GROUP BY DATA HAVING COUNT(*) > 1)");

    }

}