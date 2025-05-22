package com.peter.ccgraphics.data;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map.Entry;

public class CountingMap<T extends Object> {

    private final HashMap<T, Integer> count = new HashMap<>();

    public CountingMap() {

    }

    public void count(T obj) {
        if (!count.containsKey(obj)) {
            count.put(obj, 1);
            return;
        }
        int c = count.get(obj);
        count.put(obj, c + 1);
    }

    protected class Element {
        public T obj;
        public int freq;

        public Element(T obj, int freq) {
            this.obj = obj;
            this.freq = freq;
        }

        public Element(Entry<T, Integer> entry) {
            this.obj = entry.getKey();
            this.freq = entry.getValue();
            if (obj == null) {
                throw new IllegalArgumentException("Object can not be null");
            }
        }
    }

    public T[] sort(T[] outArr) {
        @SuppressWarnings("unchecked")
        Element[] arr = (Element[]) Array.newInstance(Element.class, count.size());
        int i = 0;
        for (Entry<T, Integer> e : count.entrySet()) {
            arr[i] = new Element(e);
            i++;
        }

        quickSort(arr, 0, arr.length - 1);
        
        if (arr.length != outArr.length) {
            throw new IllegalArgumentException("`outArr` must the the same size as the collection");
        }
        for (int j = 0; j < arr.length; j++) {
            outArr[j] = arr[j].obj;
            if (outArr[j] == null) {
                throw new IllegalArgumentException("Object at ["+j+"] was null");
            }
        }
        return outArr;
    }
    
    protected void quickSort(Element[] arr, int start, int end) {
        int l = end - start;
        if (l < 2) {
            return;
        } else if (l == 2) {
            if (arr[start].freq < arr[end].freq) {
                swap(arr, start, end);
            }
            return;
        }
        int p = quickSortPartition(arr, start, end);
        quickSort(arr, start, p);
        quickSort(arr, p+1, end);
    }
    
    protected int quickSortPartition(Element[] arr, int start, int end) {
        Element pivot = arr[start];
        int left = start;
        int right = end;

        int t = 0;
        while (left < right) {
            t++;
            if (t > end - start) {
                throw new RuntimeException("Something went wrong ...");
            }
            do {
                left++;
            } while (arr[left].freq > pivot.freq && left < right);

            do {
                right--;
            } while (arr[right].freq < pivot.freq && left < right);

            if (left >= right) {
                return right;
            }

            swap(arr, left, right);
        }
        
        return right;
    }

    protected void swap(Element[] arr, int i1, int i2) {
        Element e = arr[i1];
        arr[i1] = arr[i2];
        arr[i2] = e;
    }

    public int size() {
        return count.size();
    }

    // public Set<T> mostCommon(int num) {
    //     Set<Element> mostCommon = new HashSet<Element>();
    //     T leastCommon = null;
    //     int leastCommonFreq = -1;

    //     for (Entry<T, Integer> entry : count.entrySet()) {
    //         T val = entry.getKey();
    //         int freq = entry.getValue();
            
    //     }

    //     Set<T> out = new HashSet<T>();
    //     for (Element el : mostCommon) {
    //         out.add(el.obj);
    //     }
    //     return out;
    // }
}
