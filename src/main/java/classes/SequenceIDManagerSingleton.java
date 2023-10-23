package classes;

/**
 * class SequenceIDManagerSingleton
 * responsible for assigning every distinct message a seq number
 */
public class SequenceIDManagerSingleton {
    private static int sequenceIDCounter = 0;

    public static int getSequenceIDCounter() { return sequenceIDCounter; }
    public static void incrementSequenceIDCounter() { sequenceIDCounter++; }
}
