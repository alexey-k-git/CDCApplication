package com.restfulrobot.cdcapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.restfulrobot.cdcapplication.interfaces.JavaScriptInterface;
import com.restfulrobot.cdcapplication.objects.Coordinar;
import com.restfulrobot.cdcapplication.objects.PlaceItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity {

    // индитификаторы для сообщения Handler'a
    final public static int NEW_PLACE_MESSAGE = 0;
    final public static int DISTANCES_FROM_ME_MESSAGE = 1;

    // WebView с картой Яндекса
    private WebView browser;
    // ссылка на ListFragment с точками
    private PlaceItemsListFragment listFragment;
    // Массив для spinner
    private String[] data = {"по номеру", "по адресу", "по удаленности", "по удаленности 2"};
    private Spinner spinner;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listFragment = (PlaceItemsListFragment) getFragmentManager().findFragmentById(R.id.list_frag);
        // Заводим handler для связи с JavaScriptInterface
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case (NEW_PLACE_MESSAGE):
                        listFragment.addNewItem((PlaceItem) msg.obj);
                        break;
                    case (DISTANCES_FROM_ME_MESSAGE): {
                        listFragment.setDistances((double[]) msg.obj);
                        listFragment.sort(TypeOfSort.SortByDistanceFromMe);
                        Toast.makeText(MainActivity.this, "Сортировка завершена успешно!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        };
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface(handler);
        // настраиваем WebView
        browser = (WebView) findViewById(R.id.yandexMapWebView);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setGeolocationEnabled(true);
        browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        browser.addJavascriptInterface(javaScriptInterface, "Android");
        // загружаем в WebView страницу html с Яндекс картой
        try {
            InputStream is = getAssets().open("index.html");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String htmlText = new String(buffer);
            browser.loadDataWithBaseURL(
                    "http://com.restfulrobot.cdcapplication.ymapapp",
                    htmlText,
                    "text/html",
                    "UTF-8",
                    null
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        // настраиваем Spinner для сортировки точек по разным параметрам
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (listFragment.isSortable()) {
                    switch (position) {
                        case 0:
                            listFragment.sort(TypeOfSort.SortById);
                            break;
                        case 1:
                            listFragment.sort(TypeOfSort.SortByAddress);
                            break;
                        case 2: {
                            Toast.makeText(MainActivity.this, "Идёт сортировка по удаленности от вас.\nПодождите, пожалуйста, некоторое время", Toast.LENGTH_LONG).show();
                            String queryArguments = listFragment.getQueryArgumentsForSearchingDistance();
                            browser.loadUrl("javascript:searchDistance("
                                    + queryArguments
                                    + ")");
                        }
                        case 3: {
                            GPSTracker gps = new GPSTracker(MainActivity.this);
                            if (gps.canGetLocation()) {
                                double latitude = gps.getLatitude();
                                double longitude = gps.getLongitude();
                                if (!(latitude != 0 && longitude != 0)) {
                                    Toast.makeText(MainActivity.this, "Не удалось определить ваше положение.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Идёт сортировка по удаленности от вас.\nПодождите, пожалуйста, некоторое время", Toast.LENGTH_LONG).show();
                                    String queryArguments = listFragment.getQueryArgumentsForSearchingDistance();
                                    browser.loadUrl("javascript:searchDistance2("
                                            + queryArguments + ","
                                            + String.valueOf(latitude) + ","
                                            + String.valueOf(longitude)
                                            + ")");
                                }

                            } else {
                                gps.showSettingsAlert();
                            }

                        }
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    // обращаемся к яндекс карте с запросом перейти на новое место
    public void goToPlaceQuery(int id, String comment, float latitude, float longitude) {
        String queryArguments = String.valueOf(id) + "," + "'" + comment + "'" + ","
                + String.valueOf(latitude) + "," + String.valueOf(longitude);
        browser.loadUrl("javascript:goToPlace(" + queryArguments + ")");
    }

    // обращаемся к яндекс карте с запросом изменить комментарий у открытого балуна
    public void changeCommentQuery(PlaceItem placeItem, boolean activeItemFlag) {
        if (activeItemFlag) {
            String queryArguments = String.valueOf(placeItem.getId()) + "," + "'" + placeItem.getComment() + "'" + ","
                    + String.valueOf(placeItem.getCoordinar().getLatitude()) + "," + String.valueOf(placeItem.getCoordinar().getLongitude());
            browser.loadUrl("javascript:goToPlace(" + queryArguments + ")");
        }
        listFragment.dismissDialog();
    }

    // обращаемся к яндекс карте с запросом на удаление текущей метки
    public void deletePlaceFromMapQuery() {
        browser.loadUrl("javascript:deletePlaceFromMap()");
    }

    // вызываем яндекс навигатор
    public void navigate(View view) {
        if (listFragment.isPointExist()) {
            Coordinar lastCoordinar = listFragment.getLastCoordinar();
            Intent intent = new Intent("ru.yandex.yandexnavi.action.BUILD_ROUTE_ON_MAP");
            intent.setPackage("ru.yandex.yandexnavi");
            PackageManager pm = getPackageManager();
            List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
            // Проверяем, установлен ли Яндекс.Навигатор
            if (infos == null || infos.size() == 0) {
                // Если нет - будем открывать страничку Навигатора в Google Play
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=ru.yandex.yandexnavi"));
            } else {
                intent.putExtra("lat_to", lastCoordinar.getLatitude());
                intent.putExtra("lon_to", lastCoordinar.getLongitude());
            }
            startActivity(intent);
        }
    }
}

// типы сортировки точек
enum TypeOfSort {
    SortById,
    SortByAddress,
    SortByDistanceFromMe
}
