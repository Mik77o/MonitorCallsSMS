package com.example.micha.monitorcallssms;

/**
 * @author Michał Kochmański
 * @version 1.0
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Klasa odpowiedzialna za uruchomienie aplikacji,
 * związania z glowna Acitivty aplikacji,
 * z jej poziomu możliwe jest uruchomienie innych Activity
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Wywoływana, gdy aktywność jest tworzona po raz pierwszy.
     * Ta metoda udostępnia także pakiet zawierający wcześniej zamrożony stan działania, o ile taki był.
     *
     * @param savedInstanceState Na ogół używa się do przekazywania danych pomiędzy różnymi Aktywnościami.
     *                           To zależy od programisty, jaki typ wartości chce przekazać,
     *                           ale pakiet może przechowywać wszystkie typy wartości i przekazywać je do nowej aktywności.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*wywołanie konstruktora*/
        super.onCreate(savedInstanceState);
        /*ustawienie Layout'u określonego w pliku activity_main.xml, R to resource*/
        setContentView(R.layout.activity_main);
        /*zabezpieczenie przed obracaniem ekranu*/
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*definicje zmiennych typu ImageButton i powiązanie ich z elementami dodanymi w activity_main.xml*/
        ImageButton btn = (ImageButton) findViewById(R.id.imageButton1);
        ImageButton btn2 = (ImageButton) findViewById(R.id.imageButton2);
        ImageButton btn3 = (ImageButton) findViewById(R.id.imageButton4);
        Button but = (Button) findViewById(R.id.button);

        /*dodanie słuchaczy zdarzeń reagujących na kliknięcie w dany przycisk*/
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                /*wywołanie funkcji odpowiedzialnej za zmianę Activity*/
                startActivity(new Intent(MainActivity.this, CallsActivity.class));
            }
        });
        /*tworzony jest nowy widok*/
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SMSActivity.class));

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        /*Intent uruchamia inną Acitivity, jest łącznikiem pomiędzy nimi*/
        but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DBActivity.class));
            }
        });
    }

}


