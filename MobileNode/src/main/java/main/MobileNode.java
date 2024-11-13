package main;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ckafka.data.SwapData;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import main.java.ckafka.mobile.CKMobileNode;
import main.java.ckafka.mobile.tasks.SendLocationTask;

/**
 * @authors João Biscaia, Thomas de Mello, Marcello Nascif
 *
 */
public class MobileNode extends CKMobileNode {
    protected String nomeNode;
    protected double latitude;
	protected double longitude;
    
    /**
     * Constructor
     */

    public MobileNode(){
    }

    public MobileNode(String nomeNode, double latitude, double longitude) {
		super();
        this.nomeNode = nomeNode;
		this.latitude = latitude;
		this.longitude = longitude;
	}

    public String getNomeNode(){
        return nomeNode;
    }

    public UUID getUUID() {
        return this.mnID;
    }

    /**
     * Sends a message to processing nodes<br>
     *
     * @param keyboard
     */

    private void sendMessageToPN(Scanner keyboard) {
        System.out.print("Entre com a mensagem para o PN: ");
        String messageText = keyboard.nextLine();

        ApplicationMessage message = createDefaultApplicationMessage();
        SwapData data = new SwapData();

        data.setMessage(messageText.getBytes(StandardCharsets.UTF_8));
        data.setTopic("AppModel");
        message.setContentObject(data);

        sendMessageToGateway(message);
    }

    /**
     * fazTudo<br>
     * Read user option from keyboard (unicast or groupcast message)<br>
     * Read destination receipt from keyboard (UUID or Group)<br>
     * Read message from keyboard<br>
     * Send message<br>
     */
    public void fazTudo() {
        /*
         * User interface (!)
         */
        Scanner keyboard = new Scanner(System.in);
        boolean fim = false;
        while(!fim) {
            System.out.print("Mensagem para (P)rocessing Node (Z para terminar)? ");
            String linha = keyboard.nextLine();
            linha = linha.toUpperCase();

            System.out.println(String.format("Sua opção foi %s.", linha));
            switch (linha) {
                case "P":
                    sendMessageToPN(keyboard);
                    break;
                case "Z":
                    fim = true;
                    break;

                default:
                    System.out.println("Opção inválida");
                    break;
            }

            if (linha.equals("Z")) {
                break;
            }
        }

        keyboard.close();
        System.out.println("FIM!");
        System.exit(0);
    }

    /**
     * Sends a unicast message
     * @param keyboard
     */
    private void sendUnicastMessage(Scanner keyboard) {
        System.out.println("Mensagem unicast. Entre com o UUID do indivíduo:\n"
                + "HHHHHHHH-HHHH-HHHH-HHHH-HHHHHHHHHHHH");
        String uuid = keyboard.nextLine();
        System.out.print("Entre com a mensagem: ");
        String messageText = keyboard.nextLine();
        System.out.println(String.format("Enviando mensagem |%s| para o indivíduo %s.", messageText, uuid));

        // Create and send the message
        SwapData privateData = new SwapData();
        privateData.setMessage(messageText.getBytes(StandardCharsets.UTF_8));
        privateData.setTopic("PrivateMessageTopic");
        privateData.setRecipient(uuid);
        ApplicationMessage message = createDefaultApplicationMessage();
        message.setContentObject(privateData);
        sendMessageToGateway(message);
    }

    /**
     * sendGroupcastMessage<br>
     * Sends a groupcast message<br>
     * @param keyboard
     */
    private void sendGroupcastMessage(Scanner keyboard) {
        // get message content
        String group;
        System.out.print("Mensagem groupcast. Entre com o número do grupo: ");
        group = keyboard.nextLine();
        System.out.print("Entre com a mensagem: ");
        String messageText = keyboard.nextLine();
        System.out.println(String.format("Enviando mensagem |%s| para o grupo %s.", messageText, group));
        // create and send the message
        SwapData groupData = new SwapData();
        groupData.setMessage(messageText.getBytes(StandardCharsets.UTF_8));
        groupData.setTopic("GroupMessageTopic");
        groupData.setRecipient(group);
        ApplicationMessage message = createDefaultApplicationMessage();
        message.setContentObject(groupData);
        sendMessageToGateway(message);
    }

    /**
     * Method called when the mobile node connects with the Gateway
     *
     * @post send location task is scheduled
     */
    @Override
    public void connected(NodeConnection nodeConnection) {
        try{
            logger.debug("Connected");
            final SendLocationTask sendlocationtask = new SendLocationTask(this);
            this.scheduledFutureLocationTask = this.threadPool.scheduleWithFixedDelay(sendlocationtask, 5000, 60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Error scheduling SendLocationTask", e);
        }
    }

    /**
     *
     */
    @Override
    public void newMessageReceived(NodeConnection nodeConnection, Message message) {
        logger.debug("New Message Received");
        try {
            SwapData swp = fromMessageToSwapData(message);
            if(swp.getTopic().equals("Ping")) {
                message.setSenderID(this.mnID);
                sendMessageToGateway(message);
            } else {
                String str = new String(swp.getMessage(), StandardCharsets.UTF_8);
                logger.info(String.format("Message received from %s: %s", message.getRecipientID(), str));
            }
        } catch (Exception e) {
            logger.error("Error reading new message received");
        }
    }

    @Override
    public void disconnected(NodeConnection nodeConnection) {}

    @Override
    public void unsentMessages(NodeConnection nodeConnection, List<Message> list) {}

    @Override
    public void internalException(NodeConnection nodeConnection, Exception e) {}

}
