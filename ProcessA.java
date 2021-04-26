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
                queue.take(); //Expecting Autoriser
                ConsoleUtils.debug("A" + id + " : J'utilise la section Critique Youpi", color);
                sleep(5000);
                ConsoleUtils.debug("A" + id + " : Je lache la section Critique", color);
                queue.put(StateQueue.Quitter);
                queue.take(); //Expecting Ack
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}