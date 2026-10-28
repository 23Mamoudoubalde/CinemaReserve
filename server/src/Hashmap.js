// JavaScript port of Hashmap.java: a hash table with separate chaining.
// Each bucket is an array of [key, value] pairs. When the average chain
// length passes LOAD_FACTOR_THRESHOLD, the table doubles and rehashes.

const DEFAULT_SIZE = 5;
const LOAD_FACTOR_THRESHOLD = 2.0;

// Same idea as Java's String.hashCode(): s[0]*31^(n-1) + ... + s[n-1]
function hashCode(key) {
  let h = 0;
  for (let i = 0; i < key.length; i++) {
    h = (Math.imul(31, h) + key.charCodeAt(i)) | 0;
  }
  return h;
}

export class Hashmap {
  constructor(initialSize = DEFAULT_SIZE) {
    this.buckets = Array.from({ length: initialSize }, () => []);
    this.numEntries = 0;
  }

  #index(key) {
    return Math.abs(hashCode(key)) % this.buckets.length;
  }

  #loadFactor() {
    return this.numEntries / this.buckets.length;
  }

  #rehash() {
    const old = this.buckets;
    this.buckets = Array.from({ length: old.length * 2 }, () => []);
    this.numEntries = 0;
    for (const bucket of old) {
      for (const [key, value] of bucket) this.put(key, value);
    }
  }

  put(key, value) {
    if (this.#loadFactor() > LOAD_FACTOR_THRESHOLD) this.#rehash();
    const bucket = this.buckets[this.#index(key)];
    const entry = bucket.find(([k]) => k === key);
    if (entry) {
      entry[1] = value;
      return;
    }
    bucket.push([key, value]);
    this.numEntries++;
  }

  get(key) {
    const entry = this.buckets[this.#index(key)].find(([k]) => k === key);
    return entry ? entry[1] : null;
  }

  containsKey(key) {
    return this.get(key) !== null;
  }

  remove(key) {
    const bucket = this.buckets[this.#index(key)];
    const i = bucket.findIndex(([k]) => k === key);
    if (i !== -1) {
      bucket.splice(i, 1);
      this.numEntries--;
    }
  }

  size() {
    return this.numEntries;
  }

  bucketCount() {
    return this.buckets.length;
  }
}
