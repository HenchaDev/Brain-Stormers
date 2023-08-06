import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmailServer {
    private static Map<String, String> users = new HashMap<>();
    private static Map<String, StringBuilder> userInboxes = new HashMap<>();

    public static void main(String[] args) {
        // Initialize users (Replace with database implementation)
        users.put("user1@example.com", "password1");
        users.put("user2@example.com", "password2");

        try {
            ServerSocket serverSocket = new ServerSocket(25000);
            System.out.println("Email Server started on port 25000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientRequest = reader.readLine();
            if (clientRequest.startsWith("LOGIN")) {
                String[] credentials = clientRequest.split(" ");
                String email = credentials[1];
                String password = credentials[2];

                if (verifyCredentials(email, password)) {
                    writer.println("OK");
                    handleEmailClient(reader, writer, email);
                } else {
                    writer.println("INVALID");
                }
            } else {
                writer.println("INVALID REQUEST");
            }
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean verifyCredentials(String email, String password) {
        return users.containsKey(email) && users.get(email).equals(password);
    }

    private static void handleEmailClient(BufferedReader reader, PrintWriter writer, String email) throws IOException {
        writer.println("Welcome to Umail! You are logged in as " + email);
        while (true) {
            String clientRequest = reader.readLine();
            if (clientRequest.equals("SEND")) {
                String recipients = reader.readLine();
                String subject = reader.readLine();
                String body = reader.readLine();

                String messageId = generateMessageId();
                String message = "From: " + email + "\r\n" +
                        "To: " + recipients + "\r\n" +
                        "Subject: " + subject + "\r\n" +
                        "Date: " + System.currentTimeMillis() + "\r\n" +
                        "Message-ID: " + messageId + "\r\n" +
                        "Keywords: Umail, Java\r\n" +
                        "\r\n" + body;

                storeInInbox(recipients, message);

                writer.println("Email sent successfully!");
            } else if (clientRequest.equals("FETCH")) {
                StringBuilder inbox = userInboxes.getOrDefault(email, new StringBuilder());
                writer.println(inbox.toString());
            } else if (clientRequest.equals("LOGOUT")) {
                break;
            } else {
                writer.println("INVALID REQUEST");
            }
        }
    }

    private static void storeInInbox(String recipients, String message) {
        String[] recipientList = recipients.split(",");
        for (String recipient : recipientList) {
            StringBuilder inbox = userInboxes.getOrDefault(recipient.trim(), new StringBuilder());
            inbox.append(message).append("\n");
            userInboxes.put(recipient.trim(), inbox);
        }
    }

    private static String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
