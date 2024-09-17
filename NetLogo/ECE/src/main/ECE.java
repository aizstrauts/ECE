

/*
 * MIT License
 *
 * Copyright (c) 2024 Artis Aizstrauts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.rabbitmq.client.*;
import org.nlogo.api.Command;
import org.nlogo.api.*;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;


public class ECE extends DefaultClassManager {

    private final static Logger LOGGER = Logger.getLogger(ECE.class.getName());
    static SubscribedExchanges subscribedExchanges = new SubscribedExchanges();
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public void send(String EXCHANGE_NAME, int tick, Argument data, ECEMessage.DataType dataType) throws IOException, ExtensionException {

        Object message;
        switch (dataType) {
            case INTEGER:
                message = data.getIntValue();
                break;
            case DOUBLE:
                message = data.getDoubleValue();
                break;
            case STRING:
                message = data.getString();
                break;
            case BOOLEAN:
                message = data.getBooleanValue();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataType);
        }

        ECEMessage ECE_message = new ECEMessage(tick, dataType, message);

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.basicPublish(EXCHANGE_NAME, "", null, ECE_message.toJSON().getBytes(StandardCharsets.UTF_8));
    }


    public void load(PrimitiveManager primitiveManager) {
        primitiveManager.addPrimitive("init", new Init());
        primitiveManager.addPrimitive("close", new Close());
        primitiveManager.addPrimitive("sendNumber", new SendNumber());
        primitiveManager.addPrimitive("sendString", new SendString());
        //primitiveManager.addPrimitive("send", new Send());
        primitiveManager.addPrimitive("subscribe", new Subscribe());
        primitiveManager.addPrimitive("unsubscribe", new Unsubscribe());
        primitiveManager.addPrimitive("getSubscribedList", new GetSubscribedList());
        primitiveManager.addPrimitive("isConnectionOpen", new IsConnectionOpen());
        primitiveManager.addPrimitive("getStringData", new GetStringData());
        primitiveManager.addPrimitive("getLastStringData", new GetLastStringData());
        primitiveManager.addPrimitive("getNumberData", new GetNumberData());
        primitiveManager.addPrimitive("getLastNumberData", new GetLastNumberData());
        primitiveManager.addPrimitive("getLastTick", new GetLastTick());

    }

    public void unload(ExtensionManager pm) {
        try {
            channel.close();
            connection.close();
            factory = null;
            subscribedExchanges = new SubscribedExchanges();
        } catch (Exception e) {
            // LOGGER.warning("Extension Unload:" + e.toString());
        }
    }

    private class Init implements Command {


        @Override
        public void perform(Argument[] args, Context context) throws ExtensionException {

            String host = "localhost";

            if ((args != null) && (args.length > 0)) {
                host = String.valueOf((args[0]).getString());
            }

            //LOGGER.info("Connecting to " + host);

            try {
                factory = new ConnectionFactory();
                factory.setHost(host);
                connection = factory.newConnection();
                channel = connection.createChannel();
                //channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            } catch (IOException | TimeoutException e) {
                LOGGER.severe("Cannot connect to " + host);

                throw new RuntimeException(e);
            }
        }

        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.StringType()});
        }
    }

    private class Close implements Command {

        @Override
        public void perform(Argument[] args, Context context) {
            try {
                channel.close();
                connection.close();
                subscribedExchanges = new SubscribedExchanges();
            } catch (Exception e) {
                LOGGER.warning(e.toString());
            }
        }

        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax();
        }
    }

    private class SendString implements Command {
        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.StringType()});
        }

        @Override
        public void perform(Argument[] args, Context context) throws ExtensionException {
            try {
                send(args[0].getString(), args[1].getIntValue(), args[2], ECEMessage.DataType.STRING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class SendNumber implements Command {
        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType()});
        }

        @Override
        public void perform(Argument[] args, Context context) throws ExtensionException {
            try {
                send(args[0].getString(), args[1].getIntValue(), args[2], ECEMessage.DataType.DOUBLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private class Subscribe implements Command {

        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.StringType()});
        }


        @Override
        public void perform(Argument[] args, Context context) throws ExtensionException {
            for (Argument arg : args) {
                String EXCHANGE_NAME = arg.getString();
                subscribedExchanges.addExchange(EXCHANGE_NAME);
                try {
                    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME, "");

                    //System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                        ECEMessage message = ECEMessage.JSONToECEData(new String(delivery.getBody(), StandardCharsets.UTF_8));


                        //LOGGER.info("£££££££££££££££££££ Receive data type:" + String.valueOf(message.getData().getClass()));

//                        LOGGER.info(" [x] Received '" + new String(delivery.getBody(), StandardCharsets.UTF_8) + "'");
//                        LOGGER.info(EXCHANGE_NAME);
//                        LOGGER.info(queueName);
//                        LOGGER.info(consumerTag);
//                        LOGGER.info(delivery.getProperties().toString());
//                        LOGGER.info("Message JSON -> " + message.toJSON());


                        subscribedExchanges.addData(EXCHANGE_NAME, message.getTick(), message.getData());

                    };
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                    LOGGER.severe(e.toString());
                }

            }
        }
    }

    private static class Unsubscribe implements Command {

        @Override
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.StringType()});
        }

        @Override
        public void perform(Argument[] args, Context context) throws ExtensionException {
            for (Argument arg : args) {
                subscribedExchanges.removeExchange(arg.getString());
                //TODO: unsubscribe for rabbitmq topic/exchange
            }
        }
    }

    public static class GetSubscribedList implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{}, Syntax.ListType());
        }


        public LogoList report(Argument[] args, Context context) throws LogoException {
            LogoListBuilder list = new LogoListBuilder();
            List<String> subscribedExchangeNames = new ArrayList<>(List.of(subscribedExchanges.getSubscribedExchanges()));
            Collections.sort(subscribedExchangeNames);
            for (String topic : subscribedExchangeNames) {
                list.add(topic);
            }
            return list.toLogoList();
        }
    }

    public class IsConnectionOpen implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{}, Syntax.BooleanType());
        }

        public Object report(Argument[] args, Context context) throws LogoException {
            // LOGGER.info("Connection.isOpen:" + String.valueOf(connection.isOpen()));
            return connection.isOpen();
        }
    }

    public static class GetStringData implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType(), Syntax.NumberType()}, Syntax.StringType());
        }


        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
            String EXCHANGE_NAME = args[0].getString();
            int tick = args[1].getIntValue();
            Object value = subscribedExchanges.getExchangeData(EXCHANGE_NAME).getDataByTick(tick);
            return value != null ? (String) value : "";
        }
    }

    public static class GetLastStringData implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType()}, Syntax.StringType());
        }


        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
            String EXCHANGE_NAME = args[0].getString();
            Object value = subscribedExchanges.getExchangeData(EXCHANGE_NAME).getLastData();
            return value != null ? (String) value : "";
        }
    }


    public static class GetNumberData implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType(), Syntax.NumberType()}, Syntax.NumberType());
        }


        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
            String EXCHANGE_NAME = args[0].getString();
            int tick = args[1].getIntValue();
            Object value = subscribedExchanges.getExchangeData(EXCHANGE_NAME).getDataByTick(tick);
            return value != null ? (Double) value : 0;
        }
    }

    public static class GetLastNumberData implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType()}, Syntax.NumberType());
        }

        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
            String EXCHANGE_NAME = args[0].getString();
            Object value = subscribedExchanges.getExchangeData(EXCHANGE_NAME).getLastData();
            return value != null ? (Double) value : 0;
        }
    }

    public static class GetLastTick implements Reporter {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType()}, Syntax.NumberType());
        }

        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
            String EXCHANGE_NAME = args[0].getString();
            int value = subscribedExchanges.getExchangeData(EXCHANGE_NAME).getLastTick();
            return (double) value;
        }
    }


}

