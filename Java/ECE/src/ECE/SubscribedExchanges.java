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
