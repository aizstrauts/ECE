from collections import deque
from ECESubscribedDataInterface import ECESubscribedDataInterface


class ECESubscribedData(ECESubscribedDataInterface):

    def __init__(self):
        self.MAX_SIZE = 1000
        self.max_tick = -1
        self.tick_stack = deque(maxlen=self.MAX_SIZE)
        self.data_storage = {}

    def add_data(self, tick, data):
        if tick > self.max_tick:
            self.max_tick = tick

        self.tick_stack.append(tick)
        self.data_storage[tick] = data

    def get_last_data(self):
        if not self.tick_stack:
            return None
        else:
            return self.data_storage[self.max_tick]

    def get_last_tick(self):
        return self.max_tick

    def get_data_by_tick(self, tick):
        return self.data_storage.get(tick, None)
