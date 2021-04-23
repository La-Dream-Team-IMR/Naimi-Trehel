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
        System.out.println(id + " : Start");
        while(true) {
            long randomTime = (long) (Math.random() * 1000);
            try {
                sleep(randomTime);
                queue.put(StateQueue.Demander);
                System.out.println(color + id +" : J'attends la section critique" + ConsoleColors.RESET);
                queue.take();
                System.out.println(color + id + " : J'utilise la section Critique Youpi" + ConsoleColors.RESET);
                sleep(5000);
                queue.put(StateQueue.Quitter);
                queue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}