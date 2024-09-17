from abc import ABC, abstractmethod


class ECESubscribedDataInterface(ABC):

    @abstractmethod
    def add_data(self, tick, data):
        pass

    @abstractmethod
    def get_last_data(self):
        pass

    @abstractmethod
    def get_last_tick(self):
        pass

    @abstractmethod
    def get_data_by_tick(self, tick):
        pass
