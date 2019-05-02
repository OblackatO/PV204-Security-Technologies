package simpleapdu;

import applets.SimpleApplet;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;
import javacard.framework.JCSystem;
import javacard.security.AESKey;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
            
            //main.demoGetRandomDataCommand();
            //main.demoEncryptDecrypt(); //tasks1-4 are here.
            //main.demoUseRealCard();
            main.assignment_flow();
            
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public void demoEncryptDecrypt(CardManager cardMngr) throws Exception {
        // Task 1
        // TODO: Prepare and send APDU with 32 bytes of data for encryption, observe output
        System.out.println("********************************TASK1 starts***********************************************");
        //32bytes of data for encryption, generated from:
        // https://www.random.org/bytes/
        String STR_APDU_temp = "6a12c9c7d4479e0b96bc45e0b8d97387821f4d58d3487eb26345df6aa9246701";
        final String STR_APDU = STR_APDU_temp.trim().toUpperCase();
        System.out.println("[>]Data to be encrypted:"+STR_APDU);

        // Transmit single APDU
        byte[] message_to_encrypt = Util.hexStringToByteArray(STR_APDU);
        CommandAPDU command = new CommandAPDU(0xB0, 0x50, 0x00, 0x00, message_to_encrypt);
        final ResponseAPDU response = cardMngr.transmit(command);
        System.out.println("*********************************TASK1 ends*************************************************");
        Thread.sleep(6000);

        // Task 2
        // TODO: Extract the encrypted data from the card's response. Send APDU with this data for decryption
        System.out.println("\n*******************************TASK2 starts**************************************************");
        byte[] data = response.getData();
        CommandAPDU decrypt_command = new CommandAPDU(0xB0, 0x51, 0x00, 0x00, data); //where 0x51 stands for decryption.
        final ResponseAPDU decryption_response = cardMngr.transmit(decrypt_command);
        byte[] data_decrypted_message = decryption_response.getData();
        // TODO: Compare match between data for encryption and decrypted data
        if(Util.toHex(data_decrypted_message).equals(STR_APDU)){
            System.out.println("[>]Decrypted data matches data for encryption.");
        }else{
            System.out.println("decrypted message:"+Util.toHex(data_decrypted_message));
            System.out.println("MEssage to encrypt:"+STR_APDU);
        }

        System.out.println("*******************************TASK2 ends*****************************************************");


        // Task 3
        // TODO: What is the value of AES key used inside applet? Use debugger to figure this out

        // Task 4
        System.out.println("\n**********************************TASK4 starts**************************************************");
        // TODO: Prepare and send APDU for setting different AES key, then encrypt and verify (with http://extranet.cryptomathic.com/aescalc/index
        //key generated here: http://www.digitalsanctuary.com/aes-key-generator.php
        byte[] aes_key = Util.hexStringToByteArray("50be8bb19b3df570ff8a092a823b6ed4d273fc91bdba5c5eec52cd124d4552fe".toUpperCase());
        CommandAPDU command_setkey = new CommandAPDU(0xB0, 0x52, 0x00, 0x00, aes_key);
        final ResponseAPDU response2 = cardMngr.transmit(command_setkey);
        System.out.println("[>]New key successfully set.");
        System.out.println("[>]Data to be encrypted:"+STR_APDU);
        CommandAPDU command2 = new CommandAPDU(0xB0, 0x50, 0x00, 0x00, message_to_encrypt);
        data = cardMngr.transmit(command2).getData();
        CommandAPDU decrypt_command2 = new CommandAPDU(0xB0, 0x51, 0x00, 0x00, data); //where 0x51 stands for decryption.
        data = cardMngr.transmit(decrypt_command2).getData();
        System.out.println("*************************************TASK4 ends***************************************************");

    }

    public void demoGetRandomDataCommand(CardManager cardMngr) throws Exception {

        // Transmit single APDU
        final ResponseAPDU response = cardMngr.transmit(new CommandAPDU(Util.hexStringToByteArray(STR_APDU_GETRANDOM)));
        byte[] data = response.getData();

        //final ResponseAPDU response2 = cardMngr.transmit(new CommandAPDU(0xB0, 0x50, 0x00, 0x00, data)); // Use other constructor for CommandAPDU

        System.out.println("[>]Response from random data:"+response);
    }


    public void demoUseRealCard() throws Exception {
        // CardManager abstracts from real or simulated card, provide with applet AID
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        // Get default configuration for subsequent connection to card (personalized later)
        final RunConfig runCfg = RunConfig.getDefaultConfig();
        // A) If running on physical card
        runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL); // Use real card
        // B) If running in the simulator
        runCfg.setAppletToSimulate(SimpleApplet.class); // main class of applet to simulate
        //runCfg.setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL); // Use local simulator

        // Connect to first available card
        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            System.out.println(" Failed.");
        }
        System.out.println(" Done.");

        // Task 5 
        // TODO: Obtain random data from real card
        demoGetRandomDataCommand(cardMngr);
        // Task 6 
        // TODO: Set new key value and encrypt on card
        demoEncryptDecrypt(cardMngr);

        cardMngr.Disconnect(true);
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
        int max_number_of_attempts = 5;

        int i = 0;
        int j  = 0;
        while(j<max_number_of_attempts){

            while(i<pin_length){
                System.out.format("Enter pin digit %d:",i+1);
                scanned_int = scanner.nextInt();
                pin[i] = (byte) scanned_int;
                i++;
            }

            CommandAPDU command_verifypin = new CommandAPDU(0xB0, 0x55, 0x00, 0x00, pin);
            ResponseAPDU code_response =  cardMngr.transmit(command_verifypin);
            String swStr = String.format("%02X", code_response.getSW());
            if(swStr.equals("9000")){
                System.out.println("[>]Correct pin was inserted. Moving on through signature.");
                break;
            }else{
                System.out.format("[>]Pin does not match response code:");
                j++;
            }
        }

        //final static byte INS_SIGNDATA = (byte) 0x58;
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
