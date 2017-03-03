package com.restfulrobot.cdcapplication.comparators;


import com.restfulrobot.cdcapplication.objects.PlaceItem;

import java.util.Comparator;

public class IdComparator implements Comparator<PlaceItem> {
    @Override
    public int compare(PlaceItem placeItem1, PlaceItem placeItem2) {
        int id1 = placeItem1.getId();
        int id2 = placeItem2.getId();
        if (id1 > id2) {
            return 1;
        }
        if (id1 < id2) {
            return -1;
        }
        return 0;
    }
}
