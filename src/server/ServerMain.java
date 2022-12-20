package server;

import internal.*;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

public class ServerMain {

    private static final int MAX_USERS = 100;

    public static void main(String[] args) {

        try (ServerSocket serv = new ServerSocket(Protocol.PORT)) {
            //serv.setSoTimeout(5000000);
            System.err.println("initialized");
            ServerStopThread tester = new ServerStopThread();
            tester.start();
            do {
                Socket sock = accept(serv);
                if (sock != null) {
                    if (ServerMain.getNumUsers() < ServerMain.MAX_USERS) {
                        System.err.println(sock.getInetAddress().getHostName() + " connected");
                        ServerThread server = new ServerThread(sock);
                        server.start();
                    } else {
                        System.err.println(sock.getInetAddress().getHostName() + " connection rejected");
                        sock.close();
                    }
                }
            } while (!ServerMain.getStopFlag());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            stopAllUsers();
            System.err.println("stopped");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    public static Socket accept(ServerSocket serv) {
        assert (serv != null);
        try {
            serv.setSoTimeout(5000);
            return serv.accept();
        } catch (IOException ignored) {
        }
        return null;
    }

    private static void stopAllUsers() {
        String[] nic = getUsers();
        for (String user : nic) {
            ServerThread ut = getUser(user);
            if (ut != null) {
                ut.disconnect();
            }
        }
    }

    private static final Object syncFlags = new Object();
    private static boolean stopFlag = false;

    public static boolean getStopFlag() {
        synchronized (ServerMain.syncFlags) {
            return stopFlag;
        }
    }

    public static void setStopFlag(boolean value) {
        synchronized (ServerMain.syncFlags) {
            stopFlag = value;
        }
    }

    private static final Object syncUsers = new Object();
    private static final TreeMap<String, ServerThread> users =
            new TreeMap<>();

    public static ServerThread getUser(String userNic) {
        synchronized (ServerMain.syncUsers) {
            return ServerMain.users.get(userNic);
        }
    }

    public static ServerThread registerUser(String userNic, ServerThread user) {
        synchronized (ServerMain.syncUsers) {
            return ServerMain.users.putIfAbsent(userNic, user);
        }
    }

    public static ServerThread setUser(String userNic, ServerThread user) {
        synchronized (ServerMain.syncUsers) {
            ServerThread res = ServerMain.users.put(userNic, user);
            if (user == null) {
                ServerMain.users.remove(userNic);
            }
            return res;
        }
    }

    public static String[] getUsers() {
        synchronized (ServerMain.syncUsers) {
            return ServerMain.users.keySet().toArray(new String[0]);
        }
    }

    public static int getNumUsers() {
        synchronized (ServerMain.syncUsers) {
            return ServerMain.users.keySet().size();
        }
    }
}

class ServerStopThread extends CommandThread {

    static final String cmd = "q";
    static final String cmdL = "quit";

    Scanner fin;

    public ServerStopThread() {
        fin = new Scanner(System.in);
        ServerMain.setStopFlag(false);
        putHandler(cmd, cmdL, errorCode -> onCmdQuit());
        this.setDaemon(true);
        System.err.println("Enter '" + cmd + "' or '" + cmdL + "' to stop server\n");
    }

    public void run() {

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            if (!fin.hasNextLine())
                continue;
            String str = fin.nextLine();
            if (command(str)) {
                break;
            }
        }
    }

    public boolean onCmdQuit() {
        System.err.print("stop server...");
        fin.close();
        ServerMain.setStopFlag(true);
        return true;
    }
}

class ServerThread extends Thread {

    private final Socket sock;
    private final DataOutputStream os;
    private final DataInputStream is;
    private final InetAddress addr;

    private String userNic = null;
    private String userFullName;

    private final Object syncLetters = new Object();
    private Vector<String> letters = null;

    private final Object syncFiles = new Object();

    private Vector<CustomFile> files = null;

    private final int MAX_FILE_COUNT = 100;

    private final int MAX_LETTER_COUNT = 100;

    public int addLetter(String letter) {
        synchronized (syncLetters) {
            if (letters == null) {
                letters = new Vector<>();
            }
            if (letters.size() >= MAX_LETTER_COUNT) {
                return 1;
            }
            letters.add(letter);
        }
        return 0;
    }


    public String[] getLetters() {
        synchronized (syncLetters) {
            String[] lts = new String[0];
            synchronized (syncLetters) {
                if (letters != null) {
                    lts = letters.toArray(lts);
                    letters = null;
                }
            }
            return lts;
        }
    }


    public int addFile(CustomFile file) {
        synchronized (syncFiles) {
            if (files == null) {
                files = new Vector<>();
            }
            if (files.size() >= MAX_FILE_COUNT)
                return 1;
            files.add(file);
        }
        return 0;
    }

    public CustomFile[] getFiles() {
        synchronized (syncFiles) {
            CustomFile[] ffiles = new CustomFile[0];
            synchronized (syncFiles) {
                if (files != null) {
                    ffiles = files.toArray(ffiles);
                    files = null;
                }
            }
            return ffiles;
        }
    }

    public ServerThread(Socket s) throws IOException {
        sock = s;
        s.setSoTimeout(1000);
        os = new DataOutputStream(s.getOutputStream());
        is = new DataInputStream(s.getInputStream());
        addr = s.getInetAddress();
        this.setDaemon(true);
    }

    public void run() {
        try {
            while (true) {
                Message msg = null;
                try {
                    msg = (Message) MessageXml.readMsg(is);
                } catch (IOException | ClassNotFoundException ignored) {
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
                if (msg != null) switch (msg.getID()) {

                    case Protocol.CMD_CONNECT:
                        if (!connect((MessageConnect) msg))
                            return;
                        break;

                    case Protocol.CMD_DISCONNECT:
                        return;

                    case Protocol.CMD_USER:
                        user((MessageUser) msg);
                        break;

                    case Protocol.CMD_CHECK_MAIL:
                        checkMail((MessageCheckMail) msg);
                        break;

                    case Protocol.CMD_LETTER:
                        letter((MessageLetter) msg);
                        break;
                    case Protocol.CMD_IMAGE:
                        image((MessageFile) msg);
                        break;
                    case Protocol.CMD_CHECK_FILES:
                        checkFiles((MessageCheckFiles) msg);
                        break;
                }
            }
        } catch (IOException | JAXBException e) {
            System.err.print("Disconnect...");
        } finally {
            disconnect();
        }
    }

    boolean connect(MessageConnect msg) throws IOException, JAXBException {

        ServerThread old = register(msg.userNic, msg.userFullName);
        if (old == null) {
            MessageXml.writeMsg(os, new MessageConnectResult());
            return true;
        } else {
            MessageXml.writeMsg(os, new MessageConnectResult(
                    "User " + old.userFullName + " already connected as " + userNic));
            return false;
        }
    }

    void image(MessageFile msg) throws IOException, JAXBException {
        ServerThread user = ServerMain.getUser(msg.usrNic);
        if (user == null) {
            MessageXml.writeMsg(os, new MessageFileResult(
                    "User " + msg.usrNic + " is not found"));
        } else {
            if (user.addFile(msg.file) == 1) {
                MessageXml.writeMsg(os, new MessageFileResult("Overloaded"));
            } else
                MessageXml.writeMsg(os, new MessageFileResult());
        }
    }

    void letter(MessageLetter msg) throws IOException, JAXBException {

        ServerThread user = ServerMain.getUser(msg.usrNic);
        if (user == null) {
            MessageXml.toXml(new MessageLetterResult(
                    "User " + msg.usrNic + " is not found"), os);
        } else {
            if (user.addLetter(userNic + ": " + msg.txt) == 1) {
                MessageXml.toXml(new MessageLetterResult("Overloaded"), os);
            }
            else
                MessageXml.toXml(new MessageLetterResult(), os);
        }
    }

    void user(MessageUser msg) throws IOException, JAXBException {

        String[] nics = ServerMain.getUsers();
        if (nics.length > 0)
            MessageXml.toXml(new MessageUserResult(nics), os);
        else
            MessageXml.toXml(new MessageUserResult("Unable to get users list"), os);
    }

    void checkMail(MessageCheckMail msg) throws IOException, JAXBException {

        String[] lts = getLetters();
        if (lts != null)
            MessageXml.toXml(new MessageCheckMailResult(lts), os);
        else
            MessageXml.toXml(new MessageCheckMailResult("Unable to get mail"), os);
    }

    void checkFiles(MessageCheckFiles msg) throws IOException, JAXBException {
        CustomFile[] lts = getFiles();
        if (lts.length > 0)
            MessageXml.toXml(new MessageCheckFilesResult(lts), os);
        else
            MessageXml.toXml(new MessageCheckFilesResult("Unable to get files"), os);

    }

    private boolean disconnected = false;

    public void disconnect() {
        if (!disconnected)
            try {
                System.err.println(addr.getHostName() + " disconnected");
                unregister();
                os.close();
                is.close();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.interrupt();
                disconnected = true;
            }
    }

    private void unregister() {
        if (userNic != null) {
            ServerMain.setUser(userNic, null);
            userNic = null;
        }
    }

    private ServerThread register(String nic, String name) {
        ServerThread old = ServerMain.registerUser(nic, this);
        if (old == null) {
            if (userNic == null) {
                userNic = nic;
                userFullName = name;
                System.err.println("User '" + name + "' registered as '" + nic + "'");
            }
        }
        return old;
    }
}

