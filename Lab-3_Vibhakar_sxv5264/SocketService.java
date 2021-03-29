//Name : Sejal Vibhakar
//ID : 1001765264
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

// Class written to manage efficient 2-way communication on sockets using Queues.
// Entire logic is handwritten, not copied from any source.
public class SocketService<T> {

    private ConcurrentHashMap<String, ConcurrentLinkedDeque<T>> queues;
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private Exception exOccurred;

    public SocketService(final Socket socket) {
        try {
            this.dos = new ObjectOutputStream(socket.getOutputStream());
            this.dis = new ObjectInputStream(socket.getInputStream());
            this.queues = new ConcurrentHashMap<>();
            this.queues.put("ReaderQueue", new ConcurrentLinkedDeque<>());
            this.queues.put("WriterQueue", new ConcurrentLinkedDeque<>());
            init();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void panic() throws Exception {
        if (this.exOccurred != null) {
            throw exOccurred;
        }
    }

    public void sendMessage(final T message) throws Exception {
        panic();
        this.queues.get("WriterQueue").add(message);
    }

    public T getMessage() throws Exception {
        panic();
        if (!this.queues.get("ReaderQueue").isEmpty()) {
            return this.queues.get("ReaderQueue").remove();
        }
        return null;
    }

    private T getWriteMessage() {
        if (!this.queues.get("WriterQueue").isEmpty()) {
            return this.queues.get("WriterQueue").remove();
        }
        return null;
    }

    private void receiveMessage(final T message) {
        this.queues.get("ReaderQueue").add(message);
    }

    private void init() {

        final Thread reader = new Thread(new Runnable() {
            @SuppressWarnings("unchecked")
			@Override
            public void run() {
                final SocketService<T> _t = SocketService.this;
                try {
                    while (true) {
                        if (_t.exOccurred != null) {
                            System.out.println("Exiting reader thread");
                            return;
                        }
                        final T incomingMessage = (T) _t.dis.readObject();
                        _t.receiveMessage(incomingMessage);
                        Thread.yield();
                    }
                } catch (final Exception e) {
                    _t.exOccurred = e;
                }
            }
        });

        final Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {
                final SocketService<T> _t = SocketService.this;
                try {
                    while (true) {
                        if (_t.exOccurred != null) {
                            System.out.println("Exiting writer thread");
                            return;
                        }
                        final T messageToSend = _t.getWriteMessage();
                        if (messageToSend != null)
                            _t.dos.writeObject(messageToSend);
                        Thread.yield();
                    }
                } catch (final IOException e) {
                    _t.exOccurred = e;
                }
            }
        });

        reader.start();
        writer.start();
    }

}
