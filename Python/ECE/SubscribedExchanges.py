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

import sqlite3
from typing import Tuple
from ECESubscribedData import ECESubscribedData


class SubscribedExchanges:

    def __init__(self, session):
        self.subscribed_data = {}

        self.session = session
        self.sqlite_connection = sqlite3.connect('ECE.db', check_same_thread=False)

    def add_exchange(self, exchange_name):
        self.subscribed_data[exchange_name] = ECESubscribedData()
        cursor = self.sqlite_connection.cursor()
        cursor.execute('''DROP TABLE IF EXISTS '''+self.session+'_'+exchange_name)
        sql = '''create table if not exists '''+self.session+'_'+exchange_name+'''(
            TICK INT,
            DATATYPE TEXT,
            DATA BLOB
        )'''
        cursor.execute(sql)
        self.sqlite_connection.commit()

    def remove_exchange(self, exchange_name):
        self.subscribed_data.pop(exchange_name, None)

    def get_subscribed_exchanges(self):
        return list(self.subscribed_data.keys())

    def add_data(self, exchange_name, ece_tick, ece_datatype, ece_data):
        if exchange_name not in self.subscribed_data:
            self.subscribed_data[exchange_name] = ECESubscribedData()
        # self.subscribed_data[exchange_name].add_data(ece_tick, ece_data)
        try:
            cursor = self.sqlite_connection.cursor()
            self.sqlite_connection.execute('''INSERT INTO '''+self.session+'_'+exchange_name +
                                           '''(TICK, DATATYPE, DATA) VALUES(?,?,?)''', (ece_tick, ece_datatype, ece_data))
            self.sqlite_connection.commit()
        except Exception as e:
            print("Error while inserting data into database ", e)

    def get_exchange_data(self, exchange_name):
        if exchange_name not in self.subscribed_data:
            self.subscribed_data[exchange_name] = ECESubscribedData()
        return self.subscribed_data[exchange_name]

    def get_last_data(self, exchange_name) -> Tuple[int, str, str] | None:
        cursor = self.sqlite_connection.cursor()
        cursor.execute(f'SELECT TICK, DATATYPE, DATA FROM {self.session}_{exchange_name} ORDER BY TICK DESC LIMIT 1')
        data = cursor.fetchall()
        # print(data)
        self.sqlite_connection.commit()
        if len(data) > 0:
            return data[0]
        else:
            return None

    def get_data_by_tick(self, exchange_name: str, tick_value: int) -> Tuple[int, str, str] | None:
        """
        Retrieve the data associated with a specific tick value for the given exchange name.

        Args:
            exchange_name (str): The name of the exchange to retrieve data from.
            tick_value (int): The specific tick value to query.

        Returns:
            Tuple[int, str, str] | None: The data associated with the specific tick value if available, otherwise None.
        """
        cursor = self.sqlite_connection.cursor()
        cursor.execute(f'SELECT TICK, DATATYPE, DATA FROM {self.session}_{
                       exchange_name} WHERE TICK = ?', (tick_value,))
        data = cursor.fetchall()
        self.sqlite_connection.commit()
        if len(data) > 0:
            return data[0]
        else:
            return None

    def get_last_tick(self, exchange_name: str) -> int | None:
        """
        Retrieve the last or the largest tick value for the given exchange name.

        Args:
            exchange_name (str): The name of the exchange to retrieve the tick value from.

        Returns:
            int | None: The last or largest tick value if available, otherwise None.
        """
        cursor = self.sqlite_connection.cursor()
        cursor.execute(f'SELECT MAX(TICK) FROM {self.session}_{exchange_name}')
        result = cursor.fetchone()
        self.sqlite_connection.commit()
        if result is not None:
            return result[0]
        return None
