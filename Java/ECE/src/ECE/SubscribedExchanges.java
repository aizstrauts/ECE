package ECE;

import java.util.HashMap;
import java.util.logging.Logger;

class SubscribedExchanges {
    private final static Logger LOGGER = Logger.getLogger(SubscribedExchanges.class.getName());

    private final HashMap<String, ECESubscribedData> subscribedData = new HashMap<>();

    public void clear() {
        subscribedData.clear();
    }

    public void addExchange(String exchangeName) {
        subscribedData.put(exchangeName, new ECESubscribedData());
    }

    public void removeExchange(String exchangeName) {
        subscribedData.remove(exchangeName);
    }

    public String[] getSubscribedExchanges() {
        if (subscribedData.isEmpty()) {
            return new String[0];
        }
        return subscribedData.keySet().toArray(new String[0]);
    }

    public void addData(String exchangeName, int ECE_tick, Object data) {

        if (!subscribedData.containsKey(exchangeName)) {
            subscribedData.put(exchangeName, new ECESubscribedData());
        }

        subscribedData.get(exchangeName).addData(ECE_tick, data);
    }

    public ECESubscribedData getExchangeData(String exchangeName) {
        if (!subscribedData.containsKey(exchangeName)) {
            subscribedData.put(exchangeName, new ECESubscribedData());
        }

        return subscribedData.get(exchangeName);

    }
}
