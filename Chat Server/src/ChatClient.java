import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Error: You must first start the server!");
            return false;
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            //print on terminalside when socket is closed
           e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String server = "localhost";
        int port = 1500;
        String username = "Anonymous";

        if (args.length == 1) {
            username = args[0];
        } else if (args.length == 2) {
            username = args[0];
            port = Integer.parseInt(args[1]);
        } else if (args.length == 3) {
            username = args[0];
            port = Integer.parseInt(args[1]);
            server = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(server, port, username);
        if (!client.start()) {
            return;
        }

//        try {
//            int error = client.sInput.readInt();
//            System.out.println(error);
//            if (error == 1) {
//                System.out.println("Error: Username already taken!");
//                return;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        try {
//            String[] usernames = (String[]) client.sInput.readObject();
//            for (int i = 0; i < usernames.length; i++) {
//                if (username.equals(usernames[i])) {
//                    System.out.println("Error: Username already taken!");
//                    return;
//                }
//            }
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        System.out.println("Connection accepted " + client.socket.getInetAddress() + ":" + port);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            int type = 0;
            String recipient = null;
            if (message.equals("/logout")) {
                type = 1; //logout
            } else if (message.indexOf("/msg") == 0) {
                type = 2; //direct msg
                try {
                    recipient = message.substring(5, message.indexOf(" ", message.indexOf('g') + 2));
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Incorrect format! Enter \"/help\" for instructions.");
                    continue;
                }

                if (username.equals(recipient)) {
                    type = 3; //attempt to msg self
                }
            } else if (message.equals("/list")) {
                type = 4; //attempt to list users
            } else if (message.equals("/help")) {
                System.out.println("Choose from the following commands: \"/logout\" (to logout from the server), " +
                        "\"/msg [insert valid username] [insert message]\" (to direct message another user), and " +
                        "\"/list\" (to list users connected to server).");
                continue;
            }
            // Send an empty message to the server

            if (type == 1) {
                client.sendMessage(new ChatMessage(type, message));
                try {
                    client.socket.close();
                    client.sInput.close();
                    client.sOutput.close();
                    System.out.println("Server has closed the connection.");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (type == 2) {
                client.sendMessage(new ChatMessage(type, message.substring(message.indexOf(" ", message.indexOf('g') + 2) + 1), recipient));
            } else if (type == 3) {
                System.out.println("Error: You cannot direct message yourself!");
            } else {
                client.sendMessage(new ChatMessage(type, message));
            }
        }
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {

            try {
                while (true) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                }

            } catch (IOException | ClassNotFoundException e) {
                return;
            }
        }
    }
}
