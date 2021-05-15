import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class Site {
    public static void main(String[] args) throws Exception{
        int id = Integer.parseInt(args[0]);
        int idPere = Integer.parseInt(args[1]);
        String color = getColor(id);
        BlockingQueue<StateQueue> queue = new SynchronousQueue<StateQueue>();
        ProcessA A = new ProcessA(id, queue, color);
        ControleurInterface P = new ControleurNaimiTrehel(id, idPere, queue, color);
        System.out.println("Je suis " + id + " et mon papa est " + idPere);
        A.start();
        P.run();
        A.join();
    }

    private static String getColor(int id){
        switch (id) {
            case 1:
                return ConsoleColors.BLUE;
            case 2:
                return ConsoleColors.YELLOW;
            case 3:
                return ConsoleColors.PURPLE;
            case 4:
                return ConsoleColors.CYAN;
            case 5:
                return ConsoleColors.GREEN;
            case 6:
                return ConsoleColors.RED;
            case 7:
                return ConsoleColors.WHITE_UNDERLINED;
            default:
                return ConsoleColors.WHITE;
        }
    }
}