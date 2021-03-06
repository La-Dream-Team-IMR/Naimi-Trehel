import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ProcessA extends Thread {
    BlockingQueue<StateQueue> queue;
    int id;
    String color;

    public ProcessA(int id, BlockingQueue<StateQueue> queue, String color) throws Exception {
        this.queue = queue;
        this.id = id;
        this.color = color;
    }

    @Override
    public void run() {
        ConsoleUtils.debug("A" + id + " : Start", color);
        while (true) {
            long randomTime = (long) (Math.random() * 5000);
            try {
                sleep(randomTime);
                queue.put(StateQueue.Demander);
                ConsoleUtils.debug("A" + id + " : J'attends la section critique", color);
                queue.take(); // Expecting Autoriser
                ConsoleUtils.debug("A" + id + " : J'utilise la section Critique Youpi", color);
                // sleep(5000);
                usePrisme();
                ConsoleUtils.debug("A" + id + " : Je lache la section Critique", color);
                queue.put(StateQueue.Quitter);
                queue.take(); // Expecting Ack
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void usePrisme() throws Exception {
        Socket socket = new Socket("127.0.0.1", 8080);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // "'out.println("A"+id + " : J'utilise le prisme");
        // out.print("END\n");

        out.println(color + "A" + id + " : J'utilise le prisme" + ConsoleColors.RESET);
        //out.flush();
        sleep(100);

        /*char buf[] = new char[100];
        in.read(buf);*/

        out.println("END");
        //out.flush();

        sleep(50);

        //ConsoleUtils.debug(String.valueOf(buf), color);
        socket.close();
        // sleep(5000);
    }
}