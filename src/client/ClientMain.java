package client;

import internal.*;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientMain {
    // arguments: userNic userFullName [host]
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Invalid number of arguments\n" + "Use: nic name [host]");
            waitKeyToStop();
            return;
        }
        try (Socket sock = (args.length == 2 ?
                new Socket(InetAddress.getLocalHost(), Protocol.PORT) :
                new Socket(args[2], Protocol.PORT))) {
            System.err.println("initialized");
            session(sock, args[0], args[1]);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            System.err.println("bye...");
        }
    }

    static void waitKeyToStop() {
        System.err.println("Press a key to stop...");
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
    }

    static class Session {
        boolean connected = false;
        String userNic;
        String userName;

        Session(String nic, String name) {
            userNic = nic;
            userName = name;
        }
    }

    static void session(Socket s, String nic, String name) {
        try (Scanner in = new Scanner(System.in);
             DataInputStream is = new DataInputStream(s.getInputStream());
             DataOutputStream os = new DataOutputStream(s.getOutputStream())) {
            Session ses = new Session(nic, name);
            boolean oss = openSession(ses, is, os, in);
            if (oss) {
                try {
                    while (true) {
                        Message msg = getCommand(in);
                        MessageXml.query(msg, is, os, MessageXml.classResultById(msg.getID()));
                    }
                } finally {
                    closeSession(ses, os);
                }
            }
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    static boolean openSession(Session ses, DataInputStream is, DataOutputStream os, Scanner in)
            throws JAXBException, IOException, ClassNotFoundException {
        MessageXml.writeMsg(os, new MessageConnect(ses.userNic, ses.userName));

        
        MessageConnectResult msg = null;
        try {
            msg = (MessageConnectResult) MessageXml.readMsg(is);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        if (!msg.Error()) {
            System.err.println("connected");
            ses.connected = true;
            return true;
        }
        System.err.println("Unable to connect: " + msg.getErrorMessage());
        System.err.println("Press <Enter> to continue...");
        if (in.hasNextLine())
            in.nextLine();
        return false;
    }

    static void closeSession(Session ses, DataOutputStream os) throws JAXBException {
        if (ses.connected) {
            ses.connected = false;
            MessageXml.toXml(new MessageDisconnect(), os);
        }
    }

    static Message getCommand(Scanner in) throws IOException {
        while (true) {
            printPrompt();
            if (!in.hasNextLine())
                break;
            String str = in.nextLine();
            byte cmd = translateCmd(str);
            switch (cmd) {
                case -1:
                    return null;
                case Protocol.CMD_CHECK_MAIL:
                    return new MessageCheckMail();
                case Protocol.CMD_USER:
                    return new MessageUser();
                case Protocol.CMD_LETTER:
                    return inputLetter(in);

                case Protocol.CMD_IMAGE:
                    return inputImage(in);
                case Protocol.CMD_CHECK_FILES:
                    return new MessageCheckFiles();
                case 0:
                    continue;
                default:
                    System.err.println("Unknown command!");
            }
        }
        return null;
    }

    static MessageFile inputImage(Scanner in) throws IOException {
        String usrNic, path;
        System.out.print("Enter user NIC: ");
        usrNic = in.nextLine();
        System.out.print("Enter absolute path : ");
        path = in.nextLine();
        File myFile = new File(path);
        byte[] fileArray = new byte[(int) myFile.length()];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(fileArray, 0, fileArray.length);
        CustomFile file = new CustomFile(myFile.getName(), fileArray);
        return new MessageFile(usrNic, path, file);

    }

    static MessageLetter inputLetter(Scanner in) {
        String usrNic, letter;
        System.out.print("Enter user NIC: ");
        usrNic = in.nextLine();
        System.out.print("Enter message : ");
        letter = in.nextLine();
        return new MessageLetter(usrNic, letter);
    }

    static TreeMap<String, Byte> commands = new TreeMap<>();

    static {
        commands.put("q", (byte) -1);
        commands.put("quit", (byte) -1);
        commands.put("m", Protocol.CMD_CHECK_MAIL);
        commands.put("mail", Protocol.CMD_CHECK_MAIL);
        commands.put("u", Protocol.CMD_USER);
        commands.put("users", Protocol.CMD_USER);
        commands.put("l", Protocol.CMD_LETTER);
        commands.put("letter", Protocol.CMD_LETTER);

        commands.put("f", Protocol.CMD_IMAGE);
        commands.put("file", Protocol.CMD_IMAGE);
        commands.put("cf", Protocol.CMD_CHECK_FILES);
        commands.put("check files", Protocol.CMD_CHECK_FILES);
    }

    static byte translateCmd(String str) {
        // returns -1-quit, 0-invalid cmd, Protocol.CMD_XXX
        str = str.trim();
        Byte r = commands.get(str);
        return (r == null ? 0 : r);
    }

    static void printPrompt() {
        System.out.println();
        System.out.print("(q)uit/(m)ail/(u)sers/(l)etter/(f)ile/(cf)check files >");
        System.out.flush();
    }

    static boolean processCommand(Message msg,
                                  ObjectInputStream is, ObjectOutputStream os)
            throws IOException, ClassNotFoundException {
        if (msg != null) {
            os.writeObject(msg);
            MessageResult res = (MessageResult) is.readObject();
            if (res.Error()) {
                System.err.println(res.getErrorMessage());
            } else {
                switch (res.getID()) {
                    case Protocol.CMD_CHECK_MAIL:
                        printMail((MessageCheckMailResult) res);
                        break;
                    case Protocol.CMD_USER:
                        printUsers((MessageUserResult) res);
                        break;
                    case Protocol.CMD_LETTER:
                    case Protocol.CMD_IMAGE:
                        System.out.println("OK...");
                        break;
                    case Protocol.CMD_CHECK_FILES:
                        downloadFiles((MessageCheckFilesResult) res);
                    default:
                        assert (false);
                        break;
                }
            }
            return true;
        }
        return false;
    }

    static void printMail(MessageCheckMailResult m) {
        if (m.letters != null && m.letters.length > 0) {
            System.out.println("Your mail {");
            for (String str : m.letters) {
                System.out.println(str);
            }
            System.out.println("}");
        } else {
            System.out.println("No mail...");
        }
    }

    static void downloadFiles(MessageCheckFilesResult m) throws IOException {
        if (m.files != null && m.files.length > 0) {
            System.out.println("Downloading files...");
            for (int i = 0; i < m.files.length; i++) {
                FileOutputStream fos = new FileOutputStream(m.files[i].name);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bos.write(m.files[i].fileArray);
                bos.flush();
            }
        }
    }

    static void printUsers(MessageUserResult m) {
        if (m.userNics != null) {
            System.out.println("Users {");
            for (String str : m.userNics) {
                System.out.println("\t" + str);
            }
            System.out.println("}");
        }
    }
}
