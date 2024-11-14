package main;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

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

    public MobileNode(String nomeNode) {
		super();
        this.nomeNode = nomeNode;
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
            System.out.print("Digite Z para terminar ");
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
                logger.info(String.format("Mensagem recebida do PN: %s", str));
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
