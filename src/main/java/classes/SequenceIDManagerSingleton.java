package classes;

public class SequenceIDManagerSingleton {
    private static int sequenceIDCounter = 0;

    public static int getSequenceIDCounter() { return sequenceIDCounter; }
    public static void incrementSequenceIDCounter() { sequenceIDCounter++; }
}
