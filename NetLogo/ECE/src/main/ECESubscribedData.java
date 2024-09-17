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

import java.util.HashMap;
import java.util.Vector;

class ECESubscribedData implements ECESubscribedDataInterface {

    private final int MAX_SIZE = 1000;

    private int max_tick = -1;
    private final Vector<Integer> tickStack = new Vector<>();

    private final HashMap<Integer, Object> dataStorage = new HashMap<>();


    @Override
    public void addData(int tick, Object data) {

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
