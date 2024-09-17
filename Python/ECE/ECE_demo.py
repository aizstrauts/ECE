#!/usr/bin/env python

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
