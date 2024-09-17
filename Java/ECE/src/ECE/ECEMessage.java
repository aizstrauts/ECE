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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ECEMessage {

    private int tick;
    private DataType dataType;
    private Object data;

    public ECEMessage(int tick, DataType dataType, Object data) {
        this.tick = tick;
        this.dataType = dataType;
        this.data = data;
    }

    public static ECEMessage JSONToECEData(String json) throws IllegalStateException, JsonSyntaxException {
        ECEMessage eceMessage = (new Gson()).fromJson(json, ECEMessage.class);
        switch (eceMessage.dataType) {
            case INTEGER:
                eceMessage = new ECEMessage(eceMessage.tick, eceMessage.dataType, ((Double) eceMessage.data).intValue());
                break;
            case DOUBLE, STRING, BOOLEAN:
                eceMessage = new ECEMessage(eceMessage.tick, eceMessage.dataType, eceMessage.data);
                break;
        }
        ;

        return eceMessage;
    }

    public String toJSON() {
        return (new Gson()).toJson(this);
    }

    public enum DataType {
        INTEGER, DOUBLE, STRING, BOOLEAN
    }

    public int tick() {
        return tick;
    }

    public DataType dataType() {
        return dataType;
    }

    public Object data() {
        return data;
    }
}
