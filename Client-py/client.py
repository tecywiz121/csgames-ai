#!/usr/bin/env python3

import socket
import ai


class BombermanClient:
    available_moves = {"Left": "Left",
                       "Right": "Right",
                       "Down": "Down",
                       "Up": "Up",
                       "DropBomb": "DropBomb",
                       "Detonate": "Detonate",
                       "None": "None"}

    def __init__(self, sock=None):
        if sock is None:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        else:
            self.sock = sock

    def connect(self, host, port):
        self.sock.connect((host, port))

    def send_move(self, move):
        move += "\n"
        return self.sock.send(bytes(move, 'UTF-8'))

    def start(self):
        for line in self.readlines():
            if "quit" in line:
                self.sock.close()
                print("-Game ended")
                break
            else:
                map = self.read_map(line)
                self.send_move(self.generate_move(map))

    def readlines(self):
        buffer = self.sock.recv(4096)
        buffer = str(buffer, encoding='utf8')
        done = False
        while not done:
            if "\n" in buffer:
                (line, buffer) = buffer.split("\n", 1)
                yield line+"\n"
            else:
                more = self.sock.recv(4096)
                more = str(more, encoding='utf8')
                if not more:
                    done = True
                else:
                    buffer += more
        if buffer:
            yield buffer

    def read_map(self, line):
        map = []
        i = 0
        for x in line.split(";"):
            if x == "\n":
                break
            map.append([])
            for y in x.split(","):
                map[i].append(y)
            i += 1

        return map

    def generate_move(self, map):
    	return ai.generate_move(self, map)

if __name__ == "__main__":
    s = BombermanClient()
    s.connect("localhost", 9090)
    s.start()
