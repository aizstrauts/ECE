#!/usr/bin/env python

import time

from ECE import ECE


def main():

    ece = ECE("localhost", "TEST_A", "TEST_B")
    tick = 0

    try:

        while True:

            test_a = ece.get_last_string_data('TEST_A')
            test_b = ece.get_last_string_data('TEST_B')
            if test_a is not "" and test_b is not "" and test_a == test_b:
                print('TEST_A and TEST_B are equal')
                ece.sendDouble('TEST_C', tick, 'TEST_A and TEST_B are equal')

            tick+=1
            time.sleep(2)


    except KeyboardInterrupt:
        print('interrupted!')
        ece.close()


if __name__ == '__main__':
    main()
