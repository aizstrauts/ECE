package ECE;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@SuppressWarnings("SpellCheckingInspection")
public class ECE {

    private final static Logger LOGGER = Logger.getLogger(ECE.class.getName());
    static final SubscribedExchanges subscribedExchanges = new SubscribedExchanges();
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public void Init(String host) {
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }
        //LOGGER.info("Connecting to " + host);

        try {
            factory = new ConnectionFactory();
            factory.setHost(host);
            connection = factory.newConnection();
            channel = connection.createChannel();
            //channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            subscribedExchanges.clear();

        } catch (IOException | TimeoutException e) {
            LOGGER.severe("Cannot connect to " + host);

            throw new RuntimeException(e);
        }
    }


    public void close() {
        try {
            channel.close();
            connection.close();
            subscribedExchanges.clear();
        } catch (Exception e) {
            LOGGER.warning(e.toString());
        }
    }

    private void send(String topic, int tick, Object data, ECEMessage.DataType dataType) throws IOException {

        ECEMessage ECE_message = new ECEMessage(tick, dataType, data);
        channel.exchangeDeclare(topic, BuiltinExchangeType.FANOUT);
        channel.basicPublish(topic, "", null, ECE_message.toJSON().getBytes(StandardCharsets.UTF_8));
    }

    public void sendString(String topic, int tick, String data) {
        try {
            send(topic, tick, data, ECEMessage.DataType.STRING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendInteger(String topic, int tick, int data) {
        try {
            send(topic, tick, data, ECEMessage.DataType.INTEGER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDouble(String topic, int tick, double data) {
        try {
            send(topic, tick, data, ECEMessage.DataType.DOUBLE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBoolean(String topic, int tick, boolean data) {
        try {
            send(topic, tick, data, ECEMessage.DataType.BOOLEAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(String topic) {

        subscribedExchanges.addExchange(topic);
        try {
            channel.exchangeDeclare(topic, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, topic, "");

            //System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                try {
                    ECEMessage message = ECEMessage.JSONToECEData(new String(delivery.getBody(), StandardCharsets.UTF_8));

                    //LOGGER.info("£££££££££££££££££££ Receive data type:" + message.getData().getClass());
                    //LOGGER.info(" [x] Received '" + new String(delivery.getBody(), StandardCharsets.UTF_8) + "'");
                    //LOGGER.info(" [x] Message received:");
                    //LOGGER.info("EXCHANGE_NAME:" + EXCHANGE_NAME);
                    //LOGGER.info(delivery.getProperties().toString());
                    //LOGGER.info("Message JSON -> " + message.toJSON());
                    subscribedExchanges.addData(topic, message.tick(), message.data());
                } catch (Exception e) {
                    //System.out.println("Exception @ subscribe @ deliverCallback :" + e);
                    LOGGER.severe(e.toString());
                }
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            //System.out.println("Exception @ subscribe @ deliverCallback :" + e);
            LOGGER.severe(e.toString());
        }
    }


    public void unsubscribe(String topic) {
        subscribedExchanges.removeExchange(topic);
    }

    public List<String> getSubscribedList() {

        List<String> subscribedExchangeNames = new ArrayList<>(List.of(subscribedExchanges.getSubscribedExchanges()));
        Collections.sort(subscribedExchangeNames);
        return subscribedExchangeNames;

    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }


    public String getStringData(String topic, int tick) {
        Object value = subscribedExchanges.getExchangeData(topic).getDataByTick(tick);
        return value != null ? (String) value : "";
    }

    public String getLastStringData(String topic) {
        Object value = subscribedExchanges.getExchangeData(topic).getLastData();
        return value != null ? (String) value : "";
    }

    public int getIntegerData(String topic, int tick) {
        Object value = subscribedExchanges.getExchangeData(topic).getDataByTick(tick);
        return value != null ? (int) value : 0;
    }

    public int getLastIntegerData(String topic) {
        Object value = subscribedExchanges.getExchangeData(topic).getLastData();
        return value != null ? (int) value : 0;
    }


    public double getDoubleData(String topic, int tick) {
        Object value = subscribedExchanges.getExchangeData(topic).getDataByTick(tick);
        return value != null ? (double) value : 0;
    }

    public double getLastDoubleData(String topic) {
        Object value = subscribedExchanges.getExchangeData(topic).getLastData();
        return value != null ? (double) value : 0;
    }

    public boolean getBooleanData(String topic, int tick) {
        Object value = subscribedExchanges.getExchangeData(topic).getDataByTick(tick);
        return value != null && (boolean) value;
    }

    public boolean getLastBooleanData(String topic) {
        Object value = subscribedExchanges.getExchangeData(topic).getLastData();
        return value != null && (boolean) value;
    }

    public int getLastTick(String topic) {
        return subscribedExchanges.getExchangeData(topic).getLastTick();
    }

}
