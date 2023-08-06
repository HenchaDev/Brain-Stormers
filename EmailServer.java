import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailServerClient {
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        boolean isServer = args.length > 0 && args[0].equalsIgnoreCase("server");

        if (isServer) {
            startServer();
        } else {
            startClient();
        }
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Email server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient() {
        final String SERVER_ADDRESS = "localhost";

        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Connected to Umail.com email server");

            String line;
            while ((line = consoleReader.readLine()) != null) {
                if ("QUIT".equalsIgnoreCase(line)) {
                    writer.println(line);
                    System.out.println("Connection closed");
                    break;
                }

                writer.println(line);
                String response = reader.readLine();
                System.out.println("Server: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String request;
            while ((request = reader.readLine()) != null) {
                if ("QUIT".equalsIgnoreCase(request)) {
                    writer.println("221 Bye");
                    break;
                } else if ("HELO".equalsIgnoreCase(request)) {
                    writer.println("250 Hello");
                } else if (request.startsWith("MAIL FROM:")) {
                    writer.println("250 Sender OK");
                } else if (request.startsWith("RCPT TO:")) {
                    writer.println("250 Recipient OK");
                } else if ("DATA".equalsIgnoreCase(request)) {
                    writer.println("354 Enter message, ending with \".\" on a line by itself.");
                    handleEmailData(reader);
                    writer.println("250 Message received");
                } else {
                    writer.println("500 Syntax error, command unrecognized");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleEmailData(BufferedReader reader) throws IOException {
        Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);

        String line;
        StringBuilder data = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.equals(".")) {
                break;
            }
            data.append(line).append("\r\n");
        }

        try {
            message.setText(data.toString());

            // Set additional headers
            message.setHeader("Message-ID", "<" + UUID.randomUUID().toString() + "@umail.com>");
            message.setHeader("Keywords", "test, email");
            message.setHeader("X-Mailer", "Umail.com Java Email Client/Server");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
