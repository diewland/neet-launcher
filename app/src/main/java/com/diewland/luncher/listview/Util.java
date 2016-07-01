package com.diewland.luncher.listview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yuwat on 10/26/2015.
 */
public class Util {

    public static Item get_info_from_title(Map<String, Item> hm, String title) {
        for (String k : hm.keySet()) {
            Item info = hm.get(k);
            if (info.getTitle().equals(title)) {
                return info;
            }
        }
        return null;
    }

    public static List<Item> sort_by(String sort_type, Collection<Item> items){
        if(sort_type.equals("TS")){
            return ts_sort(items);
        }
        else { // default: score
            return score_sort(items);
        }
    }

    // http://stackoverflow.com/questions/780541/how-to-sort-a-hashmap-in-java
    public static List<Item> score_sort(Collection<Item> items){
        List<Item> sorted_items = new ArrayList<Item>(items);
        Collections.sort(sorted_items, new Comparator<Item>() {
            public int compare(Item o1, Item o2) {
                if(o1.getScore() != o2.getScore()){
                    // sort by score first
                    return o2.getScore() - o1.getScore();
                }
                else {
                    // else sort by alphabet
                    return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                }
            }
        });
        return sorted_items;
    }

    public static List<Item> ts_sort(Collection<Item> items){
        List<Item> sorted_items = new ArrayList<Item>(items);
        Collections.sort(sorted_items, new Comparator<Item>() {
            public int compare(Item o1, Item o2) {
                if(o1.getTS() != o2.getTS()){
                    // sort by ts first
                    return Long.valueOf(o2.getTS()).compareTo(Long.valueOf(o1.getTS()));
                }
                else {
                    // else sort by alphabet
                    return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                }
            }
        });
        return sorted_items;
    }

    public static List<Item> filter(List<Item> items, String q){
        if((q == null) || (q.equals(""))){
            return items;
        }
        List<Item> result = new ArrayList<Item>();
        for(Item it : items){
            if(it.getTitle().toLowerCase().indexOf(q.toLowerCase()) > -1){
                result.add(it);
            }
        }
        return result;
    }

}
