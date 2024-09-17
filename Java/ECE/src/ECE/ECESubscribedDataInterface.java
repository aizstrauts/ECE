package ECE;

public interface ECESubscribedDataInterface {
    void addData(int tick, Object data);

    Object getLastData();

    int getLastTick();

    Object getDataByTick(int tick);
}
