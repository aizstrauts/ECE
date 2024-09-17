# /*
#  * MIT License
#  *
#  * Copyright (c) 2024 Artis Aizstrauts
#  *
#  * Permission is hereby granted, free of charge, to any person obtaining a copy
#  * of this software and associated documentation files (the "Software"), to deal
#  * in the Software without restriction, including without limitation the rights
#  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  * copies of the Software, and to permit persons to whom the Software is
#  * furnished to do so, subject to the following conditions:
#  *
#  * The above copyright notice and this permission notice shall be included in all
#  * copies or substantial portions of the Software.
#  *
#  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#  * SOFTWARE.
#  */

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
