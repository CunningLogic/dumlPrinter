package com.cunninglogic.dumlprinter;

public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("dumlPrinter 0.1 - by Jon Sawyer - jon@cunninglogic.com\n");
            System.out.println("Usage:");
            System.out.println("java -jar dumlPrinter.jar <packet>\n");
            System.out.println("Example:");
            System.out.println("java -jar dumlPrinter.jar  550D04332A2835124000002AE4");
            return;
        }

        try {
            parse(strToBytes(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void parse(byte[] duml) {

        System.out.println("Packet:\t\t" + bytestoStr(duml));
        if (duml[0] != 0x55) {
            System.out.println("Invalid magic");
            return;
        } else if (duml.length < 13) {
            System.out.println("Invalid packet length, duml packet has a minimum length of 13");
            return;
        } else if (duml.length > 0x01FF) {
            System.out.println("Invalid packet length, duml packet has a maximum length of 511, this may be a DUSS packet");
            return;
        }

        byte[] tmp = new byte[duml.length -2];
        System.arraycopy(duml,0,tmp,0,tmp.length);
        int crc16 = ((duml[duml.length - 1] & 0xFF) << 8) | (duml[duml.length - 2] & 0xFF);
        System.out.println("CRC32:\t\t" + crc16);

        if (crc16 != CRC.calc16(tmp)) {
            System.out.println("Packete CRC mismatch, " + crc16 + " != " + CRC.calc16(tmp));
            return;
        }


        byte[] header = new byte[4];
        System.arraycopy(duml,0,header,0,4);
        System.out.println("\nHeader:\t\t" + bytestoStr(header));

        int len = (header[1] | ((header[2] & 0x03) <<8));
        int ver = (header[2] >> 2);
        int crc8 = header[3] & 0xFF;

        System.out.println("Length:\t\t" + len);
        System.out.println("Version:\t" + ver);
        System.out.println("CRC8:\t\t" + crc8);

        if (ver != 1) {
            System.out.println("Unsupported DUML version, version = " + ver);
            return;
        } else if (len != duml.length) {
            System.out.println("Defined length does not match actual length, " + len + " != " + duml.length);
            return;
        } else if (crc8 != CRC.calc8(new byte[]{header[0],header[1],header[2]})) {
            System.out.println("Header CRC mismatch, " + crc8 + " != " + CRC.calc8(new byte[]{header[0],header[1],header[2]}));
            return;
        }


        byte[] transit = new byte[4];
        System.arraycopy(duml,4,transit,0,4);
        System.out.println("\nTransit:\t" + bytestoStr(transit));

        int src = transit[0] >> 5;
        int srcid = transit[0] & 0x0E;
        int tar = transit[1] >> 5;
        int tarid = transit[1] & 0x0E;
        int seqno = ((transit[3] & 0xFF) << 8) | transit[2] & 0xFF;

        System.out.println("Route:\t\t" + getRouteID(srcid,src) + " -> " + getRouteID(tarid,tar));
        System.out.println("Source:\t\t" + src);
        System.out.println("Source ID:\t" + srcid + "\t\t" + getLoc(srcid));
        System.out.println("Target:\t\t" + tar);
        System.out.println("Target ID:\t" + tarid + "\t\t" + getLoc(tarid));
        System.out.println("Sequence:\t" + seqno);


        byte[] command = new byte[3];
        System.arraycopy(duml,8,command,0,3);
        System.out.println("\nCommand:\t" + bytestoStr(command));

        int cmdType = command[0] & ~0x1F;
        int cmdSet = command[1];
        int cmdID = command[2];


        System.out.println("cmdType:\t" + cmdType + "\t\t" + getCmdType(cmdType));
        System.out.println("cmdSet:\t\t" + cmdSet+ "\t\t" + getCmdSet(cmdSet));
        System.out.println("cmdID:\t\t" + cmdID);



        if (duml.length > 13) {
            byte[] payload = new byte[duml.length - 0x0C];
            System.arraycopy(duml,0x0B,payload,0,payload.length);
            System.out.println("\nPayload:\t" + bytestoStr(payload));
            System.out.println("Length:\t\t" + payload.length);
        }

    }

    private static String getCmdSet(int idx) {
        String cmdSet = "Unknown";
        switch (idx) {
            case 0:
                cmdSet = "Universal";
                break;
            case 1:
                cmdSet = "Special";
                break;
            case 2:
                cmdSet = "Camera";
                break;
            case 3:
                cmdSet = "Flight Controller";
                break;
            case 4:
                cmdSet = "Gimbal";
                break;
            case 5:
                cmdSet = "Mainboard";
                break;
            case 6:
                cmdSet = "Remote Control";
                break;
            case 7:
                cmdSet = "WiFi";
                break;
            case 8:
                cmdSet = "DM368";
                break;
            case 9:
                cmdSet = "HD Map";
                break;
            case 10:
                cmdSet = "VPS / Obstacle Avoidance";
                break;
            case 11:
                cmdSet = "Simulator";
                break;
            case 12:
                cmdSet = "Order";
                break;
            case 13:
                cmdSet = "Smart Battery";
                break;
            case 14:
                cmdSet = "Data Logger";
                break;
            case 15:
                cmdSet = "RTK";
                break;
            case 16:
                cmdSet = "Automated Test";
                break;
        }
        return cmdSet;
    }

    private static String getCmdType(int idx) {
        String cmdType = "Unknown";
        switch (idx) {
            case 0:
                cmdType = "No ACK";
                break;
            case 32:
                cmdType = "Push";
                break;
            case 64:
                cmdType = "ACK";
                break;
            case 128:
                cmdType = "Response";
                break;
        }
        return cmdType;
    }

    private static String getRouteID(int id, int loc) {
        String route = "";
        route = Integer.toString(loc);
        if (route.length() == 1) {
            route = "0" + route;
        }
        route = Integer.toString(id) + route;
        if (route.length() == 3) {
            route = "0" + route;
        }
        return route;
    }

    private static String getLoc(int idx) {
        String location = "Unknown";
        switch (idx) {
            case 1:
                location = "Camera";
                break;
            case 2:
                location = "Mobile App";
                break;
            case 3:
                location = "Flight Controller";
                break;
            case 4:
                location = "Gimbal";
                break;
            case 5:
                location = "Mainboard";
                break;
            case 6:
                location = "Remote Control";
                break;
            case 7:
                location = "Wifi Module Air Side";
                break;
            case 8:
                location = "DM368 Air Side";
                break;
            case 9:
                location = "HD Map Air Side";
                break;
            case 10:
                location = "PC";
                break;
            case 11:
                location = "Smart Battery";
                break;
            case 12:
                location = "ESC";
                break;
            case 13:
                location = "DM368 Ground Side";
                break;
            case 14:
                location = "HD Map Ground Side";
                break;
            case 15:
                location = "String Conversion Air Side";
                break;
            case 16:
                location = "String Conversion Ground Side";
                break;
            case 17:
                location = "VPS";
                break;
            case 18:
                location = "Obstacle Avoidance";
                break;
            case 19:
                location = "HD Graphics Air Side";
                break;
            case 20:
                location = "HD Map Ground Side FPGA";
                break;
            case 21:
                location = "Simulator";
                break;
            case 22:
                location = "Base Station";
                break;
            case 23:
                location = "Airborne Computing Platform";
                break;
            case 24:
                location = "RC Battery";
                break;
            case 25:
                location = "IMU";
                break;
            case 26:
                location = "GPS";
                break;
            case 27:
                location = "WiFi Module Ground Side";
                break;
            case 28:
                location = "Agricultural Machine Signal Conversion Board";
                break;

        }
        return location;
    }

    public static String bytestoStr(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] strToBytes(String input) {
        String s = input.replace(" ", "").trim();
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
/*



radix: hexadecimal
radix: hexadecimal



 */