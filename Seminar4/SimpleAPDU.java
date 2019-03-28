package simpleapdu;

import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Scanner;

/**
 * Test class.
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author Petr Svenda (petrs), Dusan Klinec (ph4r05)
 */
public class SimpleAPDU {
    private static String APPLET_AID = "73696D706C65";
    private static byte APPLET_AID_BYTE[] = Util.hexStringToByteArray(APPLET_AID);

    private static final String STR_APDU_GETRANDOM = "B054100000";

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            SimpleAPDU main = new SimpleAPDU();
            main.assignment_flow();
            
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public void assignment_flow() throws Exception {

        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();
        runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);
        // Connect to first available card
        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            System.out.println(" Failed.");
        }
        System.out.println(" Done.");

        Scanner scanner = new Scanner(System.in);

        int scanned_int = 0;
        int pin_length = 4;
        byte[] pin = new byte[4];
        int max_number_of_attempts = 5; //max attempts defined while setting pin.

        int i = 0;
        int j  = 0;
        while(j<max_number_of_attempts){

            while(i<pin_length){
                System.out.format("Enter pin digit %d:",i+1);
                scanned_int = scanner.nextInt();
                pin[i] = (byte) scanned_int;
                i++;
            }

            CommandAPDU command_verifypin = new CommandAPDU(0xB0, 0x55, 0x00, 0x00, pin); //verify pin.
            ResponseAPDU code_response =  cardMngr.transmit(command_verifypin);
            String swStr = String.format("%02X", code_response.getSW());
            if(swStr.equals("9000")){
                System.out.println("[>]Correct pin was inserted. Moving on through signature.");
                break;
            }else{
                System.out.format("[>]Pin does not match.");
                j++;
            }
        }

        System.out.println("[>]Sending the following data to be signed: 48656c6c6f20776f726c64");
        final String message_to_sign = "48656c6c6f20776f726c64"; //Hello world in hex
        byte[] message_to_sign_bytes = Util.hexStringToByteArray(message_to_sign);
        CommandAPDU command_to_sign = new CommandAPDU(0xB0, 0x58, 0x00, 0x00, message_to_sign_bytes);
        ResponseAPDU response_signature =  cardMngr.transmit(command_to_sign);
        byte[] response_signature_data = response_signature.getData();
        System.out.println("[>]Signed data:"+Util.toHex(response_signature_data));
        System.out.format("[>]Time taken for encryption on card: %d ms.", cardMngr.time);
    }
}
