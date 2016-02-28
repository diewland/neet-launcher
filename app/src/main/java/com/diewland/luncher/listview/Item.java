package com.diewland.luncher.listview;

import java.util.HashMap;
import java.util.List;

public class Item {

    private String title;
    private String pkg;
    private int score;

    public Item(String title, String pkg){
        this.title = title;
        this.pkg = pkg;
        this.score = 0;
    }

    public Item(String title, String pkg, int score){
        this.title = title;
        this.pkg = pkg;
        this.score = score;
    }

    public String getTitle(){
        return this.title;
    }

    public String getPackage(){
        return this.pkg;
    }

    public int getScore(){
        return this.score;
    }

    public void click(){
        this.score++;
    }

    // test
    public static void main(String[] args){

        HashMap<String, Item> app_list = new HashMap<>();

        Item a = new Item("apple",  "com.apple",    0);
        Item b = new Item("banana", "com.banana",   0);
        Item c = new Item("coconut","com.coconut",  0);
        Item d = new Item("donut",  "com.donut",    2);
        Item e = new Item("eletro", "com.eletro",   3);

        app_list.put("com.coconut", c);
        app_list.put("com.banana", b);
        app_list.put("com.eletro", e);
        app_list.put("com.donut", d);
        app_list.put("com.apple", a);

        // sort by util
        List<Item> sorted_apps = Util.score_sort(app_list.values());
        for(Item app : sorted_apps){
            System.out.println(app.getPackage() + "\t" + app.getTitle() + "\t" + app.getScore());
        }
        System.out.println("--- 4 clicks of banana");
        b.click();
        b.click();
        b.click();
        b.click();
        for(Item app : Util.score_sort(app_list.values())){
            System.out.println(app.getPackage() + "\t" + app.getTitle() + "\t" + app.getScore());
        }
    }

}


