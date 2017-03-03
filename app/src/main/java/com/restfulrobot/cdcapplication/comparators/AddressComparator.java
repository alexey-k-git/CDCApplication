package com.restfulrobot.cdcapplication.comparators;

import com.restfulrobot.cdcapplication.objects.PlaceItem;

import java.util.Comparator;

public class AddressComparator implements Comparator<PlaceItem> {
    @Override
    public int compare(PlaceItem placeItem1, PlaceItem placeItem2) {
        String str1 = placeItem1.getAddress();
        String str2 = placeItem2.getAddress();
        return str1.compareTo(str2);
    }
}
