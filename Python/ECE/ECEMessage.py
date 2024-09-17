import json
from enum import Enum


class DataType(Enum):
    INTEGER = "INTEGER"
    DOUBLE = "DOUBLE"
    STRING = "STRING"
    BOOLEAN = "BOOLEAN"


class ECEMessage:

    def __init__(self, tick, data_type, data):
        self.tick = tick
        self.data_type = data_type
        self.data = data

    @staticmethod
    def json_to_ece_data(json_str):
        data_dict = json.loads(json_str)
        return ECEMessage(data_dict['tick'], DataType(data_dict['dataType']), data_dict['data'])

    def to_json(self):
        return json.dumps(self.__dict__, default=lambda o: o.value if isinstance(o, Enum) else o.__dict__)

    def get_tick(self):
        return self.tick

    def get_data_type(self):
        return self.data_type

    def get_data(self):
        return self.data
