import java.io.*;
import java.net.Socket;

public class EmailClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 25000);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to Email Server");
            System.out.print("Enter your email address: ");
            String email = userInputReader.readLine();
            System.out.print("Enter your password: ");
            String password = userInputReader.readLine();

            // Sending login request to the server
            writer.println("LOGIN " + email + " " + password);

            String response = reader.readLine();
            if (response.equals("OK")) {
                System.out.println("Login successful!");
                handleClientInteraction(userInputReader, writer);
            } else {
                System.out.println("Login failed. Invalid credentials.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientInteraction(BufferedReader userInputReader, PrintWriter writer) throws IOException {
        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Send an email");
            System.out.println("2. Fetch inbox");
            System.out.println("3. Logout");
            System.out.print("Enter option number: ");

            String option = userInputReader.readLine();

            if (option.equals("1")) {
                sendEmail(userInputReader, writer);
            } else if (option.equals("2")) {
                fetchInbox(writer);
            } else if (option.equals("3")) {
                writer.println("LOGOUT");
                System.out.println("Logged out successfully.");
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void sendEmail(BufferedReader userInputReader, PrintWriter writer) throws IOException {
        System.out.print("Enter recipients (comma-separated email addresses): ");
        String recipients = userInputReader.readLine();
        System.out.print("Enter subject: ");
        String subject = userInputReader.readLine();
        System.out.print("Enter body: ");
        String body = userInputReader.readLine();

        // Sending email data to the server
        writer.println("SEND");
        writer.println(recipients);
        writer.println(subject);
        writer.println(body);

        // Read and display the server's response
        String response = userInputReader.readLine();
        System.out.println(response);
    }

    private static void fetchInbox(PrintWriter writer) {
        // Requesting the server to fetch the inbox
        writer.println("FETCH");
        // The server will respond and display the inbox
    }
}
