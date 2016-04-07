First, you should run all of the junit tests. They should all pass. You might want to enable asserts first.
src/SlidingWindow/SpaceSaving.java is the implementation of CSS.
It is implemented without keeping the total count of large items, so src/SlidingWindow/CountingSpaceSaving.java takes care of that (quite inefficiently).
src/SlidingWindow/Sliwi.java is the implementation of WCSS and relies on SpaceSaving.java
src/HashTables/TinyTableWithValues is our implementation of TinyTable, the hash table we rely on for this implementation.
