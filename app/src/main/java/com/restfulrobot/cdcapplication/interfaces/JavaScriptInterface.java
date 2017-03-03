package com.restfulrobot.cdcapplication.interfaces;

import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;

import com.restfulrobot.cdcapplication.MainActivity;
import com.restfulrobot.cdcapplication.objects.Coordinar;
import com.restfulrobot.cdcapplication.objects.PlaceItem;

// Класс взаимодействия Приложения с JavaScript'ом
public class JavaScriptInterface {

    private Handler mainActivityHandler;
    private int lastId = 0;

    public JavaScriptInterface(Handler handler) {
        this.mainActivityHandler = handler;
    }

    // Создание новой точки в списке
    @JavascriptInterface
    public void createNewPlaceItem(String newContent, String latitude, String longitude) {
        Coordinar coordinar = new Coordinar(Float.valueOf(latitude), Float.valueOf(longitude));
        PlaceItem newPlace = new PlaceItem(lastId, "Обычный комментарий", newContent, coordinar);
        Message msg = mainActivityHandler.obtainMessage(MainActivity.NEW_PLACE_MESSAGE, newPlace);
        mainActivityHandler.sendMessage(msg);
    }

    // Формируем новое id точки для карты
    @JavascriptInterface
    public int getNextId() {
        return ++lastId;
    }

    // Получаем список расстояний до всех текущих точек
    @JavascriptInterface
    public void sortByDistance(String[] distanceArray) {
        double[] distances = new double[distanceArray.length];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = Double.valueOf(distanceArray[i]);
        }
        Message msg = mainActivityHandler.obtainMessage(MainActivity.DISTANCES_FROM_ME_MESSAGE, distances);
        mainActivityHandler.sendMessage(msg);
    }


}

