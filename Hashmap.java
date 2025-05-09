//MXB220041 - Mamoudou Balde
import java.util.LinkedList;


public class Hashmap {
    //Default initial size of the hash table
    private static final int DEFAULT_SIZE = 5;
    //Threshold for rehashing - when average chain length exceeds this value
    private static final double LOAD_FACTOR_THRESHOLD = 2.0;
    
    //Array of linked lists to handle collisions through chaining
    private LinkedList<Entry>[] buckets;
    //Current size of the hash table (number of buckets)
    private int size;
    //Total number of key-value pairs stored
    private int numEntries;
    
    //Default constructor using default size
    public Hashmap() {
        this(DEFAULT_SIZE);
    }
    
    //Constructor with custom initial size
    //Suppresses unchecked warning for creating array of generic types
    @SuppressWarnings("unchecked")
    public Hashmap(int initialSize) {
        buckets = new LinkedList[initialSize];
        //Initialize each bucket with an empty linked list
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new LinkedList<>();
        }
        size = initialSize;
        numEntries = 0;
    }
    
    //Inserts or updates a key-value pair in the hash table
    public void put(String key, Customer value) {
        //Check if load factor exceeds threshold and rehash if needed
        if (getLoadFactor() > LOAD_FACTOR_THRESHOLD) {
            rehash();
        }
        
        //Calculate the bucket index for this key
        int index = getIndex(key);
        
        //Check if key already exists and update value if found
        for (Entry entry : buckets[index]) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }
        
        //Key doesn't exist, add new entry to the bucket
        buckets[index].add(new Entry(key, value));
        numEntries++;
    }
    
    //Retrieves the value associated with the given key
    //Returns null if key is not found
    public Customer get(String key) {
        int index = getIndex(key);
        
        //Search through the bucket for the key
        for (Entry entry : buckets[index]) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        
        //Key not found
        return null;
    }
    
    //Checks if the hash table contains the specified key
    public boolean containsKey(String key) {
        int index = getIndex(key);
        
        //Search through the bucket for the key
        for (Entry entry : buckets[index]) {
            if (entry.key.equals(key)) {
                return true;
            }
        }
        
        //Key not found
        return false;
    }
    
    //Removes the key-value pair for the specified key
    public void remove(String key) {
        int index = getIndex(key);
        
        //Search and remove entry if found
        for (int i = 0; i < buckets[index].size(); i++) {
            Entry entry = buckets[index].get(i);
            if (entry.key.equals(key)) {
                buckets[index].remove(i);
                numEntries--;
                return;
            }
        }
    }
    
    //Returns the number of key-value pairs in the hash table
    public int size() {
        return numEntries;
    }
    
    //Checks if the hash table is empty
    public boolean isEmpty() {
        return numEntries == 0;
    }
    
    //Calculates the bucket index for a given key using hash function
    //Uses modulo to ensure index is within bounds
    private int getIndex(String key) {
        return Math.abs(key.hashCode()) % size;
    }
    
    //Calculates the load factor (average chain length)
    //Used to determine when to rehash the table
    private double getLoadFactor() {
        if (size == 0) return 0;
        return (double) numEntries / size;
    }
    
    //Rehashes the table when load factor exceeds threshold
    //Doubles the size of the hash table and redistributes all entries
    @SuppressWarnings("unchecked")
    private void rehash() {
        //Create a larger hash table
        int newSize = size * 2;
        LinkedList<Entry>[] oldBuckets = buckets;
        
        //Create new array with doubled size
        buckets = new LinkedList[newSize];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new LinkedList<>();
        }
        
        //Update size and reset entry count
        size = newSize;
        numEntries = 0;
        
        //Reinsert all entries from old table into new table
        for (LinkedList<Entry> bucket : oldBuckets) {
            for (Entry entry : bucket) {
                put(entry.key, entry.value);
            }
        }
    }
    
    //Private inner class to represent key-value pairs
    private static class Entry {
        //Key for the hash table entry
        String key;
        //Value associated with the key
        Customer value;
        
        //Constructor to create a new entry
        Entry(String key, Customer value) {
            this.key = key;
            this.value = value;
        }
    }
}