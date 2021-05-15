import java.time.*;

public class ConsoleUtils {

    public static void debug(String textToDebug, String color){
        LocalDateTime timeStamp = LocalDateTime.now();
        System.out.println("[" + timeStamp.getHour()+":"+timeStamp.getMinute()+":"+timeStamp.getSecond()+"]" + " : " + color + textToDebug + ConsoleColors.RESET);
    }
    public static void debug(String textToDebug){
        debug(textToDebug, ConsoleColors.WHITE);
    }
}
