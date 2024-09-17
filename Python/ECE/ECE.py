from collections import deque
import json
from typing import Any
from webbrowser import get
from ECESubscribedDataInterface import ECESubscribedDataInterface
from SubscribedExchanges import SubscribedExchanges
import pika
import threading
import sqlite3


class _ECE:
    def __init__(self, session=None):
        self.MAX_SIZE = 1000
        self.max_tick = -1

        self.tick_stack = deque(maxlen=self.MAX_SIZE)
        self.session = session
        self.data_storage = {}
        self.subscribed_exchanges = SubscribedExchanges(self.session)
        self.subscribe_channels = {}

    def init(self, host="localhost", ):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host=host))
        self.channel = self.connection.channel()
        # print("ECE initialized", self.connection.is_open)

    def close(self):
        if len(self.get_subscribed_list()) > 0:
            for exchange_name in self.get_subscribed_list():
                self.unsubscribe(exchange_name)

        if self.connection and self.connection.is_open:
            self.channel.close()
            self.connection.close()

        # self.subscribed_exchanges = SubscribedExchanges(self.sqlite_connection, self.session)
        # if self.sqlite_connection != None:
        #     self.sqlite_connection.close()

    def sendDouble(self, exchange_name: str, tick: int, data: Any):
        print("ECE sendDouble: ", exchange_name, " tick: ", tick, " data: ", data)
        self.send(exchange_name, tick, data, "DOUBLE")

    def send(self, exchange_name: str, tick: int, data: Any, data_type: str):
        print("Sending to exchange: ", exchange_name, " tick: ",
              tick, " data: ", data, " data_type: ", data_type)
        message = {
            "tick": tick,
            "dataType": data_type,
            "data": data
        }

        try:
            self.channel.exchange_declare(
                exchange=exchange_name, exchange_type='fanout')
        except Exception as e:
            print("Error sending message: ", e)

        self.channel.basic_publish(
            exchange=exchange_name, routing_key='', body=json.dumps(message))

    def subscribe(self, exchange_name: str):
        print("Subscribing to exchange: ", exchange_name)

        self.subscribed_exchanges.add_exchange(exchange_name)

        self.channel.exchange_declare(exchange=exchange_name, exchange_type='fanout')
        result = self.channel.queue_declare(queue='', exclusive=True)
        queue_name = result.method.queue

        self.channel.queue_bind(exchange=exchange_name, queue=queue_name)

        def callback(ch, method, properties, body):
            print(" [x] %r" % body)
            data = json.loads(body)
            self.subscribed_exchanges.add_data(exchange_name, data['tick'], data['dataType'], data['data'])

        self.channel.basic_consume(
            queue=queue_name,
            on_message_callback=callback,
            auto_ack=True
        )

    def start_consuming(self):
        try:
            self.channel.start_consuming()
        except KeyboardInterrupt:
            self.channel.stop_consuming()
            self.connection.close()
            print('interrupted!')

    def unsubscribe(self, exchange_name):
       # if self.subscribe_connections[exchange_name]["channel"]:
       #     self.subscribe_connections[exchange_name]["channel"].stop_consuming()
        # self.subscribe_connections[exchange_name]["channel"].close()

        # self.subscribe_connections[exchange_name]["connection"].close()
        self.subscribed_exchanges.remove_exchange(exchange_name)

    def get_subscribed_list(self):
        return self.subscribed_exchanges.get_subscribed_exchanges()

    def is_connection_open(self):
        return self.connection.is_open if self.connection else False

    def get_string_data(self, exchange_name, tick):
        return self.subscribed_exchanges.get_exchange_data(exchange_name).get_data_by_tick(tick)

    def get_last_string_data(self, exchange_name: str) -> str:
        """
        Retrieve the last string data associated with the given exchange name.

        Args:
            exchange_name (str): The name of the exchange to retrieve data from.

        Returns:
            str: The last string data if available, otherwise an empty string.
        """
        last_data = self.subscribed_exchanges.get_last_data(exchange_name)
        if last_data is None:
            return ""

        _, data_type, data = last_data
        if data_type == "STRING":
            return data

        return ""

    def get_last_int_data(self, exchange_name) -> int:
        if self.subscribed_exchanges.get_last_data(exchange_name) == None:
            return 0
        else:
            (tick, dataType, data) = self.subscribed_exchanges.get_last_data(exchange_name)  # type: ignore
            # print("Data: ", data)
            return int(data) if data != None else 0

    def get_last_tick(self, exchange_name) -> int:
        last_tick = self.subscribed_exchanges.get_last_tick(exchange_name)
        if last_tick is not None:
            return last_tick
        else:
            # Handle the None case, for example, return a default value or raise an exception
            return -1  # or any other appropriate default value or action


class ECE:
    def __init__(self, host="localhost", *subscribe_exchange_names: str):
        # TODO: generate session id
        self.session = "test"
        self.ece_subscribe = _ECE(self.session)
        self.ece_subscribe.init(host)
        for exchange_name in subscribe_exchange_names:
            self.ece_subscribe.subscribe(exchange_name)

        self.ece = _ECE(self.session)

        self.ece_send = _ECE(self.session)
        self.ece_send.init(host)

        try:
            t1 = threading.Thread(target=self.ece_subscribe.start_consuming)
            t1.start()
            # t1.join()
        except:
            pass

    def get_last_string_data(self, exchange_name: str) -> str:
        return self.ece.get_last_string_data(exchange_name)

    def get_last_int_data(self, exchange_name: str) -> int:
        return self.ece.get_last_int_data(exchange_name)

    def sendDouble(self, exchange_name: str, tick: int, data: Any):
        print("MAIN ECE sendDouble: ", exchange_name, " tick: ",
              tick, " data: ", data)
        self.ece_send.sendDouble(exchange_name, tick, data)

    def close(self):
        self.ece.close()
        self.ece_subscribe.close()
        self.ece_send.close()
