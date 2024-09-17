package ECE;

import java.util.HashMap;
import java.util.Vector;

class ECESubscribedData implements ECESubscribedDataInterface {

    private final Vector<Integer> tickStack = new Vector<>();
    private final HashMap<Integer, Object> dataStorage = new HashMap<>();
    private int max_tick = -1;

    @Override
    public void addData(int tick, Object data) {

        int MAX_SIZE = 1000;
        if (this.tickStack.size() > MAX_SIZE) {
            int firstTickValue = tickStack.firstElement();
            this.tickStack.remove(0);
            this.dataStorage.remove(firstTickValue);
        }

        if (tick > max_tick) {
            max_tick = tick;
        }

        this.tickStack.add(tick);
        this.dataStorage.put(tick, data);
    }

    @Override
    public Object getLastData() {
        if (this.tickStack.isEmpty()) {
            return null;
        } else {
            return this.dataStorage.get(max_tick);
        }
    }

    @Override
    public int getLastTick() {
        return this.max_tick;
    }

    @Override
    public Object getDataByTick(int tick) {

        if (this.tickStack.contains(tick) && this.dataStorage.containsKey(tick)) {
            return this.dataStorage.get(tick);
        } else {
            return null;
        }
    }
}
