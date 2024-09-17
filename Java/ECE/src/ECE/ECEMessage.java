/*
package ECE;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

record ECEMessage(int tick, ECEMessage.DataType dataType, Object data) {

    static public ECEMessage JSONToECEData(String json) throws IllegalStateException, JsonSyntaxException {
        ECEMessage eceMessage = (new Gson()).fromJson(json, ECEMessage.class);
        return switch (eceMessage.dataType) {
            case INTEGER ->
                    new ECEMessage(eceMessage.tick(), eceMessage.dataType(), ((Double) eceMessage.data).intValue());
            case DOUBLE, STRING, BOOLEAN -> new ECEMessage(eceMessage.tick(), eceMessage.dataType(), eceMessage.data);
        };
    }

*/
/*
{
  "tick": 1,
  "dataType": "INTEGER",
  "data": 123
}
*//*


    public String toJSON() {
        return (new Gson()).toJson(this);
    }

    public enum DataType {
        INTEGER, DOUBLE, STRING, BOOLEAN
    }


}
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