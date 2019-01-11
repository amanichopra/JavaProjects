import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ChatFilter chatFilter;
    static int anonymousCounter = 1;


    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer(int port, ChatFilter chatFilter) {
        this(port);
        this.chatFilter = chatFilter;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                //System.out.println(InetAddress.getLocalHost());
                System.out.println(sdf.format(date) + " Server waiting for clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                System.out.println(sdf.format(date) + " " + ((ClientThread) r).username + " just connected.");
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
//                if (!((ClientThread) r).socket.isConnected()) {
//                    ((ClientThread) r).remove(uniqueId);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        //int port = 1500;
        ChatServer server;
        if (args.length == 0) {
            int port = 1500;
            server = new ChatServer(port);
        } else if (args.length == 1) {
            int port = Integer.parseInt(args[0]);
            server = new ChatServer(port);
        } else if (args.length == 2) {
            int port = Integer.parseInt(args[0]);
            String badWordsFileName = args[1];
            ChatFilter chatFilter;
            try {
                chatFilter = new ChatFilter(badWordsFileName);
            } catch (FileNotFoundException e) {
                System.out.println("Error: File does not exist! Try again.");
                return;
            }

            server = new ChatServer(port, chatFilter);
            System.out.println("Banned Words File: " + badWordsFileName);
            System.out.println("Banned Words:");
            for (int i = 0; i < chatFilter.getBadWords().size(); i++) {
                System.out.println(chatFilter.getBadWords().get(i));
            }
            System.out.println();
        } else {
            server = null;
        }

        //ChatServer server = new ChatServer(port);
        server.start();

    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                if (username.equals("Anonymous")) {
                    username = "Anonymous_" + anonymousCounter++;
                }

//                int counter = 0;
//                for (int i = 0; i < clients.size(); i++) {
//                    if (clients.get(i).equals(username)) {
//                        sOutput.writeInt(1); //error
//                    } else {
//                       counter++;
//                    }
//                }
//
//                if (counter == clients.size()) {
//                    sOutput.writeInt(0); //normal
//                }

//                String[] usernames = new String[clients.size()];
//                for (int i = 0; i < clients.size(); i++) {
//                    usernames[i] = (clients.get(i).username);
//                }
//                sOutput.writeObject(usernames);

//                for (int i = 0; i < clients.size(); i++) {
//                    if (clients.get(i).username.equals(username)) {
//                        clients.get(i).sOutput.writeObject("Error: The username is already taken!\n");
//                        close();
//                        remove(id);
//                    }
//                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String msg) {
            if (!socket.isConnected()) {
                return false;
            }
            try {
                sOutput.writeObject(msg + "\n");
            } catch (IOException e) {

            }
            return true;
        }

        private synchronized void broadcast(String message) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            if (chatFilter != null) {
                message = chatFilter.filter(message);
            }

            for (int i = 0; i < clients.size(); i++) {
                try {
                    clients.get(i).sOutput.writeObject(sdf.format(date) + " " + username + ": ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.get(i).writeMessage(message);
            }
            System.out.println(sdf.format(date) + " " + username + ": " + message);
        }

        private void directMessage(String message, String username) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            if (chatFilter != null) {
                message = chatFilter.filter(message);
            }
            int counter = 0;


            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username) || clients.get(i).username.equals(this.username)) {
                    try {
                        clients.get(i).sOutput.writeObject(sdf.format(date) + " " + this.username + " -> " + username +
                                ": ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clients.get(i).writeMessage(message);
                    counter++;

                }
            }

            if (counter == 1) {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).username.equals(this.username)) {
                        try {
                            clients.get(i).sOutput.writeObject("Message could not successfully send. User does not " +
                                    "exist!\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            } else {
                System.out.println(sdf.format(date) + " " + this.username + " -> " + username + ": " + message);
            }

        }

        private void listUsers() {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username == this.username) {
                    try {
                        if (clients.size() == 1) {
                            clients.get(0).sOutput.writeObject("No other clients connected.\n");
                        }
                        for (int j = 0; j < clients.size(); j++) {
                            if (clients.get(j).username != this.username) {
                                clients.get(i).sOutput.writeObject(clients.get(j).username + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private synchronized void remove(int id) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id == id) {
                    clients.remove(i);
                }
            }
        }

        private void close() {
            try {
                socket.close();
                sInput.close();
                sOutput.close();
            } catch (IOException e) {

            }

        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(sdf.format(date) + " " + username + " forcefully disconnected.");
                    remove(id);
                    break;
                }

                if (cm.getType() == 0) {
                    broadcast(cm.getMessage());
                } else if (cm.getType() == 1) {
                    close();
                    remove(id);
                    System.out.println(sdf.format(date) + " " + username + " disconnected with a LOGOUT message.");
                    break;
                } else if (cm.getType() == 2) {
                    directMessage(cm.getMessage(), cm.getRecipient());
                } else if (cm.getType() == 4) {
                    listUsers();
                }
            }
            //remove(id);


//            String msg = cm.getMessage();
//            System.out.println(username + ": " + msg);
//
//
//            // Send message back to the client
//            try {
//                sOutput.writeObject("Pong");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
