package com.restfulrobot.cdcapplication.comparators;


import com.restfulrobot.cdcapplication.objects.PlaceItem;

import java.util.Comparator;

public class DistanceComparator implements Comparator<PlaceItem> {
    @Override
    public int compare(PlaceItem placeItem1, PlaceItem placeItem2) {
        double distance1 = placeItem1.getDistance();
        double distance2 = placeItem2.getDistance();
        if (distance1 > distance2) {
            return 1;
        }
        if (distance1 < distance2) {
            return -1;
        }
        return 0;
    }
}
